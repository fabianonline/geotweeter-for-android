package de.geotweeter;

import java.util.ArrayList;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.geotweeter.R;
import de.geotweeter.activities.TimelineActivity;
import de.geotweeter.timelineelements.TimelineElement;

public class TimelineElementAdapter extends ArrayAdapter<TimelineElement>{
	private ArrayList<TimelineElement> items;
	private Context context;
	private boolean useDarkTheme;

	public TimelineElementAdapter(Context context, 
			int textViewResourceId, ArrayList<TimelineElement> objects) {
		super(context, textViewResourceId, objects);
		this.items = objects;
		this.context = context;
		useDarkTheme = context.getSharedPreferences(Constants.PREFS_APP, 0).getBoolean("pref_dark_theme", false);
	}
	
	public void addAsFirst(TimelineElement t) {
		items.add(0, t);
		this.notifyDataSetChanged();
	}
	
	public void addAllAsFirst(ArrayList<TimelineElement> elements) {
		items.addAll(0, elements);
		this.notifyDataSetChanged();
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		TimelineElement t = (TimelineElement) items.get(position);
		
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.timeline_element, null);
		}
		
		if (t != null) {
			FrameLayout mapContainer = (FrameLayout) v.findViewById(R.id.map_fragment_container);
			mapContainer.removeAllViews();
			
			View mapAndControls = v.findViewById(R.id.map_and_controls);
			mapAndControls.setVisibility(View.GONE);
			
			TextView txtView;
			String text;
			
			txtView = (TextView) v.findViewById(R.id.txtSender);
			if (txtView != null) {
				txtView.setText(t.getSenderString());
			}
			
			txtView = (TextView) v.findViewById(R.id.txtTimestamp);
			if (txtView != null) {
				txtView.setText(t.getDateString());
			}
			
			txtView = (TextView) v.findViewById(R.id.txtText);
			if (txtView != null) { 
				txtView.setText(t.getTextForDisplay());
			}
			
			txtView = (TextView) v.findViewById(R.id.txtPlace);
			if (txtView != null) {
				text = t.getPlaceString();
				if (text == null) {
					txtView.setVisibility(View.GONE);
				} else {
					txtView.setText(text);
					txtView.setVisibility(View.VISIBLE);
				}
			}
			
			txtView = (TextView) v.findViewById(R.id.txtSource);
			if (txtView != null) {
				text = t.getSourceText();
				if (text == null) {
					txtView.setVisibility(View.GONE);
				} else {
					txtView.setText(text);
					txtView.setVisibility(View.VISIBLE);
				}
			}
			
			ImageView img = (ImageView) v.findViewById(R.id.avatar_image);
			if (img != null) {
				TimelineActivity.background_image_loader.displayImage(t.getAvatarSource(), img);
			}
						
		}
		//v.findViewById(R.id.tweet_element).setOnClickListener(new OnClickListener());
		v.setBackgroundResource(t.getBackgroundDrawableID(useDarkTheme));
		return v;	
	}
	
	public ArrayList<TimelineElement> getItems() {
		return items;
	}


}
