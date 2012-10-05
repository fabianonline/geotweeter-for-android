package de.fabianonline.geotweeter.timelineelements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Entities implements Serializable {

	private static final long serialVersionUID = 4057021106750032881L;
	public List<Url> urls = new ArrayList<Url>();
	public List<Hashtag> hashtags = new ArrayList<Hashtag>();
	public List<UserMention> user_mentions = new ArrayList<UserMention>();
	public List<Media> media = new ArrayList<Media>();
	
}
