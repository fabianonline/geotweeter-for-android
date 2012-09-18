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

	private void start_avatar_download() { new Thread(new UpdateBitmapThread()).start(); }
}
