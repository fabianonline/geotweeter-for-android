package de.geotweeter.apiconn.twitter;

public class NotShownEvent extends Event {
	/**
	 * 
	 */
	private static final long serialVersionUID = 808603672254742309L;

	@Override
	public String getTextForDisplay() {
		return null;
	}
}
