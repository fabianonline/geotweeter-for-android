package de.geotweeter.exceptions;

public class FollowException extends Exception {

	private static final long serialVersionUID = 1591289727868565919L;
	private boolean unfollow;
	
	public FollowException(boolean unfollow) {
		this.unfollow = unfollow; 
	}
	
	public boolean isUnfollow() {
		return unfollow;
	}
	
}
