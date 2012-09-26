package de.fabianonline.geotweeter.timelineelements;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

public abstract class TimelineElement implements Serializable {
	private static final long serialVersionUID = -4794489823636370071L;
	private static final String LOG = "TimelineElement";
	protected Date created_at = new Date();
	private static SimpleDateFormat parseableDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
	
	
	abstract public String getTextForDisplay();
	abstract public CharSequence getSourceText();
	abstract public String getAvatarSource();
	abstract public long getID();
	abstract public String getSenderScreenName();
	abstract public int getBackgroundDrawableID();

	
	public Date getDate() {
		if (created_at != null) {
			return created_at;
		}
		return new Date();
	}

	public boolean isReplyable() {
		return false;
	}
	public boolean showNotification() {
		return false;
	}
	public String getNotificationText(String type) {
		return "";
	}
	public String getNotificationContentTitle(String type) {
		return "";
	}
	public String getNotificationContentText(String type) {
		return "";
	}
	
}
