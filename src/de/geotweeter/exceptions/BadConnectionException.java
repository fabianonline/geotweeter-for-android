package de.geotweeter.exceptions;

public class BadConnectionException extends Exception {

	private static final long serialVersionUID = 5653337606591765663L;
	public RequestType type;

	public enum RequestType {
		UNSPECIFIED, USER_TIMELINE, HOME_TIMELINE, MENTIONS, DM_SENT, DM_RCVD, SINGLE_TWEET, DELETE_DM, DELETE_TWEET, RETWEET, FAV, DEFAV, FOLLOW, UNFOLLOW, RELATIONSHIP, FOLLOWERS, FRIENDS, SINGLE_USER
	}

	public BadConnectionException() {
		type = RequestType.UNSPECIFIED;
	}

	public BadConnectionException(RequestType type) {
		this.type = type;
	}

}
