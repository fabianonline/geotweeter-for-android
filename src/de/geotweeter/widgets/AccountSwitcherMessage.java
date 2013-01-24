package de.geotweeter.widgets;

import de.geotweeter.widgets.AccountSwitcherRadioButton.Message;

public class AccountSwitcherMessage {
	public Message message;
	public int unreadCount;

	public AccountSwitcherMessage(Message message, int unreadCount) {
		this.message = message;
		this.unreadCount = unreadCount;
	}
}
