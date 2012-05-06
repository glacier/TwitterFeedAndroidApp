package ca.xtreme.xlbootcamp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TweetsDbAdapter {

    public static final String KEY_USERNAME = "username";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_PICTURE_URL = "picture_url";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "TweetsDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table tweets (_id integer primary key autoincrement, "
        + "username text not null, message text not null, timestamp text not null, picture_url text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "tweets";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

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
    
    public TweetsDbAdapter(Context mCtx) {
    	this.mCtx = mCtx;
	}
    
    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public TweetsDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    /**
     * Return a Cursor over the list of all tweets in the database
     * 
     * @return Cursor over all tweets
     */
    public Cursor fetchAllTweets() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_USERNAME,
                KEY_MESSAGE, KEY_TIMESTAMP, KEY_PICTURE_URL}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the tweet that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchTweet(String username, String timestamp) throws SQLException {
    	Log.d(TAG, "Fetching tweet by " + username + " and " + timestamp);
        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID}, 
            		KEY_USERNAME + "=?" + " and " + KEY_TIMESTAMP + "=?", new String[] {username, timestamp}, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    
    /**
     * Store a downloaded tweet. If the tweet is
     * successfully stored return the new rowId, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the note
     * @param body the body of the note
     * @return rowId or -1 if failed
     */
    
    public long storeTweet(String username, String message, String timestamp, String pictureUrl) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_USERNAME, username);
        initialValues.put(KEY_MESSAGE, message);
        initialValues.put(KEY_TIMESTAMP, timestamp);
        initialValues.put(KEY_PICTURE_URL, pictureUrl);
        
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }
    
}