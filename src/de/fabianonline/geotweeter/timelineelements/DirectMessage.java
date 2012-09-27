package de.fabianonline.geotweeter.timelineelements;

import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;

import de.fabianonline.geotweeter.R;
import de.fabianonline.geotweeter.User;
import de.fabianonline.geotweeter.activities.TimelineActivity;

public class DirectMessage extends Tweet {
	private User sender;
	private User recipient;

	@Override
	public String getTextForDisplay() {
		if (sender.id == TimelineActivity.current_account.getUser().id) {
			return "<strong>an " + recipient.getScreenName() + "</strong> " + text;
		} else {
			return "<strong>von " + sender.getScreenName() + "</strong> " + text;
		}
	}

	@Override
	public CharSequence getSourceText() {
		return new SimpleDateFormat("dd.MM. HH:mm").format(created_at);
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
	public int getBackgroundDrawableID() {
		return R.drawable.listelement_background_dm;
	}
}
