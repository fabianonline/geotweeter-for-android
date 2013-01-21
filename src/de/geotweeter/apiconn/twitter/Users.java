package de.geotweeter.apiconn.twitter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Users implements Serializable {

	private static final long serialVersionUID = 5724315565389815618L;
	public long previous_cursor;
	public long next_cursor;
	public List<User> users = new ArrayList<User>();
	
}
