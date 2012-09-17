package de.fabianonline.geotweeter;

import android.graphics.Bitmap;
import android.net.Uri;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Tweet extends TimelineElement{
	public String text;
	public long id;
	public User user;
	
	//public void setUser(User u) { user = u; }
	//public void setText(String t) { text = t; }
	
	public String getTextForDisplay() {
		return "<strong>" + user.getScreenName() + "</strong> " + text;
	}
	
	public Bitmap getAvatarImage() { return user.avatar; }
}
