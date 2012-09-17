package de.fabianonline.geotweeter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
			TextView text = (TextView) v.findViewById(R.id.textView1);
			if (text!=null) { 
				text.setText(Html.fromHtml(t.getTextForDisplay()));
			}
			
			ImageView img = (ImageView) v.findViewById(R.id.avatar_image);
			if (img!=null) {
				img.setImageBitmap(t.getAvatarImage());
			}
		}
		//v.findViewById(R.id.tweet_element).setOnClickListener(new OnClickListener());
		return v;	
	}


}
