package de.geotweeter;

import android.location.Location;

public class SendableTweet {
	public String text;
	public Account account;
	public String imagePath = null;
	public Location location = null;
	public Long reply_to_status_id = null;
	
	public SendableTweet(Account account, String text) {
		this.text = text;
		this.account = account;
	}
}