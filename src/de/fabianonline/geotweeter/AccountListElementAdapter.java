package de.fabianonline.geotweeter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AccountListElementAdapter extends ArrayAdapter<UserElement> {

	private ArrayList<UserElement> items;
	private Context context;
	
	public AccountListElementAdapter(Context context, int textViewResourceId,
			ArrayList<UserElement> objects) {
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
		
		UserElement ale = (UserElement)items.get(position);
		
		if (ale != null) {
			TextView tvScreenName = (TextView) v.findViewById(R.id.tvScreenName);
			if (tvScreenName != null) {
				tvScreenName.setText(ale.getScreenName());
			}
			ImageView ivAvatar = (ImageView) v.findViewById(R.id.ivAccountAvatar);
			if (ivAvatar != null) {
				ivAvatar.setImageDrawable(ale.getAvatarDrawable());
			}
		}
		
		
		return v;
	}

}
