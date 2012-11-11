package de.geotweeter.apiconn;

import java.io.Serializable;

public class VimeoVideoInfo implements Serializable {

	private static final long serialVersionUID = -4330096112037515496L;
	public long id;
	public String title;
	public String description;
	public String url;
	public String upload_date;
	public String mobile_url;
	public String thumbnail_small;
	public String thumbnail_medium;
	public String thumbnail_large;
	public long user_id;
	public String user_name;
	public String user_url;
	public String user_portrait_small;
	public String user_portrait_medium;
	public String user_portrait_large;
	public String user_portrait_huge;
	public int stats_number_of_likes;
	public int stats_number_of_plays;
	public int stats_number_of_comments;
	public int duration;
	public int width;
	public int height;
	public String tags;
	public String embed_privacy;
	
}
