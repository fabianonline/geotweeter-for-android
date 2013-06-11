package de.geotweeter.apiconn.twitter;

import java.io.Serializable;

public class Target implements Serializable {

	private static final long serialVersionUID = -82666116341243289L;

	public long id;
	public String screen_name;
	public boolean following;
	public boolean followed_by;

}