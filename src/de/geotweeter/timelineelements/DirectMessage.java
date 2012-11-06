package de.geotweeter.timelineelements;

import de.geotweeter.R;
import de.geotweeter.User;
import de.geotweeter.activities.TimelineActivity;

public class DirectMessage extends Tweet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2657023974332061942L;
	public User sender;
	public User recipient;

	@Override
	public String getSourceText() {
		return null;
	}
	
	public void setSender(User sender) {
		if(User.all_users.containsKey(sender.id)) {
			this.sender = User.all_users.get(sender.id);
		} else {
			User.all_users.put(sender.id, sender);
			this.sender = sender;
		}
	}
	
	public void setRecipient(User recipient) {
		if(User.all_users.containsKey(recipient.id)) {
			this.recipient = User.all_users.get(recipient.id);
		} else {
			User.all_users.put(recipient.id, recipient);
			this.recipient = recipient;
		}
	}
	
	@Override
	public String getAvatarSource() {
		return sender.getAvatarSource();
	}
	
	public String getSenderScreenName() {
		return sender.getScreenName();
	}
	
	@Override
	public boolean showNotification() {
		return true;
	}
	
	@Override
	public String getNotificationText(String type) {
		return "DM von " + sender.screen_name;
	}
	
	@Override
	public String getNotificationContentTitle(String type) {
		return "DM von " + sender.screen_name;
	}
	
	@Override
	public String getNotificationContentText(String type) {
		return text;
	}
	
	@Override
	public int getBackgroundDrawableID(boolean getDarkVersion) {
		return getDarkVersion ? R.drawable.listelement_background_dark_dm : R.drawable.listelement_background_light_dm;
	}
	
	@Override
	public boolean showWithFilter(String filter) {
		return false;
	}
	
	@Override
	public String getSenderString() {
		if (sender.id == TimelineActivity.current_account.getUser().id) {
			return "an " + recipient.getScreenName();
		} else {
			return "von " + sender.getScreenName();
		}
	}
	
	@Override
	public String getPlaceString() {
		return null;
	}
}
