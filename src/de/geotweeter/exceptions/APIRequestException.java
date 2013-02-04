package de.geotweeter.exceptions;

import de.geotweeter.Constants.RequestType;

public class APIRequestException extends Exception {

	private static final long serialVersionUID = -192234158348831151L;
	private RequestType type;
	private int httpCode;
	private String responseBody;

	public APIRequestException(RequestType type, int httpCode,
			String responseBody) {
		this.type = type;
		this.httpCode = httpCode;
		this.responseBody = responseBody;
	}

	public RequestType getType() {
		return type;
	}

	public int getHttpCode() {
		return httpCode;
	}

	public String getResponseBody() {
		return responseBody;
	}

}
