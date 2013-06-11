package de.geotweeter.exceptions;

import org.scribe.exceptions.OAuthException;

public class TweetSendException extends Exception {

	private static final long serialVersionUID = 3953083064393551380L;

	public TweetSendException() {

	}

	public TweetSendException(OAuthException ex) {
		super(ex);
	}

	public TweetSendException(String msg) {
		super(msg);
	}

}
