package de.fabianonline.geotweeter.activities;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import de.fabianonline.geotweeter.R;
import de.fabianonline.geotweeter.TimelineActivity;
import de.fabianonline.geotweeter.Utils;
import de.fabianonline.geotweeter.exceptions.TweetSendException;

public class NewTweetActivity extends Activity {
	private static final String LOG = "NewTweetActivity";
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
				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpslistener);
				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, gpslistener);
			}
		}
	}
	
	protected class GPSCoordsListener implements LocationListener {
		private String currentProvider = null;
		private NewTweetActivity activity;
		public GPSCoordsListener(NewTweetActivity a) { activity = a; }
		public void onLocationChanged(Location new_location) {
			Log.d("GPSCoordsListener", "Provider: " + new_location.getProvider() + "  Accuracy: " + new_location.getAccuracy());
			if (location!=null) Log.d("GPSCoordsListener", "New==GPS="+String.valueOf(new_location.getProvider().equals(LocationManager.GPS_PROVIDER)) + ", Old==Network="+String.valueOf(location.getProvider().equals(LocationManager.NETWORK_PROVIDER)));
			/* Wir nehmen die aktuellen Koordinaten, wenn es
			 *   a) die ersten Koordinaten sind,
			 *   b) die bisherigen Koordinaten nur Netzwerk-genau waren
			 *   c) Die Accurracy gleich oder besser ist.
			 */
			if (location==null || new_location.getAccuracy() <= location.getAccuracy() || (new_location.getProvider().equals(LocationManager.GPS_PROVIDER) && location.getProvider().equals(LocationManager.NETWORK_PROVIDER))) {
						location = new_location;
						Log.d("GPSCoordsListener", "Koordinaten sind besser.");
			}
			/*
			 * Ab einer Genauigkeit von 16 Meter (Zahl auf gut Glück bestimmt) nehmen wir den Wert und hören mit GPS auf.
			 */
			if (new_location.getAccuracy() <= 16 ) {
				Toast.makeText(getBaseContext(), "Genaue Position erhalten.", Toast.LENGTH_SHORT).show();
				activity.lm.removeUpdates(this);
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
