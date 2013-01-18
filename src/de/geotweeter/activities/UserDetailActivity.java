package de.geotweeter.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import de.geotweeter.Constants;
import de.geotweeter.Constants.ActionType;
import de.geotweeter.R;
import de.geotweeter.User;
import de.geotweeter.Utils;

public class UserDetailActivity extends Activity {

	private final String LOG = "UserDetailActivity";

	private String url = "";
	private LayoutInflater inflater;
	private Typeface tf;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(LOG, "Create user details");
		Utils.setDesign(this);
		super.onCreate(savedInstanceState);

		inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		tf = Typeface.createFromAsset(this.getAssets(), "fonts/Entypo.otf");

		User user = (User) getIntent().getSerializableExtra("user");

		TextView screenName = (TextView) findViewById(R.id.user_screen_name);
		screenName.setText(user.screen_name);
		TextView realName = (TextView) findViewById(R.id.user_real_name);
		realName.setTag(user.name);
		if (!user.url.equals("")) {
			TextView urlIcon = (TextView) findViewById(R.id.user_url_icon);
			urlIcon.setText(Constants.ICON_URL);
			TextView urlView = (TextView) findViewById(R.id.user_url);
			urlView.setText(user.url);
			this.url = user.url;
			urlView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					openURL(url);
				}
			});
		}
		if (!user.location.equals("")) {
			TextView locationIcon = (TextView) findViewById(R.id.user_location_icon);
			locationIcon.setText(Constants.ICON_LOCATION);
			TextView location = (TextView) findViewById(R.id.user_location);
			location.setText(user.location);
		}
		if (!user.description.equals("")) {
			TextView descriptionIcon = (TextView) findViewById(R.id.user_bio_icon);
			descriptionIcon.setText(Constants.ICON_BIO);
			TextView description = (TextView) findViewById(R.id.user_bio);
			description.setText(user.description);
		}

		setContentView(R.layout.user_detail);
	}

	private void createButton(LinearLayout buttons, final ActionType type) {
		CharSequence icon = null, desc = null;
		Resources res = buttons.getResources();
		switch (type) {
		case FOLLOW:
			icon = Constants.ICON_FOLLOW;
			desc = res.getString(R.string.action_follow);
			break;
		case UNFOLLOW:
			icon = Constants.ICON_UNFOLLOW;
			desc = res.getString(R.string.action_unfollow);
			break;
		case SEND_DM:
			icon = Constants.ICON_DM;
			desc = res.getString(R.string.action_send_dm);
			break;
		case BLOCK:
			icon = Constants.ICON_BLOCK;
			desc = res.getString(R.string.action_block);
			break;
		case MARK_AS_SPAM:
			icon = Constants.ICON_SPAM;
			desc = res.getString(R.string.action_mark_as_spam);
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
				actionClick(type);
			}
		});
	}

	protected void actionClick(ActionType type) {
		// TODO Auto-generated method stub

	}

	/**
	 * Opens a given URL
	 * 
	 * @param url
	 *            The URL to be opened by the operating system
	 * @return true if successful
	 */
	protected boolean openURL(String url) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
		return true;
	}

}
