package de.fabianonline.geotweeter;

public class Tweet extends TimelineElement{
	public String text;
	public long id;
	public User user;
	public String created_at;
	public String source;
	
	public String getTextForDisplay() {
		return "<strong>" + user.getScreenName() + "</strong> " + text;
	}

	public CharSequence getSourceText() { return /*new SimpleDateFormat("dd.MM. HH:mm").format(created_at)*/ created_at + " from " + source; }

	@Override
	public String getAvatarSource() {
		return user.profile_image_url_https;
	}
}
