package de.geotweeter.apiconn;

import java.net.MalformedURLException;
import java.net.URL;

import android.util.Pair;
import de.geotweeter.apiconn.twitter.Url;

public class PlixiApiAccess {

	/**
	 * Generates URLs for the thumbnail and full size images of a Plixi picture
	 * 
	 * @param url URL from a tweet
	 * @return Pair of thumbnail and full size URL
	 * @throws MalformedURLException
	 */
	public static Pair<URL, URL> getUrlPair(Url url) throws MalformedURLException {
		return new Pair<URL, URL>(new URL("http://api.plixi.com/api/tpapi.svc/imagefromurl?url=" + url.expanded_url + "&size=thumbnail"), 
				new URL("http://api.plixi.com/api/tpapi.svc/imagefromurl?url=" + url.expanded_url + "&size=big"));
	}

}
