package de.geotweeter.apiconn;

import java.net.MalformedURLException;
import java.net.URL;

import android.util.Pair;
import de.geotweeter.timelineelements.Url;

public class InstagramApiAccess {

	public static Pair<URL, URL> getUrlPair(Url url) throws MalformedURLException {
		URL screen_url = new URL(url.expanded_url);
		return new Pair<URL, URL>(new URL("http://instagr.am/" + screen_url.getPath() + "/media/?size=t"), 
				new URL("http://instagr.am/" + screen_url.getPath() + "/media/?size=l"));
	}
	
}
