package de.fabianonline.geotweeter.timelineelements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Coordinates implements Serializable {
	private static final long serialVersionUID = 2580401981338023484L;
	public List<Float> coordinates = new ArrayList<Float>();
	public String type;
	
}
