package de.fabianonline.geotweeter;

import java.util.HashMap;

import android.graphics.drawable.Drawable;
import android.view.View;

public class User {
	public static HashMap<Long, User> all_users = new HashMap<Long, User>();
	public Drawable avatar = null;
	public long id;
	public String name, screen_name, url, description, profile_image_url_https;
	public View[] views = new View[] {};
	
	public String getScreenName() { return screen_name; }
}
