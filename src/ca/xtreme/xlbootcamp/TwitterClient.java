package ca.xtreme.xlbootcamp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.http.AndroidHttpClient;
import android.util.Log;


public class TwitterClient {
	private static final String TAG = "TwitterUpdater";

	// Defines a list of columns to retrieve from the Cursor
	public static final String[] FROM =
		{
		Twitter.Tweets.USER_ID,
		Twitter.Tweets.USERNAME,   
		Twitter.Tweets.MESSAGE,
		Twitter.Tweets.CREATED_DATE,
		Twitter.Tweets.PROFILE_IMAGE_URL,
		Twitter.Tweets.HASHTAG
		};

	// Defines a list of View IDs that corresponds to Cursor columns
	public static final int[] TO = {R.id.username, R.id.tweet_content, R.id.timestamp, R.id.profile_pic };

	public static final String TWITTER_API_URL = "http://search.twitter.com/search.json?q=";

	private ContentResolver mResolver;
	private String mSearchURI;
	private String mSearchString;
	
	public TwitterClient(Context ctx, String searchString) {
		mResolver = ctx.getContentResolver();
		mSearchString = searchString;
		mSearchURI = TWITTER_API_URL + "#" + searchString;
	}

	public void getTimelineUpdates() {
		String jsonString = downloadTweetsAsJSON();
		ArrayList<ContentValues> tweetList = parseTwitterJSON(jsonString);
		storeTweetsInContentProvider(tweetList);
	}
	
	// Gets and parses the latest tweets from twitter
	private String downloadTweetsAsJSON() {
		Log.d(TAG, "running downloadTweetsAsJSON");
		
		AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android");
		final HttpResponse response;
		final HttpGet get = new HttpGet(mSearchURI);
		
		try {
			response = httpClient.execute(get);
			Log.d(TAG, response.toString());
			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_OK) {
				Log.w(TAG, "Error " + statusCode +
						" while retrieving tweets from " + mSearchURI);
			}

			// read and serialize JSON data into a string
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			StringBuilder builder = new StringBuilder();
			for (String line = null; (line = reader.readLine()) != null;) {
				builder.append(line).append("\n");
			}
			Log.d(TAG, builder.toString());
			
			// parse and extract JSON data
			return builder.toString();
		} catch (final IOException e) {
			Log.d(TAG, "IOException: Message: " + e.getMessage());
			Log.d(TAG, "IOException: " + e.toString());
		} finally {
			if(httpClient != null) {
				httpClient.close();
			}
		}
		
		return null;
	}
	
	private ArrayList<ContentValues> parseTwitterJSON(String jsonString) {
		JSONTokener tokener = new JSONTokener(jsonString);
		
		ArrayList<ContentValues> tweetList = new ArrayList<ContentValues>();
		
		try {
			JSONObject jsonObj = new JSONObject(tokener);
			
			JSONArray results = jsonObj.getJSONArray("results");
			
			for(int i=0; i<results.length(); i++) {
				JSONObject aTweet = results.getJSONObject(i);
				
				ContentValues tweetValue = new ContentValues();
				tweetValue.put(Twitter.Tweets.USER_ID, aTweet.getString("from_user_id_str"));
				tweetValue.put(Twitter.Tweets.USERNAME, aTweet.getString("from_user_name"));
				tweetValue.put(Twitter.Tweets.MESSAGE, aTweet.getString("text"));
				tweetValue.put(Twitter.Tweets.PROFILE_IMAGE_URL, aTweet.getString("profile_image_url"));
				tweetValue.put(Twitter.Tweets.HASHTAG, mSearchString);
				
				// Parse the date string into a SQLite date string
				// so that we can properly sort tweets by time
				String timestamp = aTweet.getString("created_at");
				SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
				long unixTimeSeconds = df.parse(timestamp).getTime() / 1000;
				
				tweetValue.put(Twitter.Tweets.UNIX_TIME, unixTimeSeconds);
				tweetValue.put(Twitter.Tweets.CREATED_DATE, timestamp);
				
				tweetList.add(tweetValue);
			}
		} catch (JSONException e) {
			Log.e(TAG, "JSONException problem creating JSONObject from tokener:", e);
		} catch (ParseException e) {
			Log.e(TAG, "Could not parse date string:", e);
		}
		
		return tweetList;
	}
	
	private void storeTweetsInContentProvider(ArrayList<ContentValues> tweetList) {
		Cursor cursor = null;
		
		for (ContentValues aTweetValue : tweetList) {
			String username = (String) aTweetValue.get(Twitter.Tweets.USERNAME);
			String timestamp = (String) aTweetValue.get(Twitter.Tweets.CREATED_DATE);
			
			String selection = "username=? and timestamp=?";
			String [] selectionArgs = new String[2];
			selectionArgs[0] = username;
			selectionArgs[1] = timestamp;
			
			// Store new feed updates
			cursor = mResolver.query(Twitter.Tweets.CONTENT_URI, null, selection, selectionArgs, null);
			if(cursor.getCount() == 0) {
				// Create a new row in db with an uri of 
				// content://#{Twitter.Tweets.CONTENT_URI}/tweets/<id_value>
				mResolver.insert(Twitter.Tweets.CONTENT_URI, aTweetValue);
			}
		}
		
		if(cursor != null) {
			cursor.close();
		}
	}
}