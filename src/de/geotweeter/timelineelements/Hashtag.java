package de.geotweeter.timelineelements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Hashtag implements Serializable {

	private static final long serialVersionUID = 3435885497447226002L;
	public List<Integer> indices = new ArrayList<Integer>();
	public String text;
	
}
