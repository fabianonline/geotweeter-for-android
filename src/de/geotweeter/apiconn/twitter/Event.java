package de.geotweeter.apiconn.twitter;

import java.util.Date;

import de.geotweeter.Constants.TLEType;
import de.geotweeter.timelineelements.TimelineElement;

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
	
//	@Override
//	public int getBackgroundDrawableID(boolean getDarkVersion) {
//		if (getDarkVersion) {
//			return R.drawable.listelement_background_dark_event;
//		} else {
//			return R.drawable.listelement_background_light_event;
//		}
//	}
	
	@Override
	public String getSenderName() {
		return source.getScreenName();
	}
	
	@Override
	public boolean isOwnMessage() {
		return false;
	}
	
	@Override
	public TLEType getType() {
		return TLEType.EVENT;
	}
}
