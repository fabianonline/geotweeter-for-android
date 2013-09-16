package de.geotweeter.apiconn.twitter;

import de.geotweeter.R;
import de.geotweeter.Utils;

public class FollowEvent extends Event {

	/**
	 * 
	 */
	private static final long serialVersionUID = 341093337397059148L;

	@Override
	public String getTitleForDisplay() {
		return source.getScreenName();
	}
	
	@Override
	public String getTextForDisplay() {
		return Utils.getString(R.string.event_follow_text);
	}
}
