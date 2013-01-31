package de.geotweeter.exceptions;

public class SpamException extends Exception {

	private static final long serialVersionUID = 5181652555241659579L;
	private int httpCode = -1;
	
	public SpamException(int httpCode) {
		this.httpCode = httpCode;
	}
	
	public SpamException() {
	}
	
	public int getHttpCode() {
		return httpCode;
	}
	
}
