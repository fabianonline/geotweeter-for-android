package de.geotweeter.apiconn.twitter;

import java.io.Serializable;
import java.util.List;

public class TwitterConfig implements Serializable {

	private static final long serialVersionUID = 2236060699877071662L;
	
	public int characters_reserved_per_media;
	public int max_media_per_upload;
	public List<String> non_username_paths;
	public int photo_size_limit;
	public int short_url_length;
	public int short_url_length_https;
	
}
