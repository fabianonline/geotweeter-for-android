package de.geotweeter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.apache.http.conn.ssl.SSLSocketFactory;

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
import android.util.Pair;
import android.widget.RemoteViews;
import de.geotweeter.activities.NewTweetActivity;
import de.geotweeter.activities.TimelineActivity;
import de.geotweeter.apiconn.twitter.DirectMessage;
import de.geotweeter.apiconn.twitter.Tweet;
import de.geotweeter.apiconn.twitter.User;
import de.geotweeter.services.NotificationDeleteReceiver;
import de.geotweeter.timelineelements.TimelineElement;

@ReportsCrashes(formKey = "", formUri = "", mode = ReportingInteractionMode.DIALOG, resToastText = R.string.crash_toast_text, resDialogText = R.string.crash_dialog_text, resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, resDialogOkToast = R.string.crash_dialog_ok_toast, logcatArguments = {
		"-t", "200", "-v", "threadtime", "dalvikvm:s" }, sharedPreferencesName = Constants.PREFS_APP, additionalSharedPreferences = {
		Constants.PREFS_APP, Constants.PREFS_ACCOUNTS }, excludeMatchingSharedPreferencesKeys = { "^access_" })
public class Geotweeter extends Application {
	private static final String LOG = "Geotweeter";
	private static Geotweeter instance;
	public static Configuration config;

	public List<Pair<TimelineElement, String>> notifiedElements = new ArrayList<Pair<TimelineElement, String>>();
	private boolean darkTheme;
	private BackgroundImageLoader backgroundImageLoader;
	private AccountManager accountManager;

	private Set<String> autoCompletionContent;

	@Override
	public void onCreate() {
		Debug.log(LOG, "onCreate is running");
		instance = this;
		refreshTheme();
		SSLSocketFactory.getSocketFactory().setHostnameVerifier(
				SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		ACRAConfiguration acraConfig = ACRA.getNewDefaultConfig(this);
		acraConfig.setFormUri(Utils.getProperty("crashreport.server.url")
				+ "/send");
		ACRA.setConfig(acraConfig);
		ACRA.init(this);

		backgroundImageLoader = new BackgroundImageLoader(
				getApplicationContext());

		autoCompletionContent = Collections
				.synchronizedSet(new HashSet<String>());

		// TODO: Move Account creation to Geotweeter.java
		accountManager = new AccountManager();
		accountManager.init();

		config = (Configuration) Utils.readObjectFromFile(
				getApplicationContext(), Constants.PREFS_CONFIG);

		if (config == null) {
			config = new Configuration();
		}

		super.onCreate();
	}

	/**
	 * Returns whether the dark theme is used or not
	 * 
	 * @return true, when the dark theme is used
	 */
	public boolean useDarkTheme() {
		return darkTheme;
	}

	// public void changeTheme(boolean darkTheme) {
	// this.darkTheme = darkTheme;
	// }
	//
	public void refreshTheme() {
		darkTheme = getSharedPreferences(Constants.PREFS_APP, 0).getBoolean(
				"pref_dark_theme", false);
		if (darkTheme) {
			// setTheme(R.style.GeotweeterThemeDark);
			getTheme().applyStyle(R.style.GeotweeterThemeDark, true);
		} else {
			// setTheme(R.style.GeotweeterThemeLight);
			getTheme().applyStyle(R.style.GeotweeterThemeLight, true);
		}
	}

	/**
	 * Returns the instance of the Geotweeter-App
	 * 
	 * @return the instance of Geotweeter
	 */
	public static Geotweeter getInstance() {
		return instance;
	}

	/**
	 * Returns the current BackgroundImageLoader instance
	 * 
	 * @return BackgroundImageLoader instance
	 */
	public BackgroundImageLoader getBackgroundImageLoader() {
		return backgroundImageLoader;
	}

	/**
	 * Returns the AccountManager of the App
	 * 
	 * @return AccountManager accountmanager
	 */
	public AccountManager getAccountManager() {
		return accountManager;
	}

	/**
	 * Returns a list of all authorized user accounts
	 * 
	 * @return The list of all authorized user accounts
	 */
	public List<User> getAuthUsers() {
		List<User> result = null;

		SharedPreferences sp = getSharedPreferences(Constants.PREFS_APP, 0);
		String accountString = sp.getString("accounts", null);

		if (accountString != null) {
			String[] accounts = accountString.split(" ");
			result = User.getPersistentData(getApplicationContext(), accounts);
		}

		return result;
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
			if (t1.first instanceof DirectMessage)
				countDMs++;
			else if (t1.first instanceof Tweet)
				countTweets++;
		}

		CharSequence notificationText = t.getNotificationText(type);
		@SuppressWarnings("deprecation")
		Notification notification = new Notification(R.drawable.ic_launcher,
				notificationText, System.currentTimeMillis());
		Intent notificationIntent;
		if (t instanceof Tweet || t instanceof DirectMessage) {
			notificationIntent = new Intent(this, NewTweetActivity.class);
			notificationIntent.putExtra("de.geotweeter.reply_to_tweet", t);
		} else {
			notificationIntent = new Intent(this, TimelineActivity.class);
		}
		PendingIntent contentIntent = PendingIntent.getActivity(this,
				(int) System.currentTimeMillis(), notificationIntent, 0);

		RemoteViews contentView = new RemoteViews(getPackageName(),
				R.layout.notification_with_small_text);
		if (allNotifications.size() == 1) {
			contentView.setTextViewText(R.id.txtTitle,
					t.getNotificationContentTitle(type));
			contentView.setTextViewText(R.id.txtText,
					t.getNotificationContentText(type));
		} else {
			/* Mehrere Notifications */
			StringBuilder sb = new StringBuilder();
			if (countTweets == 1) {
				sb.append(Utils
						.getString(R.string.notification_text_mention_single));
			} else if (countTweets > 1) {
				sb.append(Utils.formatString(
						R.string.notification_text_mention_plural, countTweets));
			}
			if (countDMs > 0) {
				if (sb.length() > 0)
					sb.append(", ");
				if (countDMs == 1) {
					sb.append(Utils
							.getString(R.string.notification_text_dm_single));
				} else {
					sb.append(Utils.formatString(
							R.string.notification_text_dm_plural, countDMs));
				}
			}

			contentView.setTextViewText(
					R.id.txtTitle,
					sb.toString()
							+ ". "
							+ Utils.formatString(
									R.string.notification_text_newest,
									t.getNotificationContentTitle(type)));

			contentView.setTextViewText(R.id.txtText,
					t.getNotificationContentText(type));

			addExtendableNotification(allNotifications, notification, sb);
		}
		notification.contentView = contentView;
		notification.contentIntent = contentIntent;

		notification.deleteIntent = PendingIntent.getBroadcast(this, 0,
				new Intent(this, NotificationDeleteReceiver.class), 0);

		if (vibrateAndPlaySoundAndStuff) {
			if (pref.getBoolean("pref_notifications_sound_enabled", true)) {
				String sound = pref.getString(
						"pref_notifications_sound_ringtone",
						"DEFAULT_RINGTONE_URI");
				if (!"".equals(sound)) {
					notification.sound = Uri.parse(sound);
				}
			}

			if (pref.getBoolean("pref_notifications_vibration_enabled", true)) {
				notification.vibrate = new long[] { 0, 200, 500, 200 };
			}

			if (pref.getBoolean("pref_notifications_led_enabled", false)) {
				notification.ledARGB = (int) Long.parseLong(pref.getString(
						"pref_notifications_led_color", "4278190335"));
				notification.ledOnMS = 200;
				notification.ledOffMS = 1000;
				notification.flags |= Notification.FLAG_SHOW_LIGHTS;
			}
		}
		notificationManager.notify(Constants.NOTIFICATION_ID, notification);
	}

