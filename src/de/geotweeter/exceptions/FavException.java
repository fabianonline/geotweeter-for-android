package de.geotweeter.exceptions;

public class FavException extends Exception {

	private static final long serialVersionUID = -1928701430344659483L;
	private boolean defav;
	
	public FavException(boolean defav) {
		this.defav = defav;
	}

	public boolean isDefav() {
		return defav;
	}

}
