package ca.xtreme.xlbootcamp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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
import android.net.http.AndroidHttpClient;
import android.util.Log;

public class TwitterUpdater {
    private static final String TAG = "TwitterUpdater";
    
    // Defines a list of columns to retrieve from the Cursor and load into an output row
	public static final String[] FROM =
		{
			Twitter.Tweets.USER_ID,
			Twitter.Tweets.USERNAME,   // Contract class constant containing the word column name
			Twitter.Tweets.MESSAGE, 	  // Contract class constant containing the locale column name
			Twitter.Tweets.CREATED_DATE,
			Twitter.Tweets.PROFILE_IMAGE_URL
		};

	// Defines a list of View IDs that will receive the Cursor columns for each row
	public static final int[] TO = {R.id.username, R.id.tweet_content, R.id.timestamp, R.id.profile_pic };
	
//	HashMap<String,SoftReference<Bitmap>> imageCache =
//	        new HashMap<String,SoftReference<Bitmap>>();
	
	private String mSearchURI;
	private ContentResolver mResolver;
	private static Context mCtx;
	private static File mCacheDir;
	
	public TwitterUpdater(Context ctx) {
		//empty default constructor
		mCtx = ctx;
		mResolver = ctx.getContentResolver();
		mCacheDir = mCtx.getCacheDir();
		mSearchURI = "http://search.twitter.com/search.json?q=%23bieber";
	}
	
	public TwitterUpdater(Context ctx, String uri) {
		mCtx = ctx;
		mResolver = ctx.getContentResolver();
		mCacheDir = mCtx.getCacheDir();
		mSearchURI = uri;
	}
	
	public Cursor getTimelineUpdates() {
		downloadTweets();
		return mResolver.query(Twitter.Tweets.CONTENT_URI, null, null, null, null);
	}
	
	public static Bitmap getProfileImage(String imageUrl, String userId) {
		String cachedImageAbsPath = mCacheDir.getAbsolutePath() + "/" + userId + ".jpg";
		Log.d("TwitterUpdate", "cached profile image path " + cachedImageAbsPath);
		downloadImage(imageUrl, cachedImageAbsPath);
		
		return BitmapFactory.decodeFile(cachedImageAbsPath);
	}
	
	// Gets and parses the latest tweets given a uri TwitterUpdater search API
	private void downloadTweets() {
		AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android");
		final HttpResponse response;
		
		Log.d(TAG, "search: " + mSearchURI);
		
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
			
			Log.d(TAG, builder.toString());
			
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
					
					String cachedFilename = mCacheDir.getPath() + "/" + userId + ".jpg";
					
					boolean downloaded = downloadImage(pictureUrl, cachedFilename);
					
					if(cursor.getCount() == 0) {
						Log.d(TAG, "Storing tweet from " + username + " posted at " + timestamp + " into database");
						
						// insert the tweet into the database
						ContentValues tweetValue = new ContentValues();
						tweetValue.put(Twitter.Tweets.USER_ID, userId);
						tweetValue.put(Twitter.Tweets.USERNAME, username);
						tweetValue.put(Twitter.Tweets.MESSAGE, message);
						tweetValue.put(Twitter.Tweets.CREATED_DATE, timestamp);
						tweetValue.put(Twitter.Tweets.PROFILE_IMAGE_URL, pictureUrl);

						// creates a new row with uri content://#{Twitter.Tweets.CONTENT_URI}/tweets/<id_value>
						mResolver.insert(Twitter.Tweets.CONTENT_URI, tweetValue);
						Log.d("TwitterUpdater", "Stored tweet from " + userId + ": " + username + " posted at " + timestamp);
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
			Log.d("TwitterUpdater", "Image not in cache. Downloading " + imageUrl);
			return forceDownloadImage(imageUrl, file);
		} else {
			Log.d("TwitterUpdater", "Image is cached at " + diskFilename);
		}
		return false;
	}
	
	private static boolean forceDownloadImage(String imageUrl, File file) {
		Bitmap image = BitmapDownloader.downloadBitmap(imageUrl);
		
		if(image == null) {
			Log.d("TwitterUpdater", "image was not downloaded");
			return false;
		}
		
		try {
			Log.d("ContentProviderTestActivity", "Wrote image file to device temp storage");
//			FileOutputStream fout = mCtx.openFileOutput(diskFilename, 0);
			FileOutputStream fout = new FileOutputStream(file);
			image.compress(Bitmap.CompressFormat.JPEG, 100, fout);
			fout.flush();
			fout.close();
			Log.d("TwitterUpdater", "Downloaded user profile image to " + file.getAbsoluteFile());
		} catch (FileNotFoundException e) {
			Log.d("TwitterUpdater", "Could not open " + file.getAbsoluteFile());
		} catch (IOException e) {
			Log.d("TwitterUpdater", "Could not compress bitmap");
		}
		return true;
	}
}