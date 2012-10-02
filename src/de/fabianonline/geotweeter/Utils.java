package de.fabianonline.geotweeter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

import de.fabianonline.geotweeter.exceptions.UnknownJSONObjectException;
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

	public static int stringToSoundResourceID(String string) {
		if (string.equals("meep")) {
			return R.raw.sound_meep;
		}
		if (string.equals("plang")) {
			return R.raw.sound_plang;
		}
		return R.raw.sound_pling;
	}
}
