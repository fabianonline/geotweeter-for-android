package de.geotweeter;

import java.util.ArrayList;
import java.util.List;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.Pair;
import android.widget.RemoteViews;
import de.geotweeter.activities.NewTweetActivity;
import de.geotweeter.activities.TimelineActivity;
import de.geotweeter.services.NotificationDeleteReceiver;
import de.geotweeter.timelineelements.DirectMessage;
import de.geotweeter.timelineelements.TimelineElement;
import de.geotweeter.timelineelements.Tweet;

@ReportsCrashes(formKey                = "",
                formUri                = "" /* will be overwritten in constructor */,
                mode                   = ReportingInteractionMode.DIALOG,
                resToastText           = R.string.crash_toast_text,
                resDialogText          = R.string.crash_dialog_text,
                resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
                resDialogOkToast       = R.string.crash_dialog_ok_toast,
                logcatArguments        = {"-t", "200", "-v", "threadtime", "dalvikvm:s"},
                sharedPreferencesName  = Constants.PREFS_APP,
                additionalSharedPreferences = {Constants.PREFS_APP, Constants.PREFS_ACCOUNTS},
                excludeMatchingSharedPreferencesKeys = {"^access_"})
public class Geotweeter extends Application {
	private static final String LOG = "Geotweeter";
	private static Geotweeter instance;
	public List<Pair<TimelineElement, String>> notifiedElements = new ArrayList<Pair<TimelineElement, String>>();
	private boolean darkTheme;
	
	@Override
	public void onCreate() {
		Log.d(LOG, "onCreate is running");
		instance = this;
		darkTheme = getSharedPreferences(Constants.PREFS_APP, 0).getBoolean("pref_dark_theme", false);
		ACRAConfiguration config = ACRA.getNewDefaultConfig(this);
		config.setFormUri(Utils.getProperty("crashreport.server.url") + "/send");
		ACRA.setConfig(config);
		ACRA.init(this);
		super.onCreate();
	}
	
	public boolean useDarkTheme() {
		return darkTheme;
	}

	public static Geotweeter getInstance() {
		return instance;
	}
	
	public void updateNotification(boolean vibrateAndPlaySoundAndStuff) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		SharedPreferences pref = getSharedPreferences(Constants.PREFS_APP, 0);
		List<Pair<TimelineElement, String>> allNotifications = notifiedElements;
		
		if (notifiedElements.isEmpty()) {
			notificationManager.cancel(Constants.NOTIFICATION_ID);
			return;
		}
		
		TimelineElement t = allNotifications.get(0).first;
		String type = allNotifications.get(0).second;
		int countTweets = 0;
		int countDMs = 0;
		for (Pair<TimelineElement, String> t1 : allNotifications) {
			if (t1.first instanceof DirectMessage) countDMs++;
			else if (t1.first instanceof Tweet) countTweets++;
		}
		
		
		CharSequence notificationText = t.getNotificationText(type);
		@SuppressWarnings("deprecation")
		Notification notification = new Notification(R.drawable.ic_launcher, notificationText, System.currentTimeMillis());
		Intent notificationIntent;
		if (t instanceof Tweet || t instanceof DirectMessage) {
			notificationIntent = new Intent(this, NewTweetActivity.class);
			notificationIntent.putExtra("de.geotweeter.reply_to_tweet", t);
		} else {
			notificationIntent = new Intent(this, TimelineActivity.class);
		}
		PendingIntent contentIntent = PendingIntent.getActivity(this, (int)System.currentTimeMillis(), notificationIntent, 0);
		
		RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_with_small_text);
		if (allNotifications.size() == 1) {
			contentView.setTextViewText(R.id.txtTitle, t.getNotificationContentTitle(type));
			contentView.setTextViewText(R.id.txtText, t.getNotificationContentText(type));
		} else {
			/* Mehrere Notifications */
			StringBuilder sb = new StringBuilder();
			if (countTweets == 1) {
				sb.append(Utils.getString(R.string.notification_text_mention_single));
			} else if (countTweets > 1) {
				sb.append(Utils.formatString(R.string.notification_text_mention_plural, countTweets));
			}
			if (countDMs > 0) {
				if (sb.length() > 0) sb.append(", ");
				if (countDMs == 1) {
					sb.append(Utils.getString(R.string.notification_text_dm_single));
				} else {
					sb.append(Utils.formatString(R.string.notification_text_dm_plural, countDMs));
				}
			}
			
			contentView.setTextViewText(R.id.txtTitle, sb.toString() + ". " + Utils.formatString(R.string.notification_text_newest, t.getNotificationContentTitle(type)));
			
			contentView.setTextViewText(R.id.txtText, t.getNotificationContentText(type));
			
			addExtendableNotification(allNotifications, notification, sb);
		}
		notification.contentView = contentView;
		notification.contentIntent = contentIntent;
		
		notification.deleteIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, NotificationDeleteReceiver.class), 0);
		
		if (vibrateAndPlaySoundAndStuff) {
			if (pref.getBoolean("pref_notifications_sound_enabled", true)) {
				String sound = pref.getString("pref_notifications_sound_ringtone", "DEFAULT_RINGTONE_URI");
				if (! "".equals(sound)) {
					notification.sound = Uri.parse(sound);
				}
			}
			
			if (pref.getBoolean("pref_notifications_vibration_enabled", true)) {
				notification.vibrate = new long[] {0, 200, 500, 200};
			}
			
			if (pref.getBoolean("pref_notifications_led_enabled", false)) {
				notification.ledARGB = (int) Long.parseLong(pref.getString("pref_notifications_led_color", "4278190335"));
				notification.ledOnMS = 200;
				notification.ledOffMS = 1000;
				notification.flags |= Notification.FLAG_SHOW_LIGHTS;
			}
		}
		notificationManager.notify(Constants.NOTIFICATION_ID, notification);
	}
	
	@TargetApi(16)
	private void addExtendableNotification(List<Pair<TimelineElement, String>> allNotifications, Notification notification, StringBuilder sb) {
		if (Build.VERSION.SDK_INT >= 16) {
			/* Jelly-Bean or better. Extendable Notifications are available. So let's use them! */
			RemoteViews extendedView = new RemoteViews(getPackageName(), R.layout.notification_extended);
			extendedView.setTextViewText(R.id.txtTitle, sb.toString());
			extendedView.removeAllViews(R.id.notification_extended_linear_layout);
			for (Pair<TimelineElement, String> pair : allNotifications) {
				RemoteViews elementView = new RemoteViews(getPackageName(), R.layout.notification_extended_tweet);
				elementView.setTextViewText(R.id.notification_extended_tweet_title, pair.first.getNotificationContentTitle(pair.second));
				elementView.setTextViewText(R.id.notification_extended_tweet_text, pair.first.getNotificationContentText(pair.second));
				
				Intent notificationIntent;
				if (pair.first instanceof Tweet || pair.first instanceof DirectMessage) {
					notificationIntent = new Intent(this, NewTweetActivity.class);
					notificationIntent.putExtra("de.geotweeter.reply_to_tweet", pair.first);
				} else {
					notificationIntent = new Intent(this, TimelineActivity.class);
				}
				PendingIntent tweetIntent = PendingIntent.getActivity(this, (int)System.currentTimeMillis(), notificationIntent, 0);
				
				elementView.setOnClickPendingIntent(R.id.notification_extended_tweet_container, tweetIntent);
				extendedView.addView(R.id.notification_extended_linear_layout, elementView);
			}
			notification.bigContentView = extendedView;
		}
	}
}
