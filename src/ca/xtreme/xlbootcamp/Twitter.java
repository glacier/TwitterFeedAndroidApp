package ca.xtreme.xlbootcamp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.http.AndroidHttpClient;
import android.util.Log;
import android.content.Context;

public class Twitter {
    private static final String TAG = "Twitter";
    private static final AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android");
	public static final String[] FROM = {TweetsDbAdapter.KEY_USERNAME, TweetsDbAdapter.KEY_MESSAGE, 
										TweetsDbAdapter.KEY_TIMESTAMP};
	public static final int[] TO = {R.id.username, R.id.tweet_content, R.id.timestamp};
	
	HashMap<String,SoftReference<Bitmap>> imageCache =
	        new HashMap<String,SoftReference<Bitmap>>();
	
	private TweetsDbAdapter mDbHelper;
	private static String mSearchURI;
	
	public Twitter(Context ctx) {
		//empty default constructor
		mDbHelper = new TweetsDbAdapter(ctx);
	    mDbHelper.open();
	    
		mSearchURI = "http://search.twitter.com/search.json?q=%23bieber&rpp=1";
	}
	
	public Twitter(String uri) {
		mSearchURI = uri;
	}
	
	// downloads new status updates and returns a cursor into the db for all tweets
	public ArrayList<Tweet> getTimelineUpdates() {
		downloadAndStoreTweets();
		ArrayList<Tweet> tweetsList = new ArrayList<Tweet>();
		Cursor tweetsCursor = mDbHelper.fetchAllTweets();
		
		// iterate through each result row and convert into a list of tweet objects
		while(tweetsCursor.moveToNext()) {
			String username = tweetsCursor.getString(1);
			String message = tweetsCursor.getString(2);
			String timestamp = tweetsCursor.getString(3);
			String pictureUrl = tweetsCursor.getString(4);
			
			Bitmap image = null;
			
			if (imageCache.get(username+timestamp) == null) {
				//re-download the image
				image = BitmapDownloader.downloadBitmap(pictureUrl);
			}
			
			tweetsList.add(new Tweet(username, message, timestamp, pictureUrl, image));
		}
		
		return tweetsList;
	}
	
	
	// Gets and parses the latest tweets given a uri Twitter search API
	private void downloadAndStoreTweets() {
		ArrayList<Tweet> tweetMessageList = new ArrayList<Tweet>();
		final HttpResponse response;
		
		Log.d(TAG, "search: " + mSearchURI);
		
		final HttpGet get = new HttpGet(mSearchURI);

		try {
			response = httpClient.execute(get);

			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_OK) {
				Log.w("ImageDownloader", "Error " + statusCode +
						" while retrieving bitmap from " + mSearchURI);
				return;
			}
			
			//read and serialize JSON data into a string
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			StringBuilder builder = new StringBuilder();

			for (String line = null; (line = reader.readLine()) != null;) {
				builder.append(line).append("\n");
			}
			
			Log.d(TAG, builder.toString());
			
			//parse and extract JSON data
			JSONTokener tokener = new JSONTokener(builder.toString());
			try {
				JSONObject jsonObj = new JSONObject(tokener);
				JSONArray results = jsonObj.getJSONArray("results");

				for(int i=0; i<results.length(); i++) {
					JSONObject aTweet = results.getJSONObject(i);
					String username = aTweet.getString("from_user_name");
					String message = aTweet.getString("text");
					String timestamp = aTweet.getString("created_at");
					String pictureUrl = aTweet.getString("profile_image_url");
					
					
					//check if tweet is already in the database
					Cursor cursor = mDbHelper.fetchTweet(username, timestamp);
					if(cursor.getCount() == 0) {
						Log.d(TAG, "Storing tweet from " + username + " posted at " + timestamp + " into database");
						//insert the tweet into the database
						mDbHelper.storeTweet(username, message, timestamp, pictureUrl);
						
						//download the image because it has never been downloaded before
						Bitmap image = BitmapDownloader.downloadBitmap(pictureUrl);
						
						//TODO cache the downloaded image
						imageCache.put(username+timestamp, new SoftReference<Bitmap>(image));
					}

				}
			} catch (JSONException e) {
				Log.e(TAG, "JSONException problem creating JSONObject from tokener:", e);
			}
		} catch (final IOException e) {
			Log.e(TAG, "IOException", e);
			Log.d(TAG, "IOException: Message: " + e.getMessage());
		} finally {
			if(httpClient != null) {
				httpClient.close();
			}
		}

		Log.d(TAG, "number of tweets saved = " + tweetMessageList.size());
	}
}