package de.geotweeter.apiconn;

import java.net.MalformedURLException;
import java.net.URL;

import android.util.Pair;
import de.geotweeter.timelineelements.Url;

public class MobytoApiAccess {

	public static Pair<URL, URL> getUrlPair(Url url) throws MalformedURLException {
		return new Pair<URL, URL>(new URL(url.expanded_url + ":square"), 
				new URL(url.expanded_url + ":full"));
	}

}
