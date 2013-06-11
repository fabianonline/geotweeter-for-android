package de.geotweeter.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import de.geotweeter.Debug;
import de.geotweeter.Geotweeter;

public class NotificationDeleteReceiver extends BroadcastReceiver {
	private static final String LOG = "NotificationDeleteReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Debug.log(LOG, "onReceive called");
		((Geotweeter) (context.getApplicationContext())).notifiedElements
				.clear();
	}
}
