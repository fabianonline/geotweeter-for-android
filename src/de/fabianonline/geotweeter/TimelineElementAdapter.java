package de.fabianonline.geotweeter;

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
import de.fabianonline.geotweeter.activities.TimelineActivity;
import de.fabianonline.geotweeter.timelineelements.TimelineElement;

public class TimelineElementAdapter extends ArrayAdapter<TimelineElement>{
	private ArrayList<TimelineElement> items;
	private Context context;

	public TimelineElementAdapter(Context context, 
			int textViewResourceId, ArrayList<TimelineElement> objects) {
		super(context, textViewResourceId, objects);
		this.items = objects;
		this.context = context;
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
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.timeline_element, null);
		}
		TimelineElement t = (TimelineElement) items.get(position);
		if (t != null) {
			FrameLayout mapContainer = (FrameLayout) v.findViewById(R.id.map_fragment_container);
			mapContainer.removeAllViews();
			
			LinearLayout mapAndControls = (LinearLayout) v.findViewById(R.id.map_and_controls);
			mapAndControls.setVisibility(View.GONE);
			
			TextView text = (TextView) v.findViewById(R.id.tweet_content);
			if (text != null) { 
				text.setText(Html.fromHtml(t.getTextForDisplay()));
			}
			
			text = (TextView) v.findViewById(R.id.source_text);
			if (text != null) {
				text.setText(t.getSourceText());
			}
			
			ImageView img = (ImageView) v.findViewById(R.id.avatar_image);
			if (img != null) {
				TimelineActivity.background_image_loader.displayImage(t.getAvatarSource(), img);
			}
						
		}
		//v.findViewById(R.id.tweet_element).setOnClickListener(new OnClickListener());
		v.setBackgroundResource(t.getBackgroundDrawableID());
		return v;	
	}
	
	public ArrayList<TimelineElement> getItems() {
		return items;
	}


}
