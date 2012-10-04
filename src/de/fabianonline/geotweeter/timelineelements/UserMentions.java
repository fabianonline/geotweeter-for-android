package de.fabianonline.geotweeter.timelineelements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserMentions implements Serializable {

	private static final long serialVersionUID = 6282430489178779673L;

	public String name;
	public String id_str;
	public long id;
	public List<Integer> indices = new ArrayList<Integer>();
	public String screen_name;
}
