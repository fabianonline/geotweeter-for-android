package de.geotweeter.timelineelements;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import android.util.Pair;
import de.geotweeter.Geotweeter;
import de.geotweeter.R;
import de.geotweeter.apiconn.twitter.Tweet;
import de.geotweeter.apiconn.twitter.User;

public class ProtectedAccount extends Tweet {

	private static final long serialVersionUID = 9026285847318235644L;

	public ProtectedAccount(User user) {
		this.user = user;
	}

	public int getBackgroundDrawableID(boolean getDarkVersion) {
		return getDarkVersion ? R.drawable.listelement_background_dark_unread
				: R.drawable.listelement_background_light_unread;
	}

	public String getSenderName() {
		return user.getScreenName();
	}

	public String getSenderScreenName() {
		return user.getScreenName();
	}

	public String getDateString() {
		return "";
	}

	public String getPlaceString() {
		return null;
	}

	public String getSourceText() {
		return null;
	}

	public String getAvatarSource() {
		return user.getAvatarSource();
	}

	public List<Pair<URL, URL>> getMediaList() {
		return new LinkedList<Pair<URL, URL>>();
	}

	public String getTextForDisplay() {
		return Geotweeter.getInstance().getString(R.string.user_protected);
	}

}
