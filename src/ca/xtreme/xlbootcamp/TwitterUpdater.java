package ca.xtreme.xlbootcamp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.util.Log;
import ca.xtreme.xlbootcamp.Twitter.Tweets;


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

	public static final String TWITTER_API_URL = "http://search.twitter.com/search.json?q=";

	private ContentResolver mResolver;
	private static Context mCtx;
	private static File mCacheDir;
	private String mSearchURI;
	private String mSearchString;

	public TwitterUpdater(Context ctx, String searchString) {
		mCtx = ctx;
		mResolver = ctx.getContentResolver();
		mCacheDir = mCtx.getCacheDir();
		mSearchString = searchString;
		mSearchURI = TWITTER_API_URL + "#" + searchString;
	}

	public Cursor getTimelineUpdates() {
		String jsonString = downloadTweetsAsJSON();
		ArrayList<ContentValues> tweetList = parseTwitterJSON(jsonString);
		ArrayList<ContentValues> newTweets = storeTweetsInContentProvider(tweetList);
		
		// Retrieve the tweets with hashtag mSearchString from the database
		Uri uri = Uri.withAppendedPath(Twitter.Tweets.CONTENT_URI, mSearchString);

		// For each tweet retrieve from the database, download the corresponding
		// profile image if its not already in the disk cache
		// This ensures that the profile image is in the cache when it is being 
		// displayed
		// TODO Implement an explicit cache clearing mechanism instead of
		// relying on the phone's default cache clearing policy.
		Cursor cursor = mResolver.query(uri, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			String userId = cursor.getString(cursor.getColumnIndex(Tweets.USER_ID));
			String pictureUrl = cursor.getString(cursor.getColumnIndex(Tweets.PROFILE_IMAGE_URL));

			downloadImage(pictureUrl, getCachedPhotoPath(userId));
			cursor.moveToNext();
		}
		
		// Returns the cursor over all the rows in the db
		// where hashtag = mSearchString
		cursor.moveToFirst();
		
		return cursor;
	}

	// Returns the profile image for a given userId
	public Bitmap getProfileImage(String userId) {
		String cachedImageAbsPath = getCachedPhotoPath(userId);
		
		return BitmapFactory.decodeFile(cachedImageAbsPath);
	}

	private String getCachedPhotoPath(String twitterUserId) {
		String cachedFilename = mCacheDir.getPath() + "/" + twitterUserId + ".jpg";
		
		return cachedFilename;
	}

	// Gets and parses the latest tweets from twitter
	private String downloadTweetsAsJSON() {
		AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android");
		final HttpResponse response;
		final HttpGet get = new HttpGet(mSearchURI);
		
		try {
			response = httpClient.execute(get);
			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_OK) {
				// TODO fix this log message
				Log.w("ImageDownloader", "Error " + statusCode +
						" while retrieving bitmap from " + mSearchURI);
			}

			// read and serialize JSON data into a string
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			StringBuilder builder = new StringBuilder();
			for (String line = null; (line = reader.readLine()) != null;) {
				builder.append(line).append("\n");
			}
			
			// parse and extract JSON data
			return builder.toString();
		} catch (final IOException e) {
			Log.d(TAG, "IOException: Message: " + e.getMessage());
			// show error to user instead of logging, which is bad.
		} finally {
			if(httpClient != null) {
				httpClient.close();
			}
		}
		
		return null;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return tweetList;
	}
	
	private ArrayList<ContentValues> storeTweetsInContentProvider(ArrayList<ContentValues> tweetList) {
		ArrayList<ContentValues> newTweets = new ArrayList<ContentValues>();
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
				newTweets.add(aTweetValue);
			}
		}

		if(cursor != null) {
			cursor.close();
		}
		
		return newTweets;
	}
}