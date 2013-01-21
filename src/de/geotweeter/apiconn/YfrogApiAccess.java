package de.geotweeter.apiconn;

import java.net.MalformedURLException;
import java.net.URL;

import android.util.Pair;
import de.geotweeter.apiconn.twitter.Url;

public class YfrogApiAccess {

	/**
	 * Generates URLs for the thumbnail and full size images of a Yfrog picture
	 * 
	 * @param url URL from a tweet
	 * @return Pair of thumbnail and full size URL
	 * @throws MalformedURLException
	 */
	public static Pair<URL, URL> getUrlPair(Url url) throws MalformedURLException {
		URL screen_url = new URL(url.expanded_url);
		return new Pair<URL, URL>(new URL("http://" + screen_url.getHost() + screen_url.getPath() + ":small"), 
				new URL("http://" + screen_url.getHost() + screen_url.getPath() + ":medium"));
	}

}
