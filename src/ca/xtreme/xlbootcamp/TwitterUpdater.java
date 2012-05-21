package ca.xtreme.xlbootcamp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import ca.xtreme.xlbootcamp.Twitter.Tweets;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.util.Log;

// FIXME Improve the image caching.  Implement a cache clearing mechanism instead of
// relying on the phone's cache clearing policy.
public class TwitterUpdater {
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

	public static final String DEFAULT_URL = "http://search.twitter.com/search.json?q=";
	
	private ContentResolver mResolver;
	private static Context mCtx;
	private static File mCacheDir;
	private String mSearchURI;

	private String mSearchString;
	
	public TwitterUpdater(Context ctx) {
		mCtx = ctx;
		mResolver = ctx.getContentResolver();
		mCacheDir = mCtx.getCacheDir();
		mSearchURI = DEFAULT_URL + "#bieber";
	}
	
	public TwitterUpdater(Context ctx, String searchString) throws UnsupportedEncodingException {
		mCtx = ctx;
		mResolver = ctx.getContentResolver();
		mCacheDir = mCtx.getCacheDir();
		mSearchString = searchString;
		mSearchURI = DEFAULT_URL + "#" + searchString;
	}
	
	public Cursor getTimelineUpdates() {
		downloadTweets();
		
		// Retrieve the tweets with hashtag mSearchString from the database
		Uri uri = Uri.withAppendedPath(Twitter.Tweets.CONTENT_URI, mSearchString);
		
		// For each tweet retrieve from the database, download the corresponding
		// profile image if its not already in the disk cache
		// This ensures that the profile image is in the cache when it is being 
		// displayed
		Cursor cursor = mResolver.query(uri, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			String userId = cursor.getString(cursor.getColumnIndex(Tweets.USER_ID));
			String pictureUrl = cursor.getString(cursor.getColumnIndex(Tweets.PROFILE_IMAGE_URL));
			String cachedFilename = mCacheDir.getPath() + "/" + userId + ".jpg";
			downloadImage(pictureUrl, cachedFilename);
			cursor.moveToNext();
		}
		
		cursor.moveToFirst();
		return cursor;
	}
	
	// Returns the profile image for a given userId
	public static Bitmap getProfileImage(String userId) {
		String cachedImageAbsPath = mCacheDir.getAbsolutePath() + "/" + userId + ".jpg";
		return BitmapFactory.decodeFile(cachedImageAbsPath);
	}
	
	// Gets and parses the latest tweets from twitter
	private void downloadTweets() {
		AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android");
		final HttpResponse response;
		final HttpGet get = new HttpGet(mSearchURI);
		Cursor cursor = null;
		
		try {
			response = httpClient.execute(get);
			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_OK) {
				Log.w("ImageDownloader", "Error " + statusCode +
						" while retrieving bitmap from " + mSearchURI);
				return;
			}
			
			// read and serialize JSON data into a string
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			StringBuilder builder = new StringBuilder();

			for (String line = null; (line = reader.readLine()) != null;) {
				builder.append(line).append("\n");
			}
			
			// parse and extract JSON data
			JSONTokener tokener = new JSONTokener(builder.toString());
			try {
				JSONObject jsonObj = new JSONObject(tokener);
				JSONArray results = jsonObj.getJSONArray("results");

				for(int i=0; i<results.length(); i++) {
					JSONObject aTweet = results.getJSONObject(i);
					String userId = aTweet.getString("from_user_id_str");
					String username = aTweet.getString("from_user_name");
					String message = aTweet.getString("text");
					String timestamp = aTweet.getString("created_at");
					String pictureUrl = aTweet.getString("profile_image_url");
					
					String selection = "username=? and timestamp=?"; //equivalent to SQL where statement
					String [] selectionArgs = new String[2];
					selectionArgs[0] = username;
					selectionArgs[1] = timestamp;
					
					// check if tweet is already in the database
					cursor = mResolver.query(Twitter.Tweets.CONTENT_URI, null, selection, selectionArgs, null);

					if(cursor.getCount() == 0) {
						Log.d(TAG, "Storing tweet from " + username + " posted at " + timestamp + " into database");
						
						// insert the tweet into the database
						ContentValues tweetValue = new ContentValues();
						tweetValue.put(Twitter.Tweets.USER_ID, userId);
						tweetValue.put(Twitter.Tweets.USERNAME, username);
						tweetValue.put(Twitter.Tweets.MESSAGE, message);
						tweetValue.put(Twitter.Tweets.CREATED_DATE, timestamp);
						tweetValue.put(Twitter.Tweets.PROFILE_IMAGE_URL, pictureUrl);
						tweetValue.put(Twitter.Tweets.HASHTAG, mSearchString);

						// creates a new row with uri content://#{Twitter.Tweets.CONTENT_URI}/tweets/<id_value>
						mResolver.insert(Twitter.Tweets.CONTENT_URI, tweetValue);
						Log.d("TwitterUpdater", "Stored tweet with hashtag " + mSearchString + " from " + userId + ": " 
								+ username + " posted at " + timestamp);
					}
					cursor.close();
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
			
			if(cursor != null) {
				cursor.close();
			}
		}
	}
	
	private static boolean downloadImage(String imageUrl, String diskFilename) {
		File file = new File(diskFilename);
		if(!file.exists()) {
			return forceDownloadImage(imageUrl, file);
		}
		return false;
	}
	
	private static boolean forceDownloadImage(String imageUrl, File file) {
		Bitmap image = BitmapDownloader.downloadBitmap(imageUrl);
		
		if(image == null) {
			return false;
		}
		
		try {
			FileOutputStream fout = new FileOutputStream(file);
			image.compress(Bitmap.CompressFormat.JPEG, 100, fout);
			fout.flush();
			fout.close();
		} catch (FileNotFoundException e) {
			Log.d("TwitterUpdater", "Could not open " + file.getAbsoluteFile());
		} catch (IOException e) {
			Log.d("TwitterUpdater", "Could not compress bitmap");
		}
		return true;
	}
}