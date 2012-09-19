package de.fabianonline.geotweeter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
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
	
	public String getScreenName() { 
		return screen_name; 
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
