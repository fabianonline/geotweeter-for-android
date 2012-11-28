package de.geotweeter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.geotweeter.activities.TimelineActivity;

/**
 * Provides the View for the authenticated accounts list
 * 
 * @author Lutz Krumme (@el_emka)
 *
 */
public class AccountListElementAdapter extends ArrayAdapter<Account> {

	private List<Account> items;
	private Context context;
	
	/**
	 * {@inheritDoc}
	 * 
	 * @param context
	 * @param textViewResourceId
	 * @param objects
	 */
	public AccountListElementAdapter(Context context, int textViewResourceId,
			List<Account> objects) {
		super(context, textViewResourceId, objects);
		this.items = objects;
		this.context = context;
	}
	
	/**
	 * Provides the UI thread with a view of a certain array position
	 * 
	 * @param position The element index whose view should be built
	 * @param convertView The view object to be designed, will be constructed if null
	 * @param parent
	 */
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
				TimelineActivity.background_image_loader.displayImage(listElement.getUser().getAvatarSource(), ivAvatar, true);
			}
		}
		
		
		return v;
	}

}
