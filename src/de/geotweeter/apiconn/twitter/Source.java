package de.geotweeter.apiconn.twitter;

import java.io.Serializable;

public class Source implements Serializable{
	
	private static final long serialVersionUID = 3741036733890866690L;
	
	public boolean can_dm;
	public boolean blocking;
	public boolean all_replies;
	public boolean want_retweets;
	public long id;
	public boolean marked_spam;
	public String screen_name;
	public boolean following;
	public boolean followed_by;
	public boolean notifications_enabled;
	
}