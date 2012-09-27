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
	
	private static SimpleDateFormat parseableDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
	public static String tweetTimeStyle;
	
	protected Date created_at = new Date();
	
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
	
	public void setCreated_at(String str) {
		try { 
			created_at = parseableDateFormat.parse(str); 
		} catch (ParseException e) {
			Log.e(LOG, "Unparseable Date: " + str);
		}
	}
	
	protected String getDateString() {
		Log.d(LOG, tweetTimeStyle);
		if(tweetTimeStyle.equals("minutes")) {
			long time = System.currentTimeMillis()-created_at.getTime();
			if(time >= 0) {
				time /= 1000;
				if(time < 60) {
					return "vor " + time + " Sekunden";
				}
				time /= 60;
				if(time < 60) {
					return "vor " + time + " Minuten";
				}
				time /= 60;
				if(time < 24) {
					return "vor " + time + " Stunden";
				}
				time /= 24;
				if(time < 7) {
					return "am " + new SimpleDateFormat("EEEE").format(created_at);
				}
				return new SimpleDateFormat("dd.MM.yy HH:mm").format(created_at);
			}
		} else if(tweetTimeStyle.matches("dd\\.MM(\\.yy)? HH:mm")) {
			return new SimpleDateFormat(tweetTimeStyle).format(created_at);
		}
		return "Zeit";
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
	
}
