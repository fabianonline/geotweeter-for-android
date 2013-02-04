package de.geotweeter.exceptions;

import de.geotweeter.Constants.RequestType;

public class BadConnectionException extends Exception {

	private static final long serialVersionUID = 5653337606591765663L;
	public RequestType type;

	public BadConnectionException() {
		type = RequestType.UNSPECIFIED;
	}

	public BadConnectionException(RequestType type) {
		this.type = type;
	}

}
