package ca.xtreme.xlbootcamp.twitter.app;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import ca.xtreme.xlbootcamp.twitter.app.Twitter.Tweets;

public class TweetsDatabaseProvider extends ContentProvider {

	private static final String TAG = "TweetsDatabaseProvider";

	private static final String DATABASE_CREATE_SQL =
			" create table tweets (_id integer primary key autoincrement, "
			+ "userid text not null, username text not null, message text not null, "
			+ "unix_time text not null, timestamp text not null, photo_url text not null, "
			+ "hashtag text not null);";

	private static final String DATABASE_NAME = "tweets.db";
	private static final String DATABASE_TABLE = "tweets";
	private static final int DATABASE_VERSION = 1;


	private static final int TWEETS = 1;
	private static final int TWEET_ID = 2;
	private static final int TWEET_HASHTAG = 3;

	private static HashMap<String, String> sTweetsProjectionMap;
	private static final UriMatcher sUriMatcher;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_SQL);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS tweets");
			onCreate(db);
		}
	}

	private DatabaseHelper databaseHelper;

	@Override
	public boolean onCreate() {
		databaseHelper = new DatabaseHelper(getContext());
		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(DATABASE_TABLE);

		switch (sUriMatcher.match(uri)) {
		case TWEETS:
			qb.setProjectionMap(sTweetsProjectionMap);
			break;
		case TWEET_ID:
			qb.setProjectionMap(sTweetsProjectionMap);
			qb.appendWhere(Tweets._ID + "=" + uri.getPathSegments().get(1));
			break;
		case TWEET_HASHTAG:
			qb.setProjectionMap(sTweetsProjectionMap);
			qb.appendWhere(Tweets.HASHTAG + "=" + "\"" 
						  + uri.getPathSegments().get(1).substring(1) 
						  + "\"");
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = Twitter.Tweets.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		// Get the database and run the query
		SQLiteDatabase db = databaseHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		
		return c;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// Validate the requested uri
		if (sUriMatcher.match(uri) != TWEETS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Make sure that the row to be inserted is not empty
		if (values == null || values.size() == 0) {
			throw new IllegalArgumentException("Content values cannot be empty");
		}

		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		long rowId = db.insert(DATABASE_TABLE, Tweets.TWEET, values);
		Log.d(TAG, "insert(): Inserting on uri " + uri.toString());
		
		if (rowId > 0) {
			return ContentUris.withAppendedId(Twitter.Tweets.CONTENT_URI, rowId);
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException("Content provider update is not implemented");
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException("Content provider delete is not implemented");
	}

	@Override
	public String getType(Uri uri) {
		throw new UnsupportedOperationException("Content provider getType is not implemented");
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(Twitter.AUTHORITY, "tweets", TWEETS);
		sUriMatcher.addURI(Twitter.AUTHORITY, "tweets/#", TWEET_ID);
		sUriMatcher.addURI(Twitter.AUTHORITY, "tweets/*", TWEET_HASHTAG);

		sTweetsProjectionMap = new HashMap<String, String>();
		sTweetsProjectionMap.put(Tweets._ID, Tweets._ID);
		sTweetsProjectionMap.put(Tweets.USER_ID, Tweets.USER_ID);
		sTweetsProjectionMap.put(Tweets.USERNAME, Tweets.USERNAME);
		sTweetsProjectionMap.put(Tweets.MESSAGE, Tweets.MESSAGE);
		sTweetsProjectionMap.put(Tweets.PROFILE_IMAGE_URL, Tweets.PROFILE_IMAGE_URL);
		sTweetsProjectionMap.put(Tweets.CREATED_DATE, Tweets.CREATED_DATE);
		sTweetsProjectionMap.put(Tweets.HASHTAG, Tweets.HASHTAG);
	}
}