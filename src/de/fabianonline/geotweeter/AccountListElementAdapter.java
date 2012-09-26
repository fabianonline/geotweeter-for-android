package de.fabianonline.geotweeter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.fabianonline.geotweeter.activities.TimelineActivity;

public class AccountListElementAdapter extends ArrayAdapter<Account> {

	private ArrayList<Account> items;
	private Context context;
	
	public AccountListElementAdapter(Context context, int textViewResourceId,
			ArrayList<Account> objects) {
		super(context, textViewResourceId, objects);
		this.items = objects;
		this.context = context;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.account_list_element, null);
		}
		
//		UserElement listElement = (UserElement)items.get(position);
		Account listElement = (Account)items.get(position);
		
		if (listElement != null) {
			TextView tvScreenName = (TextView) v.findViewById(R.id.tvScreenName);
			if (tvScreenName != null) {
				tvScreenName.setText(listElement.getUser().getScreenName());
			}
			ImageView ivAvatar = (ImageView) v.findViewById(R.id.ivAccountAvatar);
			if (ivAvatar != null) {
				TimelineActivity.background_image_loader.displayImage(listElement.getUser().getAvatarSource(), ivAvatar);
			}
		}
		
		
		return v;
	}

}
