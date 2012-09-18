package de.fabianonline.geotweeter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.drawable.Drawable;
import android.text.format.Time;
import android.view.View;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class Tweet extends TimelineElement{
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
	
	public Drawable getAvatarDrawable() { return user.avatar; }

	public CharSequence getSourceText() { return /*new SimpleDateFormat("dd.MM. HH:mm").format(created_at)*/ created_at + " from " + source; }
}
