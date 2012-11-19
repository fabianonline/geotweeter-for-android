package de.geotweeter.services;

import de.geotweeter.Geotweeter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationDeleteReceiver extends BroadcastReceiver {
	private static final String LOG = "NotificationDeleteReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(LOG, "onReceive called");
		((Geotweeter)(context.getApplicationContext())).notifiedElements.clear();
	}
}
