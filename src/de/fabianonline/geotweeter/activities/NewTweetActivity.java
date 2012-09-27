package de.fabianonline.geotweeter.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import de.fabianonline.geotweeter.Account;
import de.fabianonline.geotweeter.R;
import de.fabianonline.geotweeter.TimelineElementAdapter;
import de.fabianonline.geotweeter.User;
import de.fabianonline.geotweeter.Utils;
import de.fabianonline.geotweeter.exceptions.TweetSendException;
import de.fabianonline.geotweeter.timelineelements.DirectMessage;
import de.fabianonline.geotweeter.timelineelements.TimelineElement;
import de.fabianonline.geotweeter.timelineelements.Tweet;

public class NewTweetActivity extends Activity {
	private static final String LOG = "NewTweetActivity";
	protected LocationManager lm = null;
	protected Location location = null;
	protected GPSCoordsListener gpslistener = null;
	private long reply_to_id;
	
	private Account currentAccount;
	private HashMap<View, Account> viewToAccounts;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_tweet);
		
		EditText editTweetText = ((EditText)findViewById(R.id.tweet_text));
		
		editTweetText.addTextChangedListener(new RemainingCharUpdater(this));
		((ToggleButton)findViewById(R.id.btnGeo)).setOnCheckedChangeListener(new GPSToggleListener(this));
		((Button)findViewById(R.id.btnSend)).setOnClickListener(new SendTweetListener(this));
		
		Intent i = getIntent();
		if (i != null && i.getExtras() != null) {
			TimelineElement elm = (TimelineElement) i.getExtras().getSerializable("de.fabianonline.geotweeter.reply_to_tweet");
			String reply_string = "";
			if (elm instanceof DirectMessage) {
				reply_string = "d " + elm.getSenderScreenName() + " ";
			} else if (elm instanceof Tweet) {
				reply_to_id = elm.getID();
				reply_string = "@" + elm.getSenderScreenName() + " ";
			}
			editTweetText.setText(reply_string);
			editTweetText.setSelection(reply_string.length());
			
			ListView l = (ListView) findViewById(R.id.timeline);
			TimelineElementAdapter ta = new TimelineElementAdapter(this, R.layout.timeline_element, 
																    new ArrayList<TimelineElement>());
			l.setAdapter(ta);
			ta.add(elm);
		}
		
		/* Accountauswahl */
		List<Account> accounts = Account.all_accounts;
		LinearLayout lin = (LinearLayout) findViewById(R.id.linLayAccounts);
		
		currentAccount = TimelineActivity.current_account;
		
		viewToAccounts = new HashMap<View, Account>();
		for (Account account : accounts) {
			User user = account.getUser();
			ImageButton img = new ImageButton(this);
			img.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			TimelineActivity.background_image_loader.displayImage(user.getAvatarSource(), img);
			img.setPadding(5, 5, 5, 5);
			fooooo(img, currentAccount == account);
			img.setOnClickListener(new AccountChangerListener());
			lin.addView(img);
			viewToAccounts.put(img, account);
		}
		
	}
	
	protected void onPause() {
		super.onPause();
		/* Remove all GPSListeners. */
		if (gpslistener!=null && lm!=null) lm.removeUpdates(gpslistener);
	}
	
	protected class AccountChangerListener implements OnClickListener {
		public void onClick(View v) {
			Account acc = viewToAccounts.get(v);
			if(acc != currentAccount) {
				/* TODO: Hole oldView auf anderem Weg. Map, die in 2 Richtungen funktioniert */
				ImageButton oldView = (ImageButton) getViewFromAccount(currentAccount);
				fooooo(oldView, false);
				fooooo((ImageButton) v, true);
				currentAccount = acc;
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private void fooooo(ImageView v, boolean chosen) {
		int bgColor = Color.LTGRAY;
//		int bgColor = Color.WHITE;
//		int highlightColor = 0xFFFF0000;
		int highlightColor = 0xFF000000;
//		int highlightColor = 0xFFFFFFFF;
//		int highlightColor = 0xFFFFA500;
//		int highlightColor = 0xFF00FF00;
//		int highlightColor = 0xFFFFFF00;
		if(chosen) {
			v.setAlpha(255);
			GradientDrawable gradDraw = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, 
															 new int[] {highlightColor, bgColor});
			gradDraw.setGradientType(GradientDrawable.RADIAL_GRADIENT);
			gradDraw.setGradientRadius(40);
			v.setBackgroundDrawable(gradDraw);
		} else {
			v.setAlpha(50);
			v.setBackgroundColor(bgColor);
		}
	}
	
	private View getViewFromAccount(Account acc) {
		for (View v : viewToAccounts.keySet()) {
			if(viewToAccounts.get(v).equals(acc)) {
				return v;
			}
		}
		return null;
	}
	
	protected class RemainingCharUpdater implements TextWatcher {
		private Activity activity;
		public RemainingCharUpdater(Activity a) { activity = a; }
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		public void afterTextChanged(Editable s) {
			TextView t = (TextView) activity.findViewById(R.id.textCharsRemaining);
			int remaining = 140 - Utils.countChars(s.toString());
			t.setText(String.valueOf(remaining));
			if (remaining<0) {
				t.setTextColor(0xFFFF0000);
			} else {
				t.setTextColor(0xFF00FF00);
			}
		}
	}
	
	protected class GPSToggleListener implements OnCheckedChangeListener {
		private NewTweetActivity activity;
		
		public GPSToggleListener(NewTweetActivity a) { activity = a; }
		
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked==true) {
				lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
				gpslistener = new GPSCoordsListener(activity);
				List<String> providers = lm.getAllProviders();
				if(providers.contains(LocationManager.GPS_PROVIDER)) {
					lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpslistener);
				}
				if(providers.contains(LocationManager.NETWORK_PROVIDER)) {
					lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, gpslistener);
				}
			}
		}
	}
	
	protected class GPSCoordsListener implements LocationListener {
		private NewTweetActivity activity;
		
		public GPSCoordsListener(NewTweetActivity a) { activity = a; }
		
		public void onLocationChanged(Location new_location) {
			/* Wir nehmen die aktuellen Koordinaten, wenn es
			 *   a) die ersten Koordinaten sind oder
			 *   b) die bisherigen Koordinaten nur Netzwerk-genau waren
			 */
			if (location==null || 
					(new_location.getProvider().equals(LocationManager.GPS_PROVIDER) && location.getProvider().equals(LocationManager.NETWORK_PROVIDER))) {
						location = new_location;
			}
		}
		
		public void onProviderDisabled(String provider) {}
		public void onProviderEnabled(String provider) {}
		public void onStatusChanged(String provider, int new_status, Bundle extra) {}

	}
	
	public class SendTweetListener implements OnClickListener {
		private NewTweetActivity activity;
		private ProgressDialog dialog;
		Handler handler;
		
		public SendTweetListener(NewTweetActivity act) { activity = act; }
		public void onClick(View v) {
			final String text = ((TextView)activity.findViewById(R.id.tweet_text)).getText().toString().trim();
			handler = new Handler();
			dialog = ProgressDialog.show(activity, "Sending", "Sending tweet...");
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						currentAccount.sendTweet(text, location, reply_to_id);
					} catch (TweetSendException e) {
						e.printStackTrace();
						return;
					} finally {
						dialog.dismiss();
					}
					if (gpslistener!=null && lm!=null) lm.removeUpdates(gpslistener);
					activity.finish();
				}
			}).start();
		}
	}
}
