package de.geotweeter.apiconn;

import java.net.MalformedURLException;
import java.net.URL;

import android.util.Pair;
import de.geotweeter.apiconn.twitter.Url;

public class MobytoApiAccess {

	/**
	 * Generates URLs for the thumbnail and full size images of a Moby.to
	 * picture
	 * 
	 * @param url
	 *            URL from a tweet
	 * @return Pair of thumbnail and full size URL
	 * @throws MalformedURLException
	 */
	public static Pair<URL, URL> getUrlPair(Url url)
			throws MalformedURLException {
		return new Pair<URL, URL>(new URL(url.expanded_url + ":square"),
				new URL(url.expanded_url + ":full"));
	}

}
