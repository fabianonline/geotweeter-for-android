package de.fabianonline.geotweeter.activities;

import de.fabianonline.geotweeter.R;
import de.fabianonline.geotweeter.TimelineActivity;
import de.fabianonline.geotweeter.exceptions.TweetSendException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class NewTweetActivity extends Activity {
	protected LocationManager lm = null;
	protected Location location = null;
	protected GPSCoordsListener gpslistener = null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_tweet);
		
		((EditText)findViewById(R.id.tweet_text)).addTextChangedListener(new RemainingCharUpdater(this));
		((ToggleButton)findViewById(R.id.btnGeo)).setOnCheckedChangeListener(new GPSToggleListener(this));
		((Button)findViewById(R.id.btnSend)).setOnClickListener(new SendTweetListener(this));
	}
	
	protected class RemainingCharUpdater implements TextWatcher {
		private Activity activity;
		public RemainingCharUpdater(Activity a) { activity = a; }
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		public void afterTextChanged(Editable s) {
			TextView t = (TextView) activity.findViewById(R.id.textCharsRemaining);
			int remaining = 140-s.length();
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
				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpslistener);
			}
		}
	}
	
	protected class GPSCoordsListener implements LocationListener {
		private String currentProvider = null;
		private NewTweetActivity activity;
		public GPSCoordsListener(NewTweetActivity a) { activity = a; }
		public void onLocationChanged(Location loc) {
			activity.location = loc;
			if (currentProvider == LocationManager.NETWORK_PROVIDER) {
				Toast.makeText(getBaseContext(), "Grobe Position erhalten.", Toast.LENGTH_SHORT).show();
				activity.lm.removeUpdates(this);
				activity.lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
			} else if (loc.getAccuracy() <= 10 ) {
				Toast.makeText(getBaseContext(), "Genaue Position erhalten.", Toast.LENGTH_SHORT).show();
				activity.lm.removeUpdates(this);
			}
		}
		
		public void onProviderDisabled(String arg0) {
			currentProvider = null;
		}
		
		public void onProviderEnabled(String prov) {
			currentProvider = prov;
		}

		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}

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
						TimelineActivity.current_account.sendTweet(text, location);
					} catch (TweetSendException e) {
						e.printStackTrace();
						return;
					} finally {
						dialog.dismiss();
					}
					activity.finish();
					if (gpslistener!=null && activity.lm!=null) activity.lm.removeUpdates(gpslistener);
				}
			}).start();
		}
	}
}
