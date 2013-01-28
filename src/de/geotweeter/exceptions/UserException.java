package de.geotweeter.exceptions;

import de.geotweeter.exceptions.BadConnectionException.RequestType;

public class UserException extends Exception {

	private static final long serialVersionUID = -612448817195649790L;
	public int httpCode = 0;
	public RequestType type;
	
	public UserException(RequestType singleUser, int code) {
		httpCode = code;
	}
}
