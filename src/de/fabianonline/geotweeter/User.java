package de.fabianonline.geotweeter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

public class User extends UserElement implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2231080355596825396L;
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
			} catch (IOException e) { 
				e.printStackTrace(); 
			}
		}
		
	}

	private void start_avatar_download() { 
		new Thread(new UpdateBitmapThread()).start(); 
	}

	@Override
	public Drawable getAvatarDrawable() {
		return avatar;
	}

	public static ArrayList<User> getPersistentData(Context context, String[] accounts) {
		ArrayList<User> result = new ArrayList<User>();
		for (String id : accounts) {
			if (!id.equals("")) {
				try {
					FileInputStream fin = context.openFileInput(id + ".usr");
					ObjectInputStream oin = new ObjectInputStream(fin);
					User user = (User) oin.readObject();
					result.add(user);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	public void storeUser(Context context) {
		try {
			FileOutputStream fout = context.openFileOutput(String.valueOf(id) + ".usr", Context.MODE_PRIVATE);
			ObjectOutputStream oout = new ObjectOutputStream(fout);
			oout.writeObject(this);
			oout.close();
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
