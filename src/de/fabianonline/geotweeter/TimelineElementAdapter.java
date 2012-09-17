package de.fabianonline.geotweeter;

import java.util.ArrayList;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TimelineElementAdapter extends ArrayAdapter<TimelineElement>{
	private ArrayList<TimelineElement> items;
	private Context context;
	private View last_opened_view = null;

	public TimelineElementAdapter(Context context, 
			int textViewResourceId, ArrayList<TimelineElement> objects) {
		super(context, textViewResourceId, objects);
		this.items = objects;
		this.context = context;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v==null) {
			LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.timeline_element, null);
		}
		TimelineElement t = (TimelineElement)items.get(position);
		if (t!=null) {
			TextView text = (TextView) v.findViewById(R.id.tweet_content);
			if (text!=null) { 
				text.setText(Html.fromHtml(t.getTextForDisplay()));
			}
			
			text = (TextView) v.findViewById(R.id.source_text);
			if (text!=null) {
				text.setText(t.getSourceText());
			}
			
			ImageView img = (ImageView) v.findViewById(R.id.avatar_image);
			if (img!=null) {
				img.setImageDrawable(t.getAvatarDrawable());
			}
		}
		//v.findViewById(R.id.tweet_element).setOnClickListener(new OnClickListener());
		return v;	
	}


}
