package de.fabianonline.geotweeter.timelineelements;

import java.util.Date;

import de.fabianonline.geotweeter.User;

public abstract class Event extends TimelineElement {
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
	public CharSequence getSourceText() {
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
}
