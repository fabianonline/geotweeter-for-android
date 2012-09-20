package de.fabianonline.geotweeter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;

import android.graphics.drawable.Drawable;
import android.view.View;

public class Tweet extends TimelineElement{
	private static final String LOG = "Tweet";
	public String text;
	public long id;
	public User user;
	public View view;
	public Date created_at;
	public String source;
	
	private static SimpleDateFormat parseableDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");

	
	//public void setUser(User u) { user = u; }
	//public void setText(String t) { text = t; }
	
	public String getTextForDisplay() {
		if (user == null) {
			int a = 17;
			if (a==17) {}
		}
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
	
	public void setCreated_at(String str) {
		try { created_at = parseableDateFormat.parse(str); } catch (ParseException e) {}
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
}
