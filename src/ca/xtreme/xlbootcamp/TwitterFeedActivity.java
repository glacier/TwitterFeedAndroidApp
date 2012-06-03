package ca.xtreme.xlbootcamp;

import java.util.Timer;
import java.util.TimerTask;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;


public class TwitterFeedActivity extends ListActivity implements OnClickListener {

	public static final String TAG = "TwitterFeedActivity";
	private TwitterClient twitter;
	private Handler mHandler = new Handler();
	private Timer mTimer;
	private TwitterCursorAdapter mCursorAdapter;
	private String mSearchString = "bieber";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Button button = (Button) findViewById(R.id.button);
		button.setOnClickListener(this);

		// Populate the initial list view with tweets
		twitter = new TwitterClient(this, mSearchString);
		setupListView(mSearchString);
		new DownloadTweetTask().execute();
		
		// Check for new tweets every 30 seconds
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TweetTimerTask(), 30000, 30000);
	}

	public void onClick(View v) {
		mTimer.cancel();
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
		Bundle extras = data.getExtras();
		String searchString = extras.getString("ca.xtreme.xlbootcamp.Hashtag");
		
		if (searchString == null) {
			searchString = "bieber";
		}
		mSearchString = searchString;
		twitter = new TwitterClient(this, searchString);
		setupListView(searchString);
		new DownloadTweetTask().execute();
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
			progressDialog = ProgressDialog.show(TwitterFeedActivity.this, "", "Loading ...");
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