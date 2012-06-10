package ca.xtreme.xlbootcamp;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ca.xtreme.xlbootcamp.twitter.Twitter;
import ca.xtreme.xlbootcamp.twitter.TwitterClient;
import ca.xtreme.xlbootcamp.twitter.TwitterClientException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
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


public class TwitterFeedActivity extends ListActivity {

	public static final String TAG = "TwitterFeedActivity";
	private static final int DIALOG_NOT_CONNECTED_ID = 0;
	private static final int DIALOG_TWITTER_FAILED_ID = 1;
	
	private TwitterClient twitter;
	private Timer mTimer = null;
	private TwitterCursorAdapter mCursorAdapter;
	private String mSearchString = "bieber";
	private boolean mConnected = true;
	private Cursor mCursor;
	private TweetsManager tweetManager;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate() called");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		tweetManager = new TweetsManager(this.getContentResolver());
		
		if(isConnectedToNetwork()) {
			// Initialize client which provides access to Twitter
			twitter = new TwitterClient(this, mSearchString);
			
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
		stopTimerTask();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		stopTimerTask();
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume() called");
		
		super.onResume();
		
		if(mConnected) {
			startTimerTask();
		}
	}
	
	private void startTimerTask() {
		// Reschedule the timer task
		if(mTimer == null) {
			mTimer = new Timer();
			mTimer.scheduleAtFixedRate(new TweetTimerTask(), 0, 30000);
		} else {
			Log.d(TAG, "A timer instance is not created because it already exists.");
		}
	}
	
	private void stopTimerTask() {
		if(mTimer != null) {
			Log.d(TAG, "timer was cancelled and purged.");
			mTimer.cancel();
			mTimer.purge();
		}
		mTimer = null;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopTimerTask();
		finish();
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
	 * Handle the result of Activity launched via intents
	 * 
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(data != null) {
			Bundle extras = data.getExtras();
			mSearchString = extras.getString(Twitter.HASHTAG_INTENT);

			if (mSearchString != null && mSearchString.length() > 0) {
				Log.d(TAG, "Display tweets with hashtag " + mSearchString);
				twitter = new TwitterClient(this, mSearchString);
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

	
	/*
	 * Launches AsyncTasks for updating the UI periodically.
	 */
	private class TweetTimerTask extends TimerTask {
		@Override
		public void run() {
			if(isFinishing()) {
				this.cancel();
				return;
			}
			
			runOnUiThread(new Runnable() {
				public void run() {
					Log.d(TAG, "TimerTask running ...");
					new DownloadTweetTask().execute();
				}
			});
		}
	}

	/*
	 * Launches AsyncTasks for downloading from Twitter and stores new 
	 * data into the content provider. When async task is done, notifies the 
	 * Listview of updates to the content provider.
	 */
	private class DownloadTweetTask extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progressDialog;
		
		@Override
		protected void onPreExecute() {
			if(isCancelled() || isFinishing()) {
				this.cancel(true);
				return;
			}
			
			// Set up progress spinning wheel
			progressDialog = ProgressDialog.show(TwitterFeedActivity.this, 
												"Updating Tweets", "Please Wait ...");
		}
		
		@Override
		protected Void doInBackground(Void... params){
			if(isCancelled() || isFinishing()) {
				this.cancel(true);
				return null;
			}

			try {
				// Grab the tweets from Twitter.com 
				// and store in our tweets datastore
				ArrayList<ContentValues> newTweets;
				newTweets = twitter.retrieveTwitterUpdates();
				tweetManager.storeTweets(newTweets);
			} catch (TwitterClientException e) {
				Log.d(TAG, "Failed to update Twitter.");
				
				// Alert the user that twitter didn't work
				runOnUiThread(new Runnable() {
					public void run() {
						showDialog(DIALOG_TWITTER_FAILED_ID);
					}
				});
				
				// Cancel the execution of this thread
				this.cancel(true);
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(isCancelled() || isFinishing()) {
				this.cancel(true);
				return;
			}
			
			if(progressDialog != null) {
				progressDialog.dismiss();
			}
			
			getContentResolver().notifyChange(
					Uri.withAppendedPath(Twitter.Tweets.CONTENT_URI, "#" + mSearchString), null);
			
			// Reanimate the list on update
			ListView list = (ListView) findViewById(android.R.id.list);
			list.startLayoutAnimation();
		}
	}
}