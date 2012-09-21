package de.fabianonline.geotweeter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import de.fabianonline.geotweeter.timelineelements.DirectMessage;
import de.fabianonline.geotweeter.timelineelements.FavoriteEvent;
import de.fabianonline.geotweeter.timelineelements.FollowEvent;
import de.fabianonline.geotweeter.timelineelements.ListMemberAddedEvent;
import de.fabianonline.geotweeter.timelineelements.ListMemberRemovedEvent;
import de.fabianonline.geotweeter.timelineelements.NotShownEvent;
import de.fabianonline.geotweeter.timelineelements.TimelineElement;
import de.fabianonline.geotweeter.timelineelements.Tweet;

public class Utils {
	public static int countChars(String str) {
		str = str.trim();
		int length = str.length();
		Pattern p = Pattern.compile("((https?)://[^\n\r ]+)");
		Matcher m = p.matcher(str);
		while(m.find()) {
			/* Original-Link-Länge abziehen und die gekürzten-20-Zeichen hinzuaddieren. */
			length = length - m.group(1).length() + 20;
			/* War es ein https-Link, packen wir noch ein Zeichen für den gekürzten https-Link dazu. */
			if (m.group(2).equalsIgnoreCase("https")) length++;
		}
		
		return length;
	}
	
	public static TimelineElement jsonObjectToNativeObject(JSONObject obj) {
		try {
			if (obj.containsKey("text") && obj.containsKey("recipient")) {
				return JSON.toJavaObject(obj, DirectMessage.class);
			}
			if (obj.containsKey("direct_message")) {
				return JSON.toJavaObject(obj.getJSONObject("direct_message"), DirectMessage.class);
			}
			if (obj.containsKey("text")) {
				return JSON.parseObject(obj.toJSONString(), Tweet.class);
			}
			if (obj.containsKey("event")) {
				String event_type = obj.getString("event");
				if (event_type.equals("follow")) {
					return JSON.toJavaObject(obj, FollowEvent.class);
				}
				if (event_type.equals("favorite")) {
					return JSON.toJavaObject(obj, FavoriteEvent.class);
				}
				if (event_type.equals("list_member_added")) {
					return JSON.toJavaObject(obj, ListMemberAddedEvent.class);
				}
				if (event_type.equals("list_member_removed")) {
					return JSON.toJavaObject(obj, ListMemberRemovedEvent.class);
				}
				if (event_type.equals("block") || event_type.equals("user_update") || event_type.equals("unfavorite")) {
					return JSON.toJavaObject(obj, NotShownEvent.class);
				}
			}
			throw new JSONException("blubb");
		} catch (JSONException e) {
			Log.e("Utils.jsonObjectToNativeObject", "Exception: " + e.toString());
			Log.e("Utils.jsonObjectToNativeObject", "JSON: " + obj.toJSONString());
			return null;
		}
	}
}
