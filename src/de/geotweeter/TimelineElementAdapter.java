package de.geotweeter;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import de.geotweeter.activities.TimelineActivity;
import de.geotweeter.timelineelements.DirectMessage;
import de.geotweeter.timelineelements.Hashtag;
import de.geotweeter.timelineelements.TLEComparator;
import de.geotweeter.timelineelements.TimelineElement;
import de.geotweeter.timelineelements.Tweet;

public class TimelineElementAdapter extends ArrayAdapter<TimelineElement>{
	private List<TimelineElement> items;
	private Context context;
	private HashMap<Long, TimelineElement> available = new HashMap<Long, TimelineElement>();
	private Typeface tf;
	
	/**
	 * Constructor
	 * 
	 * @param context Current context
	 * @param textViewResourceId The resource ID for a layout file containing a TextView to use when instantiating views.
	 * @param objects The objects to be represented
	 */
	public TimelineElementAdapter(Context context, 
			int textViewResourceId, List<TimelineElement> objects) {
		super(context, textViewResourceId, objects);
		if (objects != null) {
			Collections.sort(objects, new TLEComparator());
		}
		this.items = objects;
		this.context = context;
		tf = Typeface.createFromAsset(context.getAssets(), "fonts/Entypo.otf");
	}
	
	/**
	 * Adds a timeline element to the adapter
	 * 
	 * @param t The element to be added
	 */
	public void addAsFirst(TimelineElement t) {
		if (!available.containsKey(t.getID())) {
			if (t.olderThan(items.get(0))) {
				items.add(0, t);
				Collections.sort(items, new TLEComparator());
			} else {
				items.add(0, t);
			}
			processNewTLE(t);
			this.notifyDataSetChanged();			
		}
	}
	
	/**
	 * Adds timeline elements to the adapter
	 * 
	 * @param elements The list of elements to be added
	 */
	public void addAllAsFirst(List<TimelineElement> elements) {
		for (TimelineElement t : elements) {
			if (!available.containsKey(t.getID())) {
				processNewTLE(t);
				items.add(t);				
				Collections.sort(items, new TLEComparator());
				this.notifyDataSetChanged();
			}
		}
	}
	
