package de.fabianonline.geotweeter;

import java.util.HashMap;
import java.util.Random;

import org.json.JSONException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

import de.fabianonline.geotweeter.activities.NewTweetActivity;
import de.fabianonline.geotweeter.exceptions.UnknownJSONObjectException;
import de.fabianonline.geotweeter.timelineelements.TimelineElement;

public class GCMIntentService extends GCMBaseIntentService {
	private static final String LOG = "GCMIntentService";

	@Override
	protected void onError(Context context, String error_id) {
		Log.d(LOG, "onError - " + error_id);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.d(LOG, "Type: " + intent.getExtras().getString("type"));
		Log.d(LOG, "Data: " + intent.getExtras().getString("data"));
		Log.d(LOG, "onMessage");
		TimelineElement t;
		try {
			t = Utils.jsonToNativeObject(intent.getExtras().getString("data"));
		} catch (JSONException e) {
			return;
		} catch (UnknownJSONObjectException e) {
			return;
		}
		if (!t.showNotification()) {
			return;
		}
		int id = t.hashCode();
		String type = intent.getExtras().getString("type");
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		CharSequence notificationText = t.getNotificationText(type);
		Notification notification = new Notification(R.drawable.ic_launcher, notificationText, System.currentTimeMillis());
		CharSequence contentTitle = t.getNotificationContentTitle(type);
		CharSequence contentText = t.getNotificationContentText(type);
		Intent notificationIntent = new Intent(this, NewTweetActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notificationManager.notify(id, notification);
	}

	@Override
	protected void onRegistered(Context context, String reg_id) {
		Log.d(LOG, "onRegistered - " + reg_id);
		TimelineActivity.reg_id = reg_id;
		for (Account a : Account.all_accounts) {
			a.registerForGCMMessages();
		}
	}

	@Override
	protected void onUnregistered(Context context, String reg_id) {
		Log.d(LOG, "onUnregistered - " + reg_id);
	}

}
