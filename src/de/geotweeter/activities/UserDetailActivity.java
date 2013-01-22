package de.geotweeter.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import de.geotweeter.AsyncImageView;
import de.geotweeter.Constants;
import de.geotweeter.Constants.ActionType;
import de.geotweeter.Geotweeter;
import de.geotweeter.R;
import de.geotweeter.Utils;
import de.geotweeter.apiconn.UserException;
import de.geotweeter.apiconn.twitter.Relationship;
import de.geotweeter.apiconn.twitter.User;
import de.geotweeter.exceptions.RelationshipException;

public class UserDetailActivity extends Activity {

	private final String LOG = "UserDetailActivity";

	private String url = "";
	private LayoutInflater inflater;
	private Typeface tf;
	private int tasksRunning = 0;
	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(LOG, "Create user details");
		Utils.setDesign(this);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.user_detail);

		String userName = (String) getIntent().getSerializableExtra("user");

		inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		tf = Typeface.createFromAsset(this.getAssets(), "fonts/Entypo.otf");
		
		new getUserDetailsTask().execute(userName);
		new getUserRelationShipTask().execute(userName);
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
		button.setVisibility(View.VISIBLE);

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

	public class getUserDetailsTask extends AsyncTask<String, Boolean, User> {

		protected void onPreExecute() {
			tasksRunning++;
			if (progressDialog == null) {
				progressDialog = ProgressDialog.show(UserDetailActivity.this,
						"", "Daten werden geladen...");
			}
		}

		@Override
		protected User doInBackground(String... params) {
			User user = null;
			try {
				user = TimelineActivity.current_account.getApi().getUser(
						params[0]);
			} catch (UserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return user;
		}

		protected void onPostExecute(User result) {
			tasksRunning--;
			if (tasksRunning == 0) {
				progressDialog.dismiss();
			}
			showUserDetails(result);
		}

	}

	public class getUserRelationShipTask extends
			AsyncTask<String, Boolean, Relationship> {

		protected void onPreExecute() {
			tasksRunning++;
			if (progressDialog == null) {
				progressDialog = ProgressDialog.show(UserDetailActivity.this,
						"", "Daten werden geladen...");
			}
		}

		@Override
		protected Relationship doInBackground(String... params) {
			Relationship relationship = null;
			try {
				relationship = TimelineActivity.current_account
						.getApi()
						.getRelationship(
								TimelineActivity.current_account.getUser().screen_name,
								params[0]);
			} catch (RelationshipException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return relationship;
		}

		protected void onPostExecute(Relationship relationship) {
			tasksRunning--;
			if (tasksRunning == 0) {
				progressDialog.dismiss();
			}
			showActionButtons(relationship);
		}

	}

	public void showUserDetails(User user) {
		AsyncImageView img = (AsyncImageView) findViewById(R.id.user_avatar);
		Geotweeter.getInstance().getBackgroundImageLoader()
				.displayImage(user.getAvatarSource(), img, true);

		TextView screenName = (TextView) findViewById(R.id.user_screen_name);
		screenName.setText(user.screen_name);
		TextView realName = (TextView) findViewById(R.id.user_real_name);
		realName.setText(user.name);
		screenName.setVisibility(View.VISIBLE);
		realName.setVisibility(View.VISIBLE);
		TextView urlIcon = (TextView) findViewById(R.id.user_url_icon);
		TextView urlView = (TextView) findViewById(R.id.user_url);
		if (user.url != null) {
			urlIcon.setTypeface(tf);
			urlIcon.setText(Constants.ICON_URL);
			urlView.setText(user.url);
			urlIcon.setVisibility(View.VISIBLE);
			urlView.setVisibility(View.VISIBLE);
			this.url = user.url;
			urlView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					openURL(url);
				}
			});
		}
		TextView locationIcon = (TextView) findViewById(R.id.user_location_icon);
		TextView location = (TextView) findViewById(R.id.user_location);
		if (!user.location.equals("")) {
			locationIcon.setTypeface(tf);
			locationIcon.setText(Constants.ICON_LOCATION);
			location.setText(user.location);
			locationIcon.setVisibility(View.VISIBLE);
			location.setVisibility(View.VISIBLE);
		}
		TextView descriptionIcon = (TextView) findViewById(R.id.user_bio_icon);
		TextView description = (TextView) findViewById(R.id.user_bio);
		if (!user.description.equals("")) {
			descriptionIcon.setTypeface(tf);
			descriptionIcon.setText(Constants.ICON_BIO);
			description.setText(user.description);
			descriptionIcon.setVisibility(View.VISIBLE);
			description.setVisibility(View.VISIBLE);
		}
	}

	public void showActionButtons(Relationship relationship) {
		LinearLayout actionButtons = (LinearLayout) findViewById(R.id.user_action_buttons);
		actionButtons.setVisibility(View.VISIBLE);

		if (relationship.target.followed_by) {
			createButton(actionButtons, ActionType.UNFOLLOW);
		} else {
			createButton(actionButtons, ActionType.FOLLOW);
		}

		if (relationship.source.can_dm) {
			createButton(actionButtons, ActionType.SEND_DM);
		}

		createButton(actionButtons, ActionType.BLOCK);
		createButton(actionButtons, ActionType.MARK_AS_SPAM);
	}

}
