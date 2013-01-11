package de.geotweeter.exceptions;

public class PermanentTweetSendException extends TweetSendException {

	public PermanentTweetSendException(String msg) {
		super(msg);
	}

	private static final long serialVersionUID = -7277568764000757431L;
	
}
