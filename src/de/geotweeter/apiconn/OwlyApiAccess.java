package de.geotweeter.apiconn;

import java.net.MalformedURLException;
import java.net.URL;

import android.util.Pair;
import de.geotweeter.Utils;
import de.geotweeter.apiconn.twitter.Url;

/**
 * Provides access to images hosted at ow.ly
 * 
 * @author Lutz Krumme (@el_emka)
 * 
 */
public class OwlyApiAccess {

	/**
	 * Generates URLs for the thumbnail and full size images of an ow.ly picture
	 * 
	 * @param url
	 *            URL from a tweet
	 * @return Pair of thumbnail and full size URL
	 * @throws MalformedURLException
	 */
	public static Pair<URL, URL> getUrlPair(Url url)
			throws MalformedURLException {
		URL screenUrl = new URL(url.expanded_url);
		String imageId = Utils.substringAfter(screenUrl.getPath(), "/i/");
		return new Pair<URL, URL>(new URL("http://static.ow.ly/photos/thumb/"
				+ imageId + ".jpg"), new URL(
				"http://static.ow.ly/photos/original/" + imageId + ".jpg"));
	}

}
