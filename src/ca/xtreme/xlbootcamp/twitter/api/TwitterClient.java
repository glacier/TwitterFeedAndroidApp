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


	public ArrayList<ContentValues> retrieveRecentTweetsByHashtag(String hashtag) 
			throws TwitterClientException {
		String jsonString = downloadTweetsAsJSON(hashtag);
		
		if(jsonString == null) {
			throw new TwitterClientException("Could not download latest tweets from Twitter");
		}

		return parseTwitterJSON(jsonString, hashtag);
	}
	
	/**
	 * Downloads the latest tweets from Twitter search API as JSON
	 * @return a serialized JSON string.  The string is null if API call to Twitter 
	 * failed.
	 * @throws TwitterClientException 
	 */

	private String downloadTweetsAsJSON(String hashtag) throws TwitterClientException {
		Log.d(TAG, "running downloadTweetsAsJSON");
		
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
			throw new TwitterClientException("There was a problem downloading the tweets", e);
		} finally {
			if(httpClient != null) {
				httpClient.close();
			}
		}
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
}