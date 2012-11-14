package de.geotweeter.timelineelements;

import de.geotweeter.R;

public class ProtectedTweet extends Tweet {

	private static final long serialVersionUID = -2236090642550922993L;

	public int getBackgroundDrawableID(boolean getDarkVersion) {
		return getDarkVersion ? R.drawable.listelement_background_dark_unread : R.drawable.listelement_background_light_unread;
	}
}