	@TargetApi(16)
	private void addExtendableNotification(
			List<Pair<TimelineElement, String>> allNotifications,
			Notification notification, StringBuilder sb) {
		if (Build.VERSION.SDK_INT >= 16) {
			/*
			 * Jelly-Bean or better. Extendable Notifications are available. So
			 * let's use them!
			 */
			RemoteViews extendedView = new RemoteViews(getPackageName(),
					R.layout.notification_extended);
			extendedView.setTextViewText(R.id.txtTitle, sb.toString());
			extendedView
					.removeAllViews(R.id.notification_extended_linear_layout);
			for (Pair<TimelineElement, String> pair : allNotifications) {
				RemoteViews elementView = new RemoteViews(getPackageName(),
						R.layout.notification_extended_tweet);
				elementView.setTextViewText(
						R.id.notification_extended_tweet_title,
						pair.first.getNotificationContentTitle(pair.second));
				elementView.setTextViewText(
						R.id.notification_extended_tweet_text,
						pair.first.getNotificationContentText(pair.second));

				Intent notificationIntent;
				if (pair.first instanceof Tweet
						|| pair.first instanceof DirectMessage) {
					notificationIntent = new Intent(this,
							NewTweetActivity.class);
					notificationIntent.putExtra("de.geotweeter.reply_to_tweet",
							pair.first);
				} else {
					notificationIntent = new Intent(this,
							TimelineActivity.class);
				}
				PendingIntent tweetIntent = PendingIntent
						.getActivity(this, (int) System.currentTimeMillis(),
								notificationIntent, 0);

				elementView
						.setOnClickPendingIntent(
								R.id.notification_extended_tweet_container,
								tweetIntent);
				extendedView.addView(R.id.notification_extended_linear_layout,
						elementView);
			}
			notification.bigContentView = extendedView;
		}
	}

	public Set<String> getAutoCompletionContent() {
		return autoCompletionContent;
	}
}
