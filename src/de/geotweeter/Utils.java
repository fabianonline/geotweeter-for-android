package de.geotweeter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;

import android.util.Log;
import android.view.View;
import android.webkit.WebView.FindListener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

import de.geotweeter.R;
import de.geotweeter.activities.TimelineActivity;
import de.geotweeter.exceptions.UnknownJSONObjectException;
import de.geotweeter.timelineelements.DirectMessage;
import de.geotweeter.timelineelements.FavoriteEvent;
import de.geotweeter.timelineelements.FollowEvent;
import de.geotweeter.timelineelements.ListMemberAddedEvent;
import de.geotweeter.timelineelements.ListMemberRemovedEvent;
import de.geotweeter.timelineelements.NotShownEvent;
import de.geotweeter.timelineelements.TimelineElement;
import de.geotweeter.timelineelements.Tweet;

public class Utils {
	private static int mainSpinnerDisplays = 0;
	private static final String LOG = "Utils";
	
	public static int countChars(String str) {
		str = str.trim();
		int length = str.length();
		Pattern p = Pattern.compile("((https?)://[^\n\r ]+)");
		Matcher m = p.matcher(str);
		while(m.find()) {
			/* Original-Link-Länge abziehen und die gekürzten-20-Zeichen hinzuaddieren. */
			length = length - m.group(1).length() + 20;
			/* War es ein https-Link, packen wir noch ein Zeichen für den gekürzten https-Link dazu. */
			if (m.group(2).equalsIgnoreCase("https")) { 
				length++;
			}
		}
		
		return length;
	}
	
	public static TimelineElement jsonToNativeObject(String json) throws JSONException, UnknownJSONObjectException {
		JSONObject obj = JSON.parseObject(json, Feature.DisableCircularReferenceDetect);
		
		if (obj.containsKey("text") && obj.containsKey("recipient")) {
			return JSON.parseObject(json, DirectMessage.class);
		}
		if (obj.containsKey("direct_message")) {
			return JSON.parseObject(obj.getJSONObject("direct_message").toJSONString(), DirectMessage.class);
		}
		if (obj.containsKey("text")) {
			return JSON.parseObject(json, Tweet.class);
		}
		if (obj.containsKey("event")) {
			String event_type = obj.getString("event");
			if (event_type.equals("follow")) {
				return JSON.parseObject(json, FollowEvent.class);
			}
			if (event_type.equals("favorite")) {
				return JSON.parseObject(json, FavoriteEvent.class);
			}
			if (event_type.equals("list_member_added")) {
				return JSON.parseObject(json, ListMemberAddedEvent.class);
			}
			if (event_type.equals("list_member_removed")) {
				return JSON.parseObject(json, ListMemberRemovedEvent.class);
			}
			if (event_type.equals("block") || event_type.equals("user_update") || event_type.equals("unfavorite")) {
				return JSON.parseObject(json, NotShownEvent.class);
			}
		}
		throw new UnknownJSONObjectException();
	}
	
	public static void showMainSpinner() {
		mainSpinnerDisplays++;
		TimelineActivity ta = TimelineActivity.getInstance();
		if (ta != null) {
			final View spinner = ta.findViewById(R.id.spinnerMain);
			final View refreshButton = ta.findViewById(R.id.btnRefresh);
			ta.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (spinner != null) {
						spinner.setVisibility(View.VISIBLE);
					}
					if (refreshButton != null) {
						refreshButton.setVisibility(View.INVISIBLE);
					}
				}
			});
		}
	}
	
	public static void hideMainSpinner() {
		mainSpinnerDisplays--;
		if (mainSpinnerDisplays <= 0) {
			mainSpinnerDisplays = 0;
			TimelineActivity ta = TimelineActivity.getInstance();
			if (ta != null) {
				final View spinner = ta.findViewById(R.id.spinnerMain);
				final View refreshButton = ta.findViewById(R.id.btnRefresh);
				ta.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (spinner != null) {
							spinner.setVisibility(View.INVISIBLE);
						}
						if (refreshButton != null) {
							refreshButton.setVisibility(View.VISIBLE);
						}
					}
				});
			}
		}
	}
}