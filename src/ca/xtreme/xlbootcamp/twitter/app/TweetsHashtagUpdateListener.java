package ca.xtreme.xlbootcamp.twitter.app;

public interface TweetsHashtagUpdateListener {
	void onUpdateStarted();
	void onUpdateFailed();
	void onUpdateSucceeded();
}
