/*
 * 
 * Adapted from NotePad.java from the NotePad sample code
 * 
 * */

package ca.xtreme.xlbootcamp;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for NotePadProvider
 */
public final class Twitter {
    public static final String AUTHORITY = "mycontentprovider.tweetsprovider";

    // This class cannot be instantiated
    private Twitter() {}
    
    /**
     * Notes table
     */
    public static final class Tweets implements BaseColumns {
        // This class cannot be instantiated
        private Tweets() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/tweets");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.note";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.note";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "timestamp DESC";

        /**
         * The title of the note
         * <P>Type: TEXT</P>
         */
        public static final String MESSAGE = "message";

        /**
         * The note itself
         * <P>Type: TEXT</P>
         */
        public static final String USERNAME = "username";

        /**
         * The timestamp for when the note was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String CREATED_DATE = "timestamp";
        
        /**
         * The user profile image url 
         */
        
        public static final String PROFILE_IMAGE_URL = "photo_url";

        /**
         * The timestamp for when the note was last modified
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String MODIFIED_DATE = "modified";

        /**
         * Used to store the values of a row in the content provider
         */
		public static final String STATUS = "status";
    }
}
