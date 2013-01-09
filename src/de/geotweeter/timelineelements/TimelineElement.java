package de.geotweeter.timelineelements;

import java.io.Serializable;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.util.Log;
import android.util.Pair;
import de.geotweeter.Constants.TLEType;
import de.geotweeter.R;
import de.geotweeter.Utils;

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
//	abstract public int getBackgroundDrawableID(boolean getDarkVersion);
	abstract public String getSenderString();
	abstract public boolean isOwnMessage();
	abstract public TLEType getType();
	
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
	
	/**
	 * Compares the age of two timeline elements
	 * 
	 * @param tle The element to be compared with
	 * @return true if the element is older than the given one
	 */
	public boolean olderThan(TimelineElement tle) {
		return (created_at.getTime() < tle.created_at.getTime());
	}
	
	/**
	 * Returns the background gradient id according to the element type
	 * and the chosen theme
	 * 
	 * @param type Element type
	 * @param darkVersion 
	 * @return Background gradient id
	 */
	public static int getBackgroundGradient(TLEType type, boolean darkVersion) {
		if (darkVersion) {
			switch (type) {
			case DM: return R.drawable.listelement_background_dark_dm;
			case EVENT: return R.drawable.listelement_background_dark_event;
			case MENTION: return R.drawable.listelement_background_dark_mention;
			case OWN: return R.drawable.listelement_background_dark_own;
			case READ: return R.drawable.listelement_background_dark_read;
			case UNREAD: return R.drawable.listelement_background_dark_unread;
			}
		} else {
			switch (type) {
			case DM: return R.drawable.listelement_background_light_dm;
			case EVENT: return R.drawable.listelement_background_light_event;
			case MENTION: return R.drawable.listelement_background_light_mention;
			case OWN: return R.drawable.listelement_background_light_own;
			case READ: return R.drawable.listelement_background_light_read;
			case UNREAD: return R.drawable.listelement_background_light_unread;
			}
		}
		return 0;
	}
	
	/**
	 * Returns the background color for the action buttons of a given
	 * timeline element type
	 * 
	 * @param type Element type
	 * @param darkVersion 
	 * @return Color id
	 */
	public static int getBackgroundColor(TLEType type, boolean darkVersion) {
		if (darkVersion) {
			switch (type) {
			case DM: return R.color.dark_dm_background_end;
			case EVENT: return R.color.dark_event_background_end;
			case MENTION: return R.color.dark_mention_background_end;
			case OWN: return R.color.dark_own_background_end;
			case READ: return R.color.dark_read_background_end;
			case UNREAD: return R.color.dark_unread_background_end;
			}
		} else {
			switch (type) {
			case DM: return R.color.light_dm_background_end;
			case EVENT: return R.color.light_event_background_end;
			case MENTION: return R.color.light_mention_background_end;
			case OWN: return R.color.light_own_background_end;
			case READ: return R.color.light_read_background_end;
			case UNREAD: return R.color.light_unread_background_end;
			}
		}		
		return 0;
	}
	
}
