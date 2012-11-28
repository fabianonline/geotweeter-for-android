package de.geotweeter.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import de.geotweeter.Geotweeter;

public class NotificationDeleteReceiver extends BroadcastReceiver {
	private static final String LOG = "NotificationDeleteReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(LOG, "onReceive called");
		((Geotweeter)(context.getApplicationContext())).notifiedElements.clear();
	}
}
