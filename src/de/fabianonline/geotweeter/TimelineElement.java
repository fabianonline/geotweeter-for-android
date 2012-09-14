package de.fabianonline.geotweeter;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;

public class TimelineElement {
	private String sender;
	private String text;
	
	public TimelineElement(String sender, String text) {
		this.sender = sender;
		this.text = text;
	}
	
	public String getSender() { return sender; }
	public String getText() { return text; }
	public String getTextForDisplay() {
		String text = "<strong><a href='https://twitter.com'>" + sender + "</a></strong> " + this.text;
		return text;
	}
}
