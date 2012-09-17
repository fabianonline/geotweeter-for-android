package de.fabianonline.geotweeter;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.format.Time;
import android.view.View;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Tweet extends TimelineElement{
	public String text;
	public long id;
	public User user;
	public View view;
	public Time created_at;
	public String source;
	
	//public void setUser(User u) { user = u; }
	//public void setText(String t) { text = t; }
	
	public String getTextForDisplay() {
		return "<strong>" + user.getScreenName() + "</strong> " + text;
	}
	
	public Drawable getAvatarDrawable() { return user.avatar; }

	public CharSequence getSourceText() { return created_at.format("%d.%m. %H:%M") + " from " + source; }
}
