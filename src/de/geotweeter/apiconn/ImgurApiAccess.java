package de.geotweeter.apiconn;

import java.net.MalformedURLException;
import java.net.URL;

import android.util.Pair;
import de.geotweeter.Utils;
import de.geotweeter.apiconn.twitter.Url;

public class ImgurApiAccess {

	/**
	 * Generates URLs for the thumbnail and full size images of a Imgur picture
	 * 
	 * @param url
	 *            URL from a tweet
	 * @return Pair of thumbnail and full size URL
	 * @throws MalformedURLException
	 */
	public static Pair<URL, URL> getUrlPair(Url url)
			throws MalformedURLException {
		String expanded_url = url.expanded_url;
		if (expanded_url.contains("/gallery/")) {
			expanded_url = Utils.substringBefore(expanded_url, "gallery/")
					+ Utils.substringAfter(expanded_url, "gallery/");
		}
		URL screen_url = new URL(expanded_url);

		return new Pair<URL, URL>(new URL("http://i.imgur.com/"
				+ screen_url.getPath() + "s.png"), new URL(
				"http://i.imgur.com/" + screen_url.getPath() + ".png"));
	}

}
