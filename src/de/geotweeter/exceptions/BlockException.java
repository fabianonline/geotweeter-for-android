package de.geotweeter.exceptions;

public class BlockException extends Exception {

	private static final long serialVersionUID = -9071305264197661550L;
	private boolean unblock;
	
	public BlockException(boolean unblock) {
		this.unblock = unblock; 
	}
	
	public boolean isUnblock() {
		return unblock;
	}
	
}
