package de.geotweeter;

import java.io.Serializable;

import de.geotweeter.apiconn.twitter.TwitterConfig;

public class Configuration implements Serializable {

	private static final long serialVersionUID = -8516507797544584891L;
	public TwitterConfig twitter;
	public long twitterTimestamp;
		
}
