package de.fabianonline.geotweeter.timelineelements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;

import com.alibaba.fastjson.JSONObject;

import de.fabianonline.geotweeter.Constants;
import de.fabianonline.geotweeter.User;

import android.graphics.drawable.Drawable;
import android.view.View;

public class Tweet extends TimelineElement{
	private static final long serialVersionUID = -6610449879010917836L;
	private static final String LOG = "Tweet";
	public String text;
	public long id;
	public User user;
	public View view;
	public String source;
	public JSONObject entities;
	
	public long getID() {
		return id;
	}
	
	public String getTextForDisplay() {
		return "<strong>" + user.getScreenName() + "</strong> " + text;
	}
	
	public void setUser(User u) {
		if(User.all_users.containsKey(u.id)) {
			user = User.all_users.get(u.id);
		} else {
			User.all_users.put(u.id, u);
			user = u;
		}
	}
	
	public String getAvatarSource() {
		return user.getAvatarSource();
	}
	
	public void setSource(String str) {
		Matcher m = Constants.REGEXP_FIND_SOURCE.matcher(str);
		if (m.find()) {
			source = m.group(1);
		} else {
			source = "web";
		}
	}
	
	public Drawable getAvatarDrawable() { return user.avatar; }

	public CharSequence getSourceText() { return new SimpleDateFormat("dd.MM. HH:mm").format(created_at) + " from " + source; }
	public String getSenderScreenName() {
		return user.getScreenName();
	}
	
	@Override
	public boolean isReplyable() {
		return true;
	}
	
	@Override
	public boolean showNotification() {
		return true;
	}
	
	@Override
	public String getNotificationText(String type) {
		if (type.equals("mention")) {
			return "Mention von " + user.screen_name + ": " + text;
		} else if (type.equals("retweet")) {
			return user.screen_name + " retweetete: " + text;
		}
		return "";
	}
	
	@Override
	public String getNotificationContentTitle(String type) {
		if (type.equals("mention")) {
			return "Mention von " + user.screen_name;
		} else if(type.equals("retweet")) {
			return "Retweet von " + user.screen_name;
		}
		return "";
	}
	
	@Override
	public String getNotificationContentText(String type) {
		return text;
	}
}
