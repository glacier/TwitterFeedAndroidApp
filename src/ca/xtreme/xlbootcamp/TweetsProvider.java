package ca.xtreme.xlbootcamp;

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
import ca.xtreme.xlbootcamp.Twitter.Tweets;

public class TweetsProvider extends ContentProvider {
	
    /**
     * Database creation sql statement
     */
	
	private static final String TAG = "TweetsProvider";
	
    private static final String DATABASE_CREATE =
        "create table tweets (_id integer primary key autoincrement, "
        + "userid text not null, username text not null, message text not null, "
        + "timestamp text not null, photo_url text not null);";

    private static final String DATABASE_NAME = "tweets.db";
    private static final String DATABASE_TABLE = "tweets";
    private static final int DATABASE_VERSION = 2;

    private static HashMap<String, String> sNotesProjectionMap;
//    private static HashMap<String, String> sLiveFolderProjectionMap;

    private static final int TWEETS = 1;
    private static final int TWEET_ID = 2;
    private static final int LIVE_FOLDER_NOTES = 3;

    private static final UriMatcher sUriMatcher;
    

    private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS tweets");
            onCreate(db);
        }
    }
    
    private DatabaseHelper mOpenHelper;
    
	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DATABASE_TABLE);

		switch (sUriMatcher.match(uri)) {
        case TWEETS:
            qb.setProjectionMap(sNotesProjectionMap);
            break;

        case TWEET_ID:
            qb.setProjectionMap(sNotesProjectionMap);
            qb.appendWhere(Tweets._ID + "=" + uri.getPathSegments().get(1));
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        
        //add a case here to handle getting Tweets with photos
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = Twitter.Tweets.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		// Validate the requested uri
		if (sUriMatcher.match(uri) != TWEETS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		Long now = Long.valueOf(System.currentTimeMillis());

		// Make sure that the fields are all set
		if (values.containsKey(Twitter.Tweets.CREATED_DATE) == false) {
			values.put(Twitter.Tweets.CREATED_DATE, now);
		}

		if (values.containsKey(Twitter.Tweets.USERNAME) == false) {
			values.put(Twitter.Tweets.USERNAME, "test");
		}

		if (values.containsKey(Twitter.Tweets.MESSAGE) == false) {
			values.put(Twitter.Tweets.MESSAGE, "this is a test tweet message");
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert(DATABASE_TABLE, Tweets.STATUS, values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(Twitter.Tweets.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	/* Leave these methods for now ... */
	
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
    
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Twitter.AUTHORITY, "tweets", TWEETS);
        sUriMatcher.addURI(Twitter.AUTHORITY, "tweets/#", TWEET_ID);
        sUriMatcher.addURI(Twitter.AUTHORITY, "live_folders/notes", LIVE_FOLDER_NOTES);

        sNotesProjectionMap = new HashMap<String, String>();
        sNotesProjectionMap.put(Tweets._ID, Tweets._ID);
        sNotesProjectionMap.put(Tweets.USER_ID, Tweets.USER_ID);
        sNotesProjectionMap.put(Tweets.USERNAME, Tweets.USERNAME);
        sNotesProjectionMap.put(Tweets.MESSAGE, Tweets.MESSAGE);
        sNotesProjectionMap.put(Tweets.PROFILE_IMAGE_URL, Tweets.PROFILE_IMAGE_URL);
        sNotesProjectionMap.put(Tweets.CREATED_DATE, Tweets.CREATED_DATE);
    }
}