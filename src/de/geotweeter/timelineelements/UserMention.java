package de.geotweeter.timelineelements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserMention implements Serializable {

	private static final long serialVersionUID = 6282430489178779673L;

	public String name;
	public long id;
	public List<Integer> indices = new ArrayList<Integer>();
	public String screen_name;
}
