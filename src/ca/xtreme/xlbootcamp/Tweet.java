package ca.xtreme.xlbootcamp;

import android.graphics.Bitmap;

// TODO Not needed anymore

public class Tweet {
	private String username;
	private String tweetContent;
	private String timestamp;
	private Bitmap profilePic;
	private String profilePicUrl;
	
	public Tweet(String username, String tweetContent, String timestamp, String url, Bitmap profilePic) {
		this.username = username;
		this.tweetContent = tweetContent;
		this.timestamp = timestamp;
		this.profilePicUrl = url;
		this.profilePic = profilePic;
	}

	public String getUsername() {
		return username;
	}
	
	public String getTweetContent() {
		return tweetContent;
	}

	public void setTweetContent(String tweetContent) {
		this.tweetContent = tweetContent;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public void setProfilePicUrl(String url) {
		this.profilePicUrl = url;
	}
	
	public String getProfilePicUrl() {
		return profilePicUrl;
	}
	public Bitmap getProfilePic() {
		return profilePic;
	}

	public void setProfilePic(Bitmap profilePic) {
		this.profilePic = profilePic;
	}
}
