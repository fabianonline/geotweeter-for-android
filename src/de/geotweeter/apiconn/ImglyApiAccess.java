package de.geotweeter.apiconn;

import java.net.MalformedURLException;
import java.net.URL;

import android.util.Pair;
import de.geotweeter.apiconn.twitter.Url;

/**
 * Provides access to img.ly
 * 
 * @author Lutz Krumme (@el_emka)
 * 
 */
public class ImglyApiAccess {

	/**
	 * Generates URLs for the thumbnail and full size images of an img.ly
	 * picture
	 * 
	 * @param url
	 *            URL from a tweet
	 * @return Pair of thumbnail and full size URL
	 * @throws MalformedURLException
	 */
	public static Pair<URL, URL> getUrlPair(Url url)
			throws MalformedURLException {
		URL screen_url = new URL(url.expanded_url);
		return new Pair<URL, URL>(new URL("http://img.ly/show/mini"
				+ screen_url.getPath()), new URL("http://img.ly/show/full"
				+ screen_url.getPath()));
	}

}
