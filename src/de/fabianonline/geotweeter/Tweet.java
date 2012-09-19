package de.fabianonline.geotweeter;

import android.graphics.drawable.Drawable;
import android.view.View;

public class Tweet extends TimelineElement {
	public String text;
	public long id;
	public User user;
	public View view;
	public String created_at;
	public String source;
	
	//public void setUser(User u) { user = u; }
	//public void setText(String t) { text = t; }
	
	public String getTextForDisplay() {
		return "<strong>" + user.getScreenName() + "</strong> " + text;
	}
	
	public Drawable getAvatarDrawable() { 
		return user.avatar; 
	}

	public CharSequence getSourceText() { 
		return /*new SimpleDateFormat("dd.MM. HH:mm").format(created_at)*/ created_at + " from " + source; 
	}
}
