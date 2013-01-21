package de.geotweeter.apiconn;

import java.net.MalformedURLException;
import java.net.URL;

import android.util.Pair;
import de.geotweeter.apiconn.twitter.Url;

public class InstagramApiAccess {

	/**
	 * Generates URLs for the thumbnail and full size images of a Instagram picture
	 * 
	 * @param url URL from a tweet
	 * @return Pair of thumbnail and full size URL
	 * @throws MalformedURLException
	 */
	public static Pair<URL, URL> getUrlPair(Url url) throws MalformedURLException {
		URL screen_url = new URL(url.expanded_url);
		return new Pair<URL, URL>(new URL("http://instagr.am/" + screen_url.getPath() + "/media/?size=t"), 
				new URL("http://instagr.am/" + screen_url.getPath() + "/media/?size=l"));
	}
	
}
