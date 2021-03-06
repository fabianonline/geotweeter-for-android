package de.geotweeter;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import de.geotweeter.Constants.ActionType;
import de.geotweeter.Constants.TLEType;
import de.geotweeter.activities.TimelineActivity;
import de.geotweeter.apiconn.twitter.DirectMessage;
import de.geotweeter.apiconn.twitter.Tweet;
import de.geotweeter.timelineelements.TLEComparator;
import de.geotweeter.timelineelements.TimelineElement;

public class TimelineElementAdapter extends ArrayAdapter<TimelineElement>
		implements Observer {
	private TimelineElementList items;
	private final Context context;
	private Typeface tf;
	private LayoutInflater inflater;
	private final Animation ani;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            Current context
	 * @param textViewResourceId
	 *            The resource ID for a layout file containing a TextView to use
	 *            when instantiating views.
	 * @param objects
	 *            The objects to be represented
	 */
	public TimelineElementAdapter(Context context, int textViewResourceId,
			TimelineElementList objects) {
		super(context, textViewResourceId, objects.getList());
		ani = AnimationUtils.loadAnimation(context, R.animator.image_click);
		if (objects != null) {
			Collections.sort(objects.getList(), new TLEComparator());
		}
		this.items = objects;
		this.context = context;
		tf = Typeface.createFromAsset(context.getAssets(), "fonts/Entypo.otf");
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		items.addObserver(this);
	}

	/**
	 * @deprecated Use {@link TimelineElementList#addAsFirst(TimelineElement)}
	 *             instead Adds a timeline element to the adapter
	 * 
	 * @param t
	 *            The element to be added
	 */
	// TODO delete
	public void addAsFirst(TimelineElement t) {
		items.addAsFirst(t);
		notifyDataSetChanged();
	}

	/**
	 * @deprecated Use {@link TimelineElementList#addAllAsFirst(List, boolean)}
	 *             instead Adds timeline elements to the adapter
	 * 
	 * @param elements
	 *            The list of elements to be added
	 */
	// TODO delete
	public void addAllAsFirst(List<TimelineElement> elements, boolean sort) {
		items.addAllAsFirst(elements, sort);
		notifyDataSetChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public View getView(int position, View convertView, ViewGroup parent) {

		TimelineElement tle = (TimelineElement) items.getList().get(position);
		boolean is_retweet = false;
		String retweeter = "";

		View v = convertView;
		if (v == null) {
			v = inflater.inflate(R.layout.timeline_element, null);
		}

		View container = v.findViewById(R.id.container);

		TLEType type = tle.getType();
		final boolean darkTheme = Geotweeter.getInstance().useDarkTheme();
		int drawableID = TimelineElement.getBackgroundGradient(type, darkTheme);
		int backgroundColorId = context.getResources().getColor(
				TimelineElement.getBackgroundColor(type, darkTheme));

		container.setBackgroundResource(drawableID);

		if (tle.getClass() == Tweet.class) {
			Tweet t = (Tweet) tle;
			if (t.retweeted_status != null && !t.maskRetweetedStatus) {
				tle = t.retweeted_status;
				is_retweet = true;
				retweeter = t.getSenderScreenName();
				t = t.retweeted_status;
			}
		}

		if (TimelineActivity.getInstance() != null) {
			LinearLayout buttons = (LinearLayout) v
					.findViewById(R.id.action_buttons);
			buttons.setBackgroundColor(backgroundColorId);
			buttons.setVisibility(View.GONE);
			buttons.removeAllViews();

			if (tle.getClass() == Tweet.class) {
				Tweet t = (Tweet) tle;

				createButton(buttons, ActionType.REPLY, tle);
				if (t.isOwnMessage()) {
					createButton(buttons, ActionType.DELETE, tle);
				} else {
					if (t.isRetweetable()) {
						createButton(buttons, ActionType.RETWEET, tle);
					}
					if (t.favorited) {
						createButton(buttons, ActionType.DEFAV, tle);
					} else {
						createButton(buttons, ActionType.FAV, tle);
					}
				}
				if (t.isConversationEndpoint()) {
					createButton(buttons, ActionType.CONV, tle);
				}

			}

			if (tle.getClass() == DirectMessage.class) {
				createButton(buttons, ActionType.REPLY, tle);
				createButton(buttons, ActionType.CONV, tle);
				createButton(buttons, ActionType.DELETE, tle);
			}
		}

		if (tle != null) {
			FrameLayout mapContainer = (FrameLayout) v
					.findViewById(R.id.map_fragment_container);
			mapContainer.removeAllViews();

			View mapAndControls = v.findViewById(R.id.map_and_controls);
			mapAndControls.setVisibility(View.GONE);
			mapAndControls.setBackgroundColor(backgroundColorId);

			TextView txtView;
			String text;

			txtView = (TextView) v.findViewById(R.id.txtSender);
			if (txtView != null) {
				txtView.setText(tle.getTitleForDisplay());
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

			AsyncImageView img = (AsyncImageView) v
					.findViewById(R.id.avatar_image);
			if (img != null) {
				if (tle.getAvatarSource() == null) {
					img.setImageResource(R.drawable.loading_image);
				} else {
					Geotweeter.getInstance().getBackgroundImageLoader()
							.displayImage(tle.getAvatarSource(), img, true);
				}

				if (TimelineActivity.getInstance() != null) {
					final String senderScreenName = tle.getSenderScreenName();

					img.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							v.startAnimation(ani);
							TimelineActivity.getInstance().userClick(
									senderScreenName);
						}
					});

				}
			}

			List<Pair<URL, URL>> media_list = tle.getMediaList();
			LinearLayout picPreviews = (LinearLayout) v
					.findViewById(R.id.picPreviews);
			if (media_list != null
					&& !media_list.isEmpty()
					&& context.getSharedPreferences(Constants.PREFS_APP, 0)
							.getBoolean("pref_show_img_previews", false)) {
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
					Geotweeter
							.getInstance()
							.getBackgroundImageLoader()
							.displayImage(url_pair.first.toString(), thumbnail,
									false);
					thumbnail.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							v.startAnimation(ani);
							showOverlay(url_pair.second.toString());
						}
					});
				}
			} else {
				picPreviews.setVisibility(View.GONE);
			}
		}
		return v;
	}

	/**
	 * Creates a tweet specific action button
	 * 
	 * @param buttons
	 *            Layout parent
	 * @param type
	 *            Button type to be generated
	 * @param tle
	 */
	private void createButton(LinearLayout buttons, final ActionType type,
			final TimelineElement tle) {
		CharSequence icon = null, desc = null;
		Resources res = buttons.getResources();
		switch (type) {
		case CONV:
			icon = Constants.ICON_CONV;
			desc = res.getString(R.string.action_conv);
			break;
		case DELETE:
			icon = Constants.ICON_DELETE;
			desc = res.getString(R.string.action_delete);
			break;
		case FAV:
			icon = Constants.ICON_FAV;
			desc = res.getString(R.string.action_fav);
			break;
		case DEFAV:
			icon = Constants.ICON_DEFAV;
			desc = res.getString(R.string.action_defav);
			break;
		case REPLY:
			icon = Constants.ICON_REPLY;
			desc = res.getString(R.string.action_reply);
			break;
		case RETWEET:
			icon = Constants.ICON_RETWEET;
			desc = res.getString(R.string.action_retweet);
			break;
		}

		LinearLayout button = (LinearLayout) inflater.inflate(
				R.layout.action_button, null);
		TextView iconView = (TextView) button.findViewById(R.id.action_icon);
		iconView.setTypeface(tf);
		iconView.setText(icon);
		TextView description = (TextView) button
				.findViewById(R.id.action_description);
		description.setText(desc);
		buttons.addView(button);
		LinearLayout.LayoutParams params = (LayoutParams) button
				.getLayoutParams();
		params.weight = 1.0f;
		params.setMargins(Utils.convertDipToPixel(3), 0,
				Utils.convertDipToPixel(3), Utils.convertDipToPixel(3));
		button.setLayoutParams(params);

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TimelineActivity.getInstance().actionClick(type, tle);
			}
		});
	}

	/**
	 * Shows a full size image overlay of the given URL
	 * 
	 * @param url
	 *            The URL of the image to be shown
	 */
	protected void showOverlay(String url) {
		ImageView img_overlay = null;
		try {
			if (!TimelineActivity.getInstance().isVisible()) {
				return;
			}
			img_overlay = (ImageView) TimelineActivity.getInstance()
					.findViewById(R.id.img_overlay);
		} catch (NullPointerException e) {
			return;
		}
		Geotweeter.getInstance().getBackgroundImageLoader()
				.displayImage(url, img_overlay, false);
		img_overlay.setVisibility(View.VISIBLE);
	}

	/**
	 * Returns the adapter's item list
	 * 
	 * @return The adapter's item list
	 */
	public List<TimelineElement> getItems() {
		return items.getList();
	}

	/**
	 * @deprecated Use
	 *             {@link TimelineElementList#replace(TimelineElement, TimelineElement)}
	 *             instead Replaces an element in the timeline element list
	 * 
	 * @param oldTle
	 * @param newTle
	 */
	// TODO delete
	public void replace(TimelineElement oldTle, TimelineElement newTle) {
		items.replace(oldTle, newTle);
		notifyDataSetChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable observable, Object data) {
		notifyDataSetChanged();
	}

}
