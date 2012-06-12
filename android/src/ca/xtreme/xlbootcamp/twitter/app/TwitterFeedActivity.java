package ca.xtreme.xlbootcamp.twitter.app;

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
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;
import ca.xtreme.xlbootcamp.twitter.R;


public class TwitterFeedActivity extends ListActivity {

	public static final String TAG = "TwitterFeedActivity";
	private static final int DIALOG_NOT_CONNECTED_ID = 0;
	private static final int DIALOG_TWITTER_FAILED_ID = 1;
	
	private TwitterCursorAdapter mCursorAdapter;
	private String mSearchString = "bieber";
	private boolean mConnected = true;
	private Cursor mCursor;
	private TweetsHashtagUpdateManager tweetManager;
	
	private class ActivityTweetsHashtagUpdateListener implements TweetsHashtagUpdateListener {

		private ProgressDialog progressDialog;

		@Override
		public void onUpdateStarted() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					progressDialog = ProgressDialog.show(TwitterFeedActivity.this, "Updating Tweets", "Please Wait ...");
				}
			});
		}
		
		@Override
		public void onUpdateFailed() {
			// Alert the user that twitter didn't work
			runOnUiThread(new Runnable() {
				public void run() {
					if(progressDialog != null) {
						progressDialog.dismiss();
					}
					
					showDialog(DIALOG_TWITTER_FAILED_ID);
				}
			});
		}

		@Override
		public void onUpdateSucceeded() {
			runOnUiThread(new Runnable() {
				public void run() {
					if(progressDialog != null) {
						progressDialog.dismiss();
					}

					// Reanimate the list on update
					ListView list = (ListView) findViewById(android.R.id.list);
					list.startLayoutAnimation();
				}
			});
		}
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		if(isConnectedToNetwork()) {
			// Initialize tweet manager which provides access to Twitter
			tweetManager = new TweetsHashtagUpdateManager(this.getContentResolver(), mSearchString, new ActivityTweetsHashtagUpdateListener());
			
			// Initialize and set up activity list view
			setupListView(mSearchString);
			setupListAnimation();
		} else {
			showDialog(DIALOG_NOT_CONNECTED_ID);
		}
	}
	
	protected Dialog onCreateDialog(int id, Bundle args) {
		Dialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		switch(id) {
		case DIALOG_NOT_CONNECTED_ID:
			//define dialog
			builder.setMessage("No internet connectivity found.")
			       .setCancelable(false)
			       .setNegativeButton("OK", new DialogInterface.OnClickListener() {
			    	   public void onClick(DialogInterface dialog, int id) {
			    		   dialog.cancel();
			    	   }
			       });
			dialog = builder.create();
			break;
		case DIALOG_TWITTER_FAILED_ID:
			builder.setMessage("Failed to update Twitter.")
					.setCancelable(true)
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

	
	/*
	 * Handles Activity Lifecycle
	 * 
	 */
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		tweetManager.stopBackgroundUpdates();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(mConnected) {
			tweetManager.startBackgroundUpdates();
		}
	}
	
	/*
	 * Create menu item
	 * 
	 */
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


	/*
	 * Handle the result of HashtagEditActivity launched via the option menu
	 * 
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(data != null) {
			Bundle extras = data.getExtras();
			mSearchString = extras.getString(Twitter.HASHTAG_INTENT);

			if (mSearchString != null && mSearchString.length() > 0) {
				Log.d(TAG, "Displaying tweets with hashtag " + mSearchString);
				setupListView(mSearchString);
			}
		} else {
			Log.d(TAG, "Intent data was null. requestCode=" 
				 + requestCode + " resultCode=" + resultCode);
		}
	}

	
	/*
	 * Helper methods
	 */
	
	/*
	 * Helper method for determining the state of the phone's network connectivity.
	 */
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
	
	/*
	 * Binds the content provider to a custom list adapter and binds the list adapter to the ListView.
	 */
	private void setupListView(String searchString) {
		// Retrieve the tweets with hashtag mSearchString from the database
		Uri uri = Uri.withAppendedPath(Twitter.Tweets.CONTENT_URI, "#" + searchString);
		mCursor = managedQuery(uri, null, null, null, null);
		
		// Tell the cursor what uri to watch, so it knows when its source data changes
		mCursor.setNotificationUri(getContentResolver(), uri);
		
		// Initialize a custom CursorAdapter and populate the list view
		// using data from the content provider returned via the cursor
		mCursorAdapter = new TwitterCursorAdapter(TwitterFeedActivity.this, R.layout.list_item, mCursor);
		ListView list = (ListView) findViewById(android.R.id.list);
		list.setAdapter(mCursorAdapter);
	}
	
	/*
	 * Creates and sets a "cascading list" animation programmatically.  
	 * A better way might be to encode this as an animation resource.
	 */
	private void setupListAnimation() {
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
	}

}