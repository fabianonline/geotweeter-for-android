package de.geotweeter;

import java.io.Serializable;

import android.location.Location;

public class SendableTweet implements Serializable {
	private static final long serialVersionUID = -2671715618529883934L;
	public String text;
	public Account account;
//	public String imagePath = null;
	public String[] images = null;
	public int remainingImages = 0;
	public Location location = null;
	public Long reply_to_status_id = null;
	public String dmRecipient = null;
	
	public String imageHoster;
	public long imageSize;
	
	public SendableTweet(Account account, String text) {
		this.text = text;
		this.account = account;
	}
}
