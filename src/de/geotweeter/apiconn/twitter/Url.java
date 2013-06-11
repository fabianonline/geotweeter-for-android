package de.geotweeter.apiconn.twitter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Url implements Serializable {

	private static final long serialVersionUID = -7819007371083758246L;
	public String expanded_url;
	public String url;
	public List<Integer> indices = new ArrayList<Integer>();
	public String display_url;

}
