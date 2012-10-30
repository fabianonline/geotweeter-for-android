package de.geotweeter.timelineelements;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

public abstract class TimelineElement implements Serializable {
	private static final long serialVersionUID = -4794489823636370071L;
	private static final String LOG = "TimelineElement";
	private static final Object parse_lock = new Object();
	
	private static SimpleDateFormat parseableDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
	public static String tweetTimeStyle = "dd.MM.yy HH:mm";
	
	protected Date created_at = new Date();
	
	abstract public String getTextForDisplay();
	abstract public String getSourceText();
	abstract public String getAvatarSource();
	abstract public long getID();
	abstract public String getSenderScreenName();
	abstract public int getBackgroundDrawableID(boolean getDarkVersion);
	abstract public String getSenderString();

	
	public Date getDate() {
		if (created_at != null) {
			return created_at;
		}
		return new Date();
	}
	
	public void setCreated_at(String str) {
		try { 
			synchronized(parse_lock) {
				created_at = parseableDateFormat.parse(str);
			}
		} catch (ParseException e) {
			Log.e(LOG, "Unparseable Date: " + str);
		}
	}
	
	public String getDateString() {
		if (tweetTimeStyle.equals("minutes")) {
			long time = System.currentTimeMillis() - created_at.getTime();
			time /= 1000;
			if (time <= 0) {
				return "gerade eben";
			}
			if (time < 60) {
				return "vor " + time + (time==1? " Sekunde": " Sekunden");
			}
			time /= 60;
			if (time < 60) {
				return "vor " + time + (time==1? " Minute": " Minuten");
			}
			time /= 60;
			if (time < 24) {
				return "vor " + time + (time==1? " Stunde": " Stunden");
			}
			time /= 24;
			if (time < 7) {
				return "am " + new SimpleDateFormat("EEEE").format(created_at);
			}
			return new SimpleDateFormat("dd.MM.yy HH:mm").format(created_at);
			
		} else if(tweetTimeStyle.matches("dd\\.MM\\.(yy)? HH:mm")) {
			return new SimpleDateFormat(tweetTimeStyle).format(created_at);
		}
		return "";
	}
	/*
	 * dd.MM.yy HH:mm</item>
        <item >dd.MM HH:mm</item>
        <item >minutes
	 */
	
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
	
	public boolean showWithFilter(String filter) {
		return true;
	}
	
	public String getPlaceString() {
		return null;
	}
		
}
