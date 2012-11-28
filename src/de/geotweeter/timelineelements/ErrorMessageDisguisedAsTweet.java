package de.geotweeter.timelineelements;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import android.util.Pair;
import de.geotweeter.Geotweeter;
import de.geotweeter.R;

public class ErrorMessageDisguisedAsTweet extends Tweet {
	private final int message_resource_id;
	
	private static final long serialVersionUID = -9192797986117349031L;
	
	public ErrorMessageDisguisedAsTweet(int message_resource_id) {
		this.message_resource_id = message_resource_id;
	}
	
	public int getBackgroundDrawableID(boolean getDarkVersion) {
		return getDarkVersion ? R.drawable.listelement_background_dark_unread : R.drawable.listelement_background_light_unread;
	}
	
	public String getSenderString() { return ""; }
	public String getDateString() { return ""; }
	public String getPlaceString() { return null; }
	public String getSourceText() { return null; }
	public String getAvatarSource() { return null; }
	public List<Pair<URL, URL>> getMediaList() { return new LinkedList<Pair<URL,URL>>(); }
	
	public String getTextForDisplay() {
		return Geotweeter.getContext().getString(message_resource_id);
	}
}
