package de.geotweeter.timelineelements;

import java.util.Date;

import de.geotweeter.R;
import de.geotweeter.User;

public abstract class Event extends TimelineElement {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6213231225574876527L;
	public User source;
	
	@Override
	public long getID() {
		return 0;
	}
	
	@Override
	public String getAvatarSource() {
		return null;
	}
	
	@Override
	public String getSourceText() {
		return null;
	}
	
	@Override
	public Date getDate() {
		return new Date();
	}
	
	public String getSenderScreenName() {
		return null;
	}
	
	public void setSource(User u) {
		if(User.all_users.containsKey(u.id)) {
			source = User.all_users.get(u.id);
		} else {
			User.all_users.put(u.id, u);
			source = u;
		}
	}
	
	@Override
	public int getBackgroundDrawableID(boolean getDarkVersion) {
		if (getDarkVersion) {
			return R.drawable.listelement_background_dark_status;
		} else {
			return R.drawable.listelement_background_light_status;
		}
	}
	
	@Override
	public String getSenderString() {
		return source.getScreenName();
	}
	
	@Override
	public boolean isOwnMessage() {
		return false;
	}
}
