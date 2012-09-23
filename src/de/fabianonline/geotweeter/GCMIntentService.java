package de.fabianonline.geotweeter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
	private static final String LOG = "GCMBaseIntentService";

	@Override
	protected void onError(Context context, String error_id) {
		Log.d(LOG, "onError - " + error_id);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.d(LOG, "onMessage");
	}

	@Override
	protected void onRegistered(Context context, String reg_id) {
		Log.d(LOG, "onRegistered - " + reg_id);
		TimelineActivity.reg_id = reg_id;
	}

	@Override
	protected void onUnregistered(Context context, String reg_id) {
		Log.d(LOG, "onUnregistered - " + reg_id);
	}

}
