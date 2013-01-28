package de.geotweeter.exceptions;

import de.geotweeter.exceptions.BadConnectionException.RequestType;

public class RelationshipException extends Exception {

	private static final long serialVersionUID = -2405720001776967080L;
	public int httpCode = 0;
	public RequestType type;

	public RelationshipException(RequestType friends, int code) {
		httpCode = code;
	}
	
}
