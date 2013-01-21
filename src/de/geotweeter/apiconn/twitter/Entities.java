package de.geotweeter.apiconn.twitter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.geotweeter.timelineelements.UserMention;

public class Entities implements Serializable {

	private static final long serialVersionUID = 4057021106750032881L;
	public List<Url> urls = new ArrayList<Url>();
	public List<Hashtag> hashtags = new ArrayList<Hashtag>();
	public List<UserMention> user_mentions = new ArrayList<UserMention>();
	public List<Media> media = new ArrayList<Media>();
	
}
