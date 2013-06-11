package de.geotweeter.exceptions;

import org.scribe.exceptions.OAuthException;

public class TemporaryTweetSendException extends TweetSendException {

	private static final long serialVersionUID = 3413446954757087342L;

	public TemporaryTweetSendException() {

	}

	public TemporaryTweetSendException(OAuthException ex) {
		super(ex);
	}

	public TemporaryTweetSendException(String msg) {
		super(msg);
	}

}
