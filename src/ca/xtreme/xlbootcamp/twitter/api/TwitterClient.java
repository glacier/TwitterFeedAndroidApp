package ca.xtreme.xlbootcamp.twitter.api;

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

import ca.xtreme.xlbootcamp.twitter.app.Twitter;

import android.content.ContentValues;
import android.net.http.AndroidHttpClient;
import android.util.Log;


public class TwitterClient {
	private static final String TAG = "TwitterClient";

	public static final String TWITTER_API_URL = "http://search.twitter.com/search.json?q=";

<<<<<<< HEAD:src/ca/xtreme/xlbootcamp/TwitterClient.java
	private String mSearchURI;
	private String mSearchString;
	
	public TwitterClient(Context ctx, String searchString) {
		mSearchString = searchString;
		mSearchURI = TWITTER_API_URL + "#" + searchString;
	}

<<<<<<< HEAD
	public boolean getTimelineUpdates() {
		String jsonString = downloadTweetsAsJSON();
		if(jsonString != null) {
			ArrayList<ContentValues> tweetList = parseTwitterJSON(jsonString);
			boolean updated = storeTweetsInContentProvider(tweetList);
			return updated;
		}
		return false;
=======
	public ArrayList<ContentValues> retrieveTwitterUpdates() throws TwitterClientException {
		String jsonString = downloadTweetsAsJSON();
=======
	public ArrayList<ContentValues> retrieveRecentTweetsByHashtag(String hashtag) 
			throws TwitterClientException {
		String jsonString = downloadTweetsAsJSON(hashtag);
>>>>>>> 4585577fe11bace63ce6b8f0608876308f227689:src/ca/xtreme/xlbootcamp/twitter/api/TwitterClient.java
		
		if(jsonString == null) {
			throw new TwitterClientException("Could not download latest tweets from Twitter");
		}

<<<<<<< HEAD:src/ca/xtreme/xlbootcamp/TwitterClient.java
		return parseTwitterJSON(jsonString);
>>>>>>> ae304f2b7283d9879b48adb332bf16bd5a701214
=======
		return parseTwitterJSON(jsonString, hashtag);
>>>>>>> 4585577fe11bace63ce6b8f0608876308f227689:src/ca/xtreme/xlbootcamp/twitter/api/TwitterClient.java
	}
	
	/**
	 * Downloads the latest tweets from Twitter search API as JSON
	 * @return a serialized JSON string.  The string is null if API call to Twitter 
	 * failed.
	 */
<<<<<<< HEAD:src/ca/xtreme/xlbootcamp/TwitterClient.java
	private String downloadTweetsAsJSON() {
		Log.d(TAG, "Retrieving tweets from search.twitter.com");
=======
	private String downloadTweetsAsJSON(String hashtag) {
		Log.d(TAG, "running downloadTweetsAsJSON");
>>>>>>> 4585577fe11bace63ce6b8f0608876308f227689:src/ca/xtreme/xlbootcamp/twitter/api/TwitterClient.java
		
		AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android");
		final HttpResponse response;
		final HttpGet get = new HttpGet(TWITTER_API_URL + "#" + hashtag);
		
		try {
			response = httpClient.execute(get);
			Log.d(TAG, response.toString());
			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_OK) {
				Log.w(TAG, "Error " + statusCode +
						" while retrieving tweets from " + TWITTER_API_URL + "#" + hashtag);
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
			Log.d(TAG, "HttpClient encountered a problem", e);
		} finally {
			if(httpClient != null) {
				httpClient.close();
			}
		}
		
		// null is returned when the Twitter API call has failed.
		// This could be due to problems with Twitter or loss of 
		// network connectivity on the phone.
		return null;
	}
	
	/**
	 * Parses a JSON string of tweets them into a list of ContentValue objects.
	 * @param jsonString - a non-null JSON string of latest tweets from Twitter. 
	 * @return list of tweets as ContentValues to be inserted into the content provider
	 */
	private ArrayList<ContentValues> parseTwitterJSON(String jsonString, String hashtag) {
		ArrayList<ContentValues> tweetList = new ArrayList<ContentValues>();
		
		JSONTokener tokener = new JSONTokener(jsonString);
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
				tweetValue.put(Twitter.Tweets.HASHTAG, hashtag);

				// Parse the date string into a Unix time in seconds since epoch.
				// We then sort this time in descending to display the latest 
				// tweets first.
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
<<<<<<< HEAD
	
	/*
	 * Inserts a list of tweet objects into the content provider. 
	 * Only new tweets (ie. a tweet that does not have the same username and timestamp as an
	 * existing tweet in content provider) is inserted
	 * @param tweetList
	 */
	private boolean storeTweetsInContentProvider(ArrayList<ContentValues> tweetList) {
		Cursor cursor = null;
		boolean newTweetsStored = false;
		
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
			
			cursor = mResolver.query(Twitter.Tweets.CONTENT_URI, null, selection, selectionArgs, null);
			if(cursor.getCount() == 0) {
				Log.d(TAG, "Inserting tweet into database ...");
				// Create a new row in db with an uri of 
				// content://#{Twitter.Tweets.CONTENT_URI}/tweets/<id_value>
				mResolver.insert(Twitter.Tweets.CONTENT_URI, aTweetValue);
				newTweetsStored = true;
			}
		}
		
		if(cursor != null) {
			cursor.close();
		}
		
		return newTweetsStored;
	}
=======
>>>>>>> ae304f2b7283d9879b48adb332bf16bd5a701214
}