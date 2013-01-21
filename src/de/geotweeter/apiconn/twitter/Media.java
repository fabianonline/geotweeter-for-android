package de.geotweeter.apiconn.twitter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Media implements Serializable {

	private static final long serialVersionUID = -5861599908982289559L;
	public String display_url;
	public String expanded_url;
	public long id;
	public String id_str;
	public List<Integer> indices = new ArrayList<Integer>();
	public String media_url;
	public String media_url_https;
	public Sizes sizes;
	public long source_status_id;
	public String source_status_id_str;
	public String type;
	public String url;
	
}
