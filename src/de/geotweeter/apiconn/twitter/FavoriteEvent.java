package de.geotweeter.apiconn.twitter;

import de.geotweeter.R;
import de.geotweeter.Utils;

public class FavoriteEvent extends Event {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5260534732247365283L;
	
	public Tweet target_object;

	@Override
	public String getTextForDisplay() {
		// TODO Remove if/else in later version?
		if (target_object != null) {
			return target_object.getTextForDisplay();
		} else {
			return null;
		}
	}
	
	@Override
	public String getTitleForDisplay() {
		return Utils.formatString(R.string.event_favorite_text2, source.getScreenName());
	}
}
