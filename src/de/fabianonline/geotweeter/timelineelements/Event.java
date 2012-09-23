package de.fabianonline.geotweeter.timelineelements;

import java.util.Date;

public abstract class Event extends TimelineElement {
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
}
