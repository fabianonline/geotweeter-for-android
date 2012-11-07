package de.geotweeter.apiconn;

import java.net.MalformedURLException;
import java.net.URL;

import android.util.Pair;
import de.geotweeter.timelineelements.Url;

public class PlixiApiAccess {

	public static Pair<URL, URL> getUrlPair(Url url) throws MalformedURLException {
		return new Pair<URL, URL>(new URL("http://api.plixi.com/api/tpapi.svc/imagefromurl?url=" + url.expanded_url + "&size=thumbnail"), 
				new URL("http://api.plixi.com/api/tpapi.svc/imagefromurl?url=" + url.expanded_url + "&size=big"));
	}

}
