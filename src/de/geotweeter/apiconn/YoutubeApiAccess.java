package de.geotweeter.apiconn;

import java.net.MalformedURLException;
import java.net.URL;

import android.util.Pair;
import de.geotweeter.Utils;
import de.geotweeter.timelineelements.Url;

public class YoutubeApiAccess {

	public static Pair<URL, URL> getUrlPair(Url url) throws MalformedURLException {
		URL screen_url = new URL(url.expanded_url);
		if (screen_url.getHost().endsWith("youtu.be")) {
			return new Pair<URL, URL>(new URL("http://img.youtube.com/vi" + screen_url.getPath() + "/default.jpg"), 
					new URL("http://img.youtube.com/vi" + screen_url.getPath() + "/maxresdefault.jpg"));
		}
		if (screen_url.getHost().contains("youtube.")) {
			if (screen_url.getPath().startsWith("/video/")) {
				String video_id = Utils.substringAfter(screen_url.getPath(), "video/");
				return new Pair<URL, URL>(new URL("http://img.youtube.com/vi/" + video_id + "/default.jpg"), 
						new URL("http://img.youtube.com/vi/" + video_id + "/maxresdefault.jpg"));				
			}
			if (screen_url.getPath().startsWith("/v/")) {
				String video_id = Utils.substringAfter(screen_url.getPath(), "v/");
				return new Pair<URL, URL>(new URL("http://img.youtube.com/vi/" + video_id + "/default.jpg"), 
						new URL("http://img.youtube.com/vi/" + video_id + "/maxresdefault.jpg"));								
			}
			String video_id = Utils.substringAfter(screen_url.getFile(), "v=");
			video_id = Utils.substringBefore(video_id, "&");
			return new Pair<URL, URL>(new URL("http://img.youtube.com/vi/" + video_id + "/default.jpg"), 
					new URL("http://img.youtube.com/vi/" + video_id + "/maxresdefault.jpg"));								
		}
		/* This can happen with links which do not point to a specific video */
		return null;
	}
}
