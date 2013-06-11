package de.geotweeter.apiconn.twitter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserIds implements Serializable {

	private static final long serialVersionUID = 3192444410497783844L;
	public long previous_cursor;
	public long next_cursor;
	public List<Long> ids = new ArrayList<Long>();

}
