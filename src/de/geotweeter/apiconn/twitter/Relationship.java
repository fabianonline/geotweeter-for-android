package de.geotweeter.apiconn.twitter;

import java.io.Serializable;

public class Relationship implements Serializable {

	public class Source {
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
	public class Target {
		public long id;
		public String screen_name;
		public boolean following;
		public boolean followed_by;
	}
	private static final long serialVersionUID = 2833914511400753030L;

	public Target target;
	public Source source;
	
}
