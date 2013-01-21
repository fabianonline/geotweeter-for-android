package de.geotweeter.apiconn.twitter;

import java.io.Serializable;

public class Place implements Serializable {
	private static final long serialVersionUID = -2119576575936549526L;

	private String full_name;

	public String getFullName() {
		return full_name;
	}
}
