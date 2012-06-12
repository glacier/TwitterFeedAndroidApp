package ca.xtreme.xlbootcamp.twitter.api;

@SuppressWarnings("serial")
public class TwitterClientException extends Exception {

	public TwitterClientException(String message) {
		super(message);
	}
	
	public TwitterClientException(String message, Throwable cause) {
		super(message, cause);
	}
}