	/**
	 * Processes a TimelineElement
	 * 
	 * @param tle The TimelineElement to be processed
	 */
	private void processNewTLE(TimelineElement tle) {
		available.put(tle.getID(), tle);
		TimelineActivity.addToAvailableTLE(tle);
		if (tle instanceof Tweet) {
			for (Hashtag ht : ((Tweet) tle).entities.hashtags) {
				Geotweeter.getInstance().getKnownHashtags().add("#" + ht.text);
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		
		TimelineElement tle = (TimelineElement) items.get(position);
		boolean is_retweet = false;
		String retweeter = "";
		
		View v = convertView;
		if (v == null) {
//			LayoutInflater vi = (LayoutInflater) Geotweeter.getInstance().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.timeline_element, null);
		}

		View container = v.findViewById(R.id.container);
		
		int drawableID = tle.getBackgroundDrawableID(Geotweeter.getInstance().useDarkTheme());
		container.setBackgroundResource(drawableID);
		
		LinearLayout buttons = (LinearLayout) v.findViewById(R.id.action_buttons);
		
		buttons.setVisibility(View.GONE);
		buttons.removeAllViews();
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (tle.getClass() == Tweet.class) {
			Tweet t = (Tweet) tle;
			if (t.retweeted_status != null) {
				tle = t.retweeted_status;
				is_retweet = true;
				retweeter = t.getSenderScreenName();
				t = t.retweeted_status;
			}
			
			createButton(buttons, inflater, Constants.ICON_REPLY, v.getResources().getString(R.string.action_reply));
			if (t.isOwnMessage()) {
				createButton(buttons, inflater, Constants.ICON_DELETE, v.getResources().getString(R.string.action_delete));
			} else {
				if (t.isRetweetable()) {
					createButton(buttons, inflater, Constants.ICON_RETWEET, v.getResources().getString(R.string.action_retweet));
				}
				createButton(buttons, inflater, Constants.ICON_FAV, v.getResources().getString(R.string.action_fav));
			}
			if (t.isConversationEndpoint()) {
				createButton(buttons, inflater, Constants.ICON_CONV, v.getResources().getString(R.string.action_conv));
			}
			
		}

		
		if (tle.getClass() == DirectMessage.class) {
			createButton(buttons, inflater, Constants.ICON_REPLY, v.getResources().getString(R.string.action_reply));
			createButton(buttons, inflater, Constants.ICON_CONV, v.getResources().getString(R.string.action_conv));
			if (tle.isOwnMessage()) {
				createButton(buttons, inflater, Constants.ICON_DELETE, v.getResources().getString(R.string.action_delete));
			}
		}
				
		if (tle != null) {
			FrameLayout mapContainer = (FrameLayout) v.findViewById(R.id.map_fragment_container);
			mapContainer.removeAllViews();
			
			View mapAndControls = v.findViewById(R.id.map_and_controls);
			mapAndControls.setVisibility(View.GONE);
			
			TextView txtView;
			String text;
			
			txtView = (TextView) v.findViewById(R.id.txtSender);
			if (txtView != null) {
				txtView.setText(tle.getSenderString());
			}
			
			txtView = (TextView) v.findViewById(R.id.txtTimestamp);
			if (txtView != null) {
				txtView.setText(tle.getDateString());
			}
			
			txtView = (TextView) v.findViewById(R.id.txtText);
			if (txtView != null) { 
				txtView.setText(tle.getTextForDisplay());
			}
			
			txtView = (TextView) v.findViewById(R.id.txtPlace);
			if (txtView != null) {
				text = tle.getPlaceString();
				if (text == null) {
					txtView.setVisibility(View.GONE);
				} else {
					txtView.setText(text);
					txtView.setVisibility(View.VISIBLE);
				}
			}
			
			txtView = (TextView) v.findViewById(R.id.txtSource);
			if (txtView != null) {
				if (is_retweet) {
					text = "RT by " + retweeter;
				} else {
					text = tle.getSourceText();
				}
				if (text == null) {
					txtView.setVisibility(View.GONE);
				} else {
					txtView.setText(text);
					txtView.setVisibility(View.VISIBLE);
				}
			}
			
			AsyncImageView img = (AsyncImageView) v.findViewById(R.id.avatar_image);
			if (img != null) {
				if (tle.getAvatarSource() == null) {
					img.setImageResource(R.drawable.loading_image);
				} else {
					Geotweeter.getInstance().getBackgroundImageLoader().displayImage(tle.getAvatarSource(), img, true);
				}
			}
			
			List<Pair<URL, URL>> media_list = tle.getMediaList();
			LinearLayout picPreviews = (LinearLayout) v.findViewById(R.id.picPreviews);
			if (media_list != null && !media_list.isEmpty() && context.getSharedPreferences(Constants.PREFS_APP, 0).getBoolean("pref_show_img_previews", false)) {
				picPreviews.removeAllViews();
				picPreviews.setVisibility(View.VISIBLE);
				for (final Pair<URL, URL> url_pair : media_list) {
					ImageView thumbnail = new ImageView(context);
					picPreviews.addView(thumbnail);
					thumbnail.setFocusable(false);
					thumbnail.getLayoutParams().width = 75;
					thumbnail.getLayoutParams().height = 75;
					thumbnail.setScaleType(ScaleType.CENTER_CROP);
					thumbnail.setImageResource(R.drawable.ic_launcher);
					Geotweeter.getInstance().getBackgroundImageLoader().displayImage(url_pair.first.toString(), thumbnail, false);
					thumbnail.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							showOverlay(url_pair.second.toString());
						}
					});
				}
			} else {
				picPreviews.setVisibility(View.GONE);
			}
		}
//		v.findViewById(R.id.tweet_element).setOnClickListener(new OnClickListener());
		return v;	
	}

	private void createButton(LinearLayout buttons, LayoutInflater inflater, CharSequence icon, CharSequence desc) {
		LinearLayout button = (LinearLayout) inflater.inflate(R.layout.action_button, null);
		TextView iconView = (TextView) button.findViewById(R.id.action_icon);
		iconView.setTypeface(tf);
		iconView.setText(icon);
		TextView description = (TextView) button.findViewById(R.id.action_description);
		description.setText(desc);
		buttons.addView(button);
		LinearLayout.LayoutParams params = (LayoutParams) button.getLayoutParams();
		params.weight = 1.0f;
		button.setLayoutParams(params);
	}
	
	/**
	 * Shows a full size image overlay of the given URL
	 * 
	 * @param url The URL of the image to be shown
	 */
	protected void showOverlay(String url) {
		ImageView img_overlay = null;
		try {
			if (!TimelineActivity.getInstance().isVisible()) {
				return;
			}
			img_overlay = (ImageView) TimelineActivity.getInstance().findViewById(R.id.img_overlay);
		} catch (NullPointerException e) {
			return;
		}
		Geotweeter.getInstance().getBackgroundImageLoader().displayImage(url, img_overlay, false);
		img_overlay.setVisibility(View.VISIBLE);
	}

	/**
	 * Returns the adapter's item list
	 * 
	 * @return The adapter's item list
	 */
	public List<TimelineElement> getItems() {
		return items;
	}


}
