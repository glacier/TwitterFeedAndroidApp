package ca.xtreme.xlbootcamp.twitter.app;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ca.xtreme.xlbootcamp.twitter.api.TwitterClient;
import ca.xtreme.xlbootcamp.twitter.api.TwitterClientException;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class TweetsHashtagUpdateManager {
	
	private ContentResolver mResolver;
	private String TAG = "TweetsHashtagUpdateManager";
	private TwitterClient mTwitter;
	private Timer mTimer;
	private String mHashtag;
	
	private ExecutorService mExecutorService = Executors.newFixedThreadPool(5);
	
	private TweetsHashtagUpdateListener mTweetsHashtagUpdateListener;
	
	public TweetsHashtagUpdateManager(ContentResolver resolver, String hashtag, TweetsHashtagUpdateListener tweetsHashtagUpdateListener) {
		mTwitter = new TwitterClient();
		mResolver = resolver;
		mHashtag = hashtag;
		mTweetsHashtagUpdateListener = tweetsHashtagUpdateListener;
	}
	
	public void updateNow() throws TwitterClientException {
		storeTweets(mTwitter.retrieveRecentTweetsByHashtag(mHashtag));
	}
	
	/*
	 * Inserts a list of tweet objects into the content provider. 
	 * Only new tweets (ie. a tweet that does not have the same username and timestamp as an
	 * existing tweet in content provider) is inserted
	 * @param tweetList
	 */
	private void storeTweets(ArrayList<ContentValues> tweetList) {
		Cursor cursor = null;
		
		// For each tweet downloaded from Twitter, store in content provider if 
		// tweet does not already exist in provider.
		// Because Twitter API search return the last N most recent tweets for a given hashtag,
		// we may end up saving lots of duplicates if we don't check for their uniqueness.
		// Uniqueness of a tweet is determined by the tuple (username, timestamp).
		for (ContentValues aTweetValue : tweetList) {
			String username = (String) aTweetValue.get(Twitter.Tweets.USERNAME);
			String timestamp = (String) aTweetValue.get(Twitter.Tweets.CREATED_DATE);
			
			String selection = "username=? and timestamp=?";
			String [] selectionArgs = new String[2];
			selectionArgs[0] = username;
			selectionArgs[1] = timestamp;
			
			// Note: it is probably better to store the id_str as well so that one can easily generate a URL to link the tweet to 
			// its corresponding resource on Twitter.com.  The Twitter API does not support username/timestamp-based urls.
			cursor = mResolver.query(Twitter.Tweets.CONTENT_URI, null, selection, selectionArgs, null);
			
			if(cursor.getCount() == 0) {
				Log.d(TAG , "Inserting tweet into database ...");
				// Create a new row in db with an uri of 
				// content://#{Twitter.Tweets.CONTENT_URI}/tweets/<id_value>
				mResolver.insert(Twitter.Tweets.CONTENT_URI, aTweetValue);
			}
		}
		
		if(cursor != null) {
			cursor.close();
		}
	}

	/*
	 * Launches AsyncTasks for updating the UI periodically.
	 */
	private class TweetTimerTask extends TimerTask {
		@Override
		public void run() {
			//mExecutorService.submit(new DownloadTweetTask());
			new DownloadTweetTask().run();
		}
	}

	/*
	 * Launches AsyncTasks for downloading from Twitter and stores new 
	 * data into the content provider. When async task is done, notifies the 
	 * Listview of updates to the content provider.
	 */

	private class DownloadTweetTask implements Runnable {
		@Override
		public void run() {
			mTweetsHashtagUpdateListener.onUpdateStarted();

			try {
				// Grab the tweets from Twitter.com 
				// and store in our tweets datastore
				updateNow();

				Log.d(TAG, "TweetsDatabaseProvider was updated ... updating listview");
				
				mResolver.notifyChange(Uri.withAppendedPath(Twitter.Tweets.CONTENT_URI, "#" + mHashtag), null);
			
				mTweetsHashtagUpdateListener.onUpdateSucceeded();
			} catch (TwitterClientException e) {
				Log.d(TAG, "Failed to update Twitter.");
				
				mTweetsHashtagUpdateListener.onUpdateFailed();
			}
		}
	}

	public void startBackgroundUpdates() {
		if (mExecutorService != null) {
			throw new IllegalStateException("Background updates already started");
		}
		
		mExecutorService = Executors.newFixedThreadPool(5);
		
		if(mTimer == null) {
			mTimer = new Timer();
			mTimer.scheduleAtFixedRate(new TweetTimerTask(), 0, 30000);
		} else {
			Log.d(TAG, "A timer instance is not created because it already exists.");
		}
	}

	public void stopBackgroundUpdates() {
		if(mTimer != null) {
			Log.d(TAG, "timer was cancelled and purged.");
			mTimer.cancel();
			mTimer.purge();
		}
		mTimer = null;
		mExecutorService.shutdownNow();
		mExecutorService=null;
	}
	
}
