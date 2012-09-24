package de.fabianonline.geotweeter.timelineelements;

import java.util.Date;

import de.fabianonline.geotweeter.R;
import de.fabianonline.geotweeter.User;

public class DirectMessage extends Tweet {
	private User sender;
	private User recipient;

	@Override
	public String getTextForDisplay() {
		// TODO Auto-generated method stub
		return "DM";
	}

	@Override
	public CharSequence getSourceText() {
		// TODO Auto-generated method stub
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
		return recipient.getAvatarSource();
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
	public int getBackgroundDrawableID() {
		return R.drawable.listelement_background_dm;
	}
}