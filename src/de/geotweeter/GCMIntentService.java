package de.geotweeter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import com.alibaba.fastjson.JSONException;
import com.google.android.gcm.GCMBaseIntentService;

import de.geotweeter.activities.TimelineActivity;
import de.geotweeter.exceptions.UnknownJSONObjectException;
import de.geotweeter.timelineelements.TimelineElement;

public class GCMIntentService extends GCMBaseIntentService {
	private static final String LOG = "GCMIntentService";

	@Override
	protected void onError(Context context, String error_id) {
		Log.d(LOG, "onError - " + error_id);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.d(LOG, "onMessage");
		
		/* Run checks to see if the user will want to see this notification. */
		SharedPreferences pref = getSharedPreferences(Constants.PREFS_APP, 0);
		if (!pref.getBoolean("pref_notifications_enabled", true)) {
			return;
		}
		
		if (pref.getBoolean("pref_notifications_silent_time_enabled", false)) {
			long start = pref.getLong("pref_notifications_silent_time_start", -1);
			long end   = pref.getLong("pref_notifications_silent_time_end",   -1);
			long now   = System.currentTimeMillis() % (24*60*60*1000);
			Log.d(LOG, "" + start + " " + end + " " + now);
			if (start >= 0 && end >= 0) {
				if (start > end) {
					/* Start ist vor Mitternach, Ende danach. */
					if (now >= start || now <= end) {
						return;
					}
				} else {
					/* Start und Ende sind am gleichen Tag. */
					if (now >= start && now <= end) {
						return;
					}
				}
			}
		}
		
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

		String type = intent.getExtras().getString("type");
		
		if ("mention".equals(type) && !pref.getBoolean("pref_notifications_types_mentions", true)) {
			return;
		} else if ("dm".equals(type) && !pref.getBoolean("pref_notifications_types_direct_messages", true)) {
			return;
		} else if ("favorite".equals(type) && !pref.getBoolean("pref_notifications_types_favorites", false)) {
			return;
		} else if ("retweet".equals(type) && !pref.getBoolean("pref_notifications_types_retweets", false)) {
			return;
		}
		
		
		/* Add the tweet to the notifications list. */
		List<Pair<TimelineElement, String>> allNotifications = ((Geotweeter) getApplication()).notifiedElements;
		allNotifications.add(0, new Pair<TimelineElement, String>(t, type));
		
		((Geotweeter) getApplication()).updateNotification();
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
