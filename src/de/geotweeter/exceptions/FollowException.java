package de.geotweeter.exceptions;

public class FollowException extends Exception {

	private static final long serialVersionUID = 1591289727868565919L;
	private boolean unfollow;
	private int httpCode;
	
	public FollowException(boolean unfollow) {
		this.unfollow = unfollow; 
	}
	
	public FollowException(boolean unfollow, int httpCode) {
		this.unfollow = unfollow;
		this.httpCode = httpCode;
	}

	public boolean isUnfollow() {
		return unfollow;
	}
	
	public int getHttpCode() {
		return httpCode;
	}
	
}
