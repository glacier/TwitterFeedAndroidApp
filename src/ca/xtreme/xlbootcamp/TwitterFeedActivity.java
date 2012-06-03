package ca.xtreme.xlbootcamp;

import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ListView;


public class TwitterFeedActivity extends ListActivity implements OnClickListener {

	public static final String TAG = "TwitterFeedActivity";
	private TwitterClient twitter;
	private Handler mHandler = new Handler();
	private Timer mTimer = null;
	private TwitterCursorAdapter mCursorAdapter;
	private String mSearchString = "bieber";
	private boolean mConnected = true;
	
	// Dialog IDs
	static final int DIALOG_NOT_CONNECTED_ID = 0;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		if(isConnectedToNetwork()) {
			// Creates and sets a "cascading list" animation
			// programmatically.  A better way might be to encode this
			// as an animation resource.
			AnimationSet set = new AnimationSet(true);

			Animation animation = new AlphaAnimation(0.0f, 1.0f);
			animation.setDuration(50);
			set.addAnimation(animation);

			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, -1.0f,Animation.RELATIVE_TO_SELF, 0.0f
					);
			animation.setDuration(200);
			set.addAnimation(animation);

			LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
			ListView listView = getListView();        
			listView.setLayoutAnimation(controller);

			// Populate the initial list view with tweets
			twitter = new TwitterClient(this, mSearchString);
			setupListView(mSearchString);
			new DownloadTweetTask().execute();

			// Check for new tweets every 30 seconds
			mTimer = new Timer();
			mTimer.scheduleAtFixedRate(new TweetTimerTask(), 30000, 30000);
		} else {
			showDialog(DIALOG_NOT_CONNECTED_ID);
		}
	}
	
	protected Dialog onCreateDialog(int id, Bundle args) {
		Dialog dialog;
		
		switch(id) {
		case DIALOG_NOT_CONNECTED_ID:
			//define dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("No internet connectivity found.")
			       .setCancelable(false)
			       .setNegativeButton("OK", new DialogInterface.OnClickListener() {
			    	   public void onClick(DialogInterface dialog, int id) {
			    		   dialog.cancel();
			    	   }
			       });
			dialog = builder.create();
			break;
		default:
			dialog = null;
		}
		
		return dialog;
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d(TAG, "onStart() is called");
	}
	
	@Override
	protected void onStop() {
		Log.d(TAG, "onStop() is called");
		super.onStop();
		if(mTimer != null) {
			mTimer.cancel();
		}
	}
	
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause() is called");
		super.onPause();
		if(mTimer != null) {
			mTimer.cancel();
		}
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume() is called");
		super.onResume();

		if(mConnected) {
			// Reschedule the timer task
			mTimer = new Timer();
			mTimer.scheduleAtFixedRate(new TweetTimerTask(), 30000, 30000);
		}
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(TAG, "onRestart() is called");
	}
	
	public void onClick(View v) {
		if(mTimer != null) {
			mTimer.cancel();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Launch activity to insert a new item
		startActivityForResult(new Intent(Intent.ACTION_EDIT), 1);
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult is called");
		if(data != null) {
			Bundle extras = data.getExtras();
			String searchString = extras.getString("ca.xtreme.xlbootcamp.Hashtag");

			if (searchString == null) {
				searchString = "bieber";
			}
			mSearchString = searchString;
			twitter = new TwitterClient(this, searchString);
			setupListView(searchString);
			new DownloadTweetTask().execute();
		} else {
			Log.d(TAG, "Intent data was null. requestCode=" 
					    + requestCode + " resultCode=" + resultCode);
		}
	}

	private boolean isConnectedToNetwork() {
		ConnectivityManager connMgr = (ConnectivityManager) 
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo == null || !networkInfo.isConnected()) {
			mConnected = false;
			return false;
		}
		mConnected = true;
		return true;
	}
	
	private void setupListView(String searchString) {
		// Retrieve the tweets with hashtag mSearchString from the database
		Uri uri = Uri.withAppendedPath(Twitter.Tweets.CONTENT_URI, "#" + searchString);
		Cursor cursor = getApplication().getContentResolver().query(uri, null, null, null, null);
		startManagingCursor(cursor);

		// Initialize a custom CursorAdapter and populate the list view
		// using data from the content provider returned via the cursor
		mCursorAdapter = new TwitterCursorAdapter(TwitterFeedActivity.this, R.layout.list_item, cursor);
		ListView list = (ListView) findViewById(android.R.id.list);
		list.setAdapter(mCursorAdapter);
		
		// Animate list layout
		list.startLayoutAnimation();
	}

	private class TweetTimerTask extends TimerTask {
		@Override
		public void run() {
			mHandler.post(new Runnable() {
				public void run() {
					new DownloadTweetTask().execute();	
				}
			});
		}
	}

	private class DownloadTweetTask extends AsyncTask<Void, Void, Void> {
		ProgressDialog progressDialog;
		
		@Override
		protected void onPreExecute() {
			// Set up progress spinning wheel
			progressDialog = ProgressDialog.show(TwitterFeedActivity.this, 
												"Updating Tweets", "Please Wait ...");
		}
		
		
		@Override
		protected Void doInBackground(Void... params){
			Log.d(TAG, "Updating tweets ...");
			twitter.getTimelineUpdates();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Log.d(TAG, "Notifying of dataset changes ...");
			progressDialog.dismiss();
			
			// Reload cursor data set
			Cursor c = mCursorAdapter.getCursor();
			
			// This is deprecated. I want to use requeryOnBackgroundThread(...) to
			// update my dataset, but I don't understand how to use it to refresh the 
			// list view.
			c.requery();
			
			// Reanimate the list on update
			ListView list = (ListView) findViewById(android.R.id.list);
			list.startLayoutAnimation();
		}
	}
}