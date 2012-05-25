package ca.xtreme.xlbootcamp;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for TweetsDatabaseProvider
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
		 * Used to store the values of a row in the content provider
		 */
		public static final String TWEET = "tweet";
		
		public static final String DEFAULT_SORT_ORDER = "unix_time DESC";

		public static final String MESSAGE = "message";

		public static final String USERNAME = "username";

		public static final String CREATED_DATE = "timestamp";

		public static final String PROFILE_IMAGE_URL = "photo_url";

		public static final String USER_ID = "userid";

		public static final String HASHTAG = "hashtag";

		public static final String UNIX_TIME = "unix_time";
	}
}
