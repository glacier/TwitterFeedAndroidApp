package ca.xtreme.xlbootcamp;

public class Tweet {
	private String username;
	private String tweetContent;
	private String timestamp;
	private String profilePic;
	
	public Tweet(String username, String tweetContent, String timestamp, String profilePic) {
		this.username = username;
		this.tweetContent = tweetContent;
		this.timestamp = timestamp;
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

	public String getProfilePic() {
		return profilePic;
	}

	public void setProfilePic(String profilePic) {
		this.profilePic = profilePic;
	}
}
