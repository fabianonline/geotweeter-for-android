package de.fabianonline.geotweeter;

import org.json.JSONException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gcm.GCMBaseIntentService;

import de.fabianonline.geotweeter.activities.NewTweetActivity;
import de.fabianonline.geotweeter.activities.TimelineActivity;
import de.fabianonline.geotweeter.exceptions.UnknownJSONObjectException;
import de.fabianonline.geotweeter.timelineelements.DirectMessage;
import de.fabianonline.geotweeter.timelineelements.TimelineElement;
import de.fabianonline.geotweeter.timelineelements.Tweet;

public class GCMIntentService extends GCMBaseIntentService {
	private static final String LOG = "GCMIntentService";

	@Override
	protected void onError(Context context, String error_id) {
		Log.d(LOG, "onError - " + error_id);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onMessage(Context context, Intent intent) {
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
		Intent notificationIntent;
		if (t instanceof Tweet || t instanceof DirectMessage) {
			notificationIntent = new Intent(this, NewTweetActivity.class);
			notificationIntent.putExtra("de.fabianonline.geotweeter.reply_to_tweet", t);
		} else {
			notificationIntent = new Intent(this, TimelineActivity.class);
		}
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, (int)System.currentTimeMillis(), notificationIntent, 0);
		
		RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_with_small_text);
		contentView.setTextViewText(R.id.txtTitle, contentTitle);
		contentView.setTextViewText(R.id.txtText, contentText);
		notification.contentView = contentView;
		//notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notification.contentIntent = contentIntent;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.vibrate = new long[] {0, 200, 500, 200};
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
