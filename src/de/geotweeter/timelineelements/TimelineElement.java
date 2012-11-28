package de.geotweeter.timelineelements;

import java.io.Serializable;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.geotweeter.R;
import de.geotweeter.Utils;

import android.util.Log;
import android.util.Pair;

/**
 * Abstract container for any timeline element 
 */
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

	/**
	 * Gets the creation time stamp
	 * 
	 * @return Creation time stamp
	 */
	public Date getDate() {
		if (created_at != null) {
			return created_at;
		}
		return new Date();
	}
	
	/**
	 * Sets the creation time stamp
	 * 
	 * @param str The time stamp to be set
	 */
	public void setCreated_at(String str) {
		try { 
			synchronized(parse_lock) {
				created_at = parseableDateFormat.parse(str);
			}
		} catch (ParseException e) {
			Log.e(LOG, "Unparseable Date: " + str);
		}
	}
	
	/**
	 * Generates the time stamp to be shown in the TimelineElement's view
	 * 
	 * @return Time stamp string to be shown
	 */
	public String getDateString() {
		if (tweetTimeStyle.equals("minutes")) {
			long time = System.currentTimeMillis() - created_at.getTime();
			time /= 1000;
			if (time <= 0) {
				return Utils.getString(R.string.timeline_element_timediff_now);
			}
			if (time < 60) {
				if (time==1) {
					return Utils.getString(R.string.timeline_element_timediff_one_second);
				} else {
					return Utils.formatString(R.string.timeline_element_timediff_multiple_seconds, time);
				}
			}
			time /= 60;
			if (time < 60) {
				if (time==1) {
					return Utils.getString(R.string.timeline_element_timediff_one_minute);
				} else {
					return Utils.formatString(R.string.timeline_element_timediff_multiple_minutes, time);
				}
			}
			time /= 60;
			if (time < 24) {
				if (time==1) {
					return Utils.getString(R.string.timeline_element_timediff_one_hour);
				} else {
					return Utils.formatString(R.string.timeline_element_timediff_multiple_hours, time);
				}
			}
			time /= 24;
			if (time < 7) {
				return Utils.formatString(R.string.timeline_element_timediff_days, new SimpleDateFormat("EEEE").format(created_at));
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
		
	public List<Pair<URL, URL>> getMediaList() {
		return new ArrayList<Pair<URL, URL>>();
	}
	
	public boolean olderThan(TimelineElement tle) {
		return (created_at.getTime() < tle.created_at.getTime());
	}
	
}
