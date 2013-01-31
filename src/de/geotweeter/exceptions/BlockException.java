package de.geotweeter.exceptions;

public class BlockException extends Exception {

	private static final long serialVersionUID = -9071305264197661550L;
	private boolean unblock;
	private int httpCode = -1;
	
	public BlockException(boolean unblock, int httpCode) {
		this.unblock = unblock;
		this.httpCode = httpCode;
	}
	
	public BlockException(boolean unblock) {
		this.unblock = unblock; 
	}
	
	public boolean isUnblock() {
		return unblock;
	}

	public int getHttpCode() {
		return httpCode;
	}
	
}
