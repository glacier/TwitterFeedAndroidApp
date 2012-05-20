package ca.xtreme.xlbootcamp;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for TweetsProvider
 */
public final class Twitter {
    public static final String AUTHORITY = "ca.xtreme.xlbootcamp.tweetsprovider";

    private Twitter() {}
    
    /**
     * Tweets table
     */
    public static final class Tweets implements BaseColumns {
        private Tweets() {}
        
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/tweets");

        /**
         * The MIME type of {@link #CONTENT_URI} not yet defined.
         */

        public static final String DEFAULT_SORT_ORDER = "timestamp DESC";

        /**
         * The status of the tweet
         * <P>Type: TEXT</P>
         */
        public static final String MESSAGE = "message";

        /**
         * name of the twitter user
         * <P>Type: TEXT</P>
         */
        public static final String USERNAME = "username";

        /**
         * The timestamp for when the tweet was posted
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

		public static final String USER_ID = "userid";
		
		public static final String HASHTAG = "hashtag";
    }
}
