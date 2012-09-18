package de.fabianonline.geotweeter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;

@JsonIgnoreProperties(ignoreUnknown=true)
public class User {
	public static HashMap<Long, User> all_users = new HashMap<Long, User>();
	public Drawable avatar = null;
	public long id;
	public String name, screen_name, url, description, profile_image_url_https;
	public View[] views = new View[] {};
	
	public String getScreenName() { return screen_name; }
	
	private class UpdateBitmapThread implements Runnable {
		public void run() {
			URL url;
			try {
				url = new URL(profile_image_url_https);
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
				return;
			}
			try {
				avatar = new BitmapDrawable(BitmapFactory.decodeStream(url.openConnection().getInputStream()));
				avatar.invalidateSelf();
			} catch (IOException e) { e.printStackTrace(); }
		}
	}

	public static User fromJSONObject(JSONObject jsonObject) throws JSONException {
		long id = jsonObject.getLong("id");
		User u;
		if (User.all_users.containsKey(id)) {
			u = User.all_users.get(id);
		} else {
			u = new User();
			User.all_users.put(id, u);
		}
		u.name = jsonObject.getString("name");
		u.screen_name = jsonObject.getString("screen_name");
		u.url = jsonObject.getString("url");
		u.description = jsonObject.getString("description");
		u.profile_image_url_https = jsonObject.getString("profile_image_url_https");
		if (u.avatar==null) u.start_avatar_download();
		return u;
	}

	private void start_avatar_download() { new Thread(new UpdateBitmapThread()).start(); }
}
