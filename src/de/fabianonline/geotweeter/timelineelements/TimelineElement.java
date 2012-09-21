package de.fabianonline.geotweeter.timelineelements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class TimelineElement {
	protected Date created_at;
	private static SimpleDateFormat parseableDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
	
	public void setCreated_at(String str) {
		try { created_at = parseableDateFormat.parse(str); } catch (ParseException e) {}
	}
	
	abstract public String getTextForDisplay();
	abstract public CharSequence getSourceText();
	abstract public String getAvatarSource();
	abstract public long getID();
	public Date getDate() {
		if (created_at != null) {
			return created_at;
		}
		return new Date();
	}
}
