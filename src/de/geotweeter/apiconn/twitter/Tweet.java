package de.geotweeter.apiconn.twitter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.view.View;
import de.geotweeter.AccountManager;
import de.geotweeter.Constants;
import de.geotweeter.Constants.TLEType;
import de.geotweeter.R;
import de.geotweeter.Utils;
import de.geotweeter.Utils.PictureService;
import de.geotweeter.apiconn.ImglyApiAccess;
import de.geotweeter.apiconn.ImgurApiAccess;
import de.geotweeter.apiconn.InstagramApiAccess;
import de.geotweeter.apiconn.LockerzApiAccess;
import de.geotweeter.apiconn.MobytoApiAccess;
import de.geotweeter.apiconn.OwlyApiAccess;
import de.geotweeter.apiconn.PlixiApiAccess;
import de.geotweeter.apiconn.TwitpicApiAccess;
import de.geotweeter.apiconn.YfrogApiAccess;
import de.geotweeter.apiconn.YoutubeApiAccess;
import de.geotweeter.timelineelements.TimelineElement;

/**
 * Representing a single tweet
 */
public class Tweet extends TimelineElement {
	private static final long serialVersionUID = -6610449879010917836L;
	@SuppressWarnings("unused")
	private static final String LOG = "Tweet";
	public Coordinates coordinates;
	public String text;
	public String textForDisplay = null;
	public long id;
	public User user;
	public View view;
	public String source;
	public Entities entities;
	public long in_reply_to_status_id;
	public long in_reply_to_user_id;
	private Place place;
	public Tweet retweeted_status;
	public boolean favorited;
	public boolean maskRetweetedStatus = false;

	public Tweet() {
	}

	public Tweet(User user) {
		this();
		this.user = user;
	}

	/**
	 * Returns the tweet id
	 * 
	 * @return Tweet id
	 */
	public long getID() {
		return id;
	}

	/**
	 * Formats the tweet text to be displayed. Short URLs will be replaced here.
	 * 
	 * @return Text to be displayed
	 */
	public String getTextForDisplay() {
		if (textForDisplay == null) {
			textForDisplay = "";
			if (text != null) {
				textForDisplay = new String(text);
			}
			if (entities != null) {
				if (entities.urls != null) {
					for (Url url : entities.urls) {
						textForDisplay = textForDisplay.replace(url.url,
								url.display_url);
					}
				}
				if (entities.media != null) {
					for (Media media : entities.media) {
						textForDisplay = textForDisplay.replace(media.url,
								media.display_url);
					}
				}

			}
			textForDisplay = textForDisplay.replace("&lt;", "<")
					.replace("&gt;", ">").replace("&amp;", "&");
		}
		return textForDisplay;
	}

	@Override
	public String getTitleForDisplay() {
		return getSenderScreenName();
	}

	public void setUser(User u) {
		if (User.all_users.containsKey(u.id)) {
			user = User.all_users.get(u.id);
		} else {
			User.all_users.put(u.id, u);
			user = u;
		}
	}

	public String getAvatarSource() {
		return user.getAvatarSource();
	}

	/**
	 * Extracts the twitter client
	 * 
	 * @param str
	 */
	public void setSource(String str) {
		Matcher m = Constants.REGEXP_FIND_SOURCE.matcher(str);
		if (m.find()) {
			source = m.group(1);
		} else {
			source = Utils.getString(R.string.tweet_source_web);
		}
	}

	public Drawable getAvatarDrawable() {
		return user.avatar;
	}

	public String getSourceText() {
		return Utils.formatString(R.string.tweet_source, source);
	}

	public String getSenderScreenName() {
		if (user == null) {
			return "Da fouque?";
		}
		return user.getScreenName();
	}

	@Override
	public boolean isReplyable() {
		return true;
	}

	@Override
	public boolean showNotification() {
		return true;
	}

	@Override
	public String getNotificationText(String type) {
		if (type.equals("mention")) {
			return Utils.formatString(R.string.tweet_notification_text_mention,
					user.screen_name, text);
		} else if (type.equals("retweet")) {
			return Utils.formatString(R.string.tweet_notification_text_retweet,
					user.screen_name, text);
		}
		return "";
	}

	@Override
	public String getNotificationContentTitle(String type) {
		if (type.equals("mention")) {
			return Utils.formatString(
					R.string.tweet_notification_content_title_mention,
					user.screen_name);
		} else if (type.equals("retweet")) {
			return Utils.formatString(
					R.string.tweet_notification_content_title_retweet,
					user.screen_name);
		}
		return "";
	}

	@Override
	public String getNotificationContentText(String type) {
		return text;
	}

	/**
	 * Returns the element type of the tweet for layout reasons
	 * 
	 * @return The timeline element type of the tweet
	 */
	@Override
	public TLEType getType() {
		try {
			User current_user = AccountManager.current_account.getUser();
			if (this.user.id == current_user.id) {
				return TLEType.OWN;
			}
			if (this.mentionsUser(current_user)) {
				return TLEType.MENTION;
			}
			if (this.id > AccountManager.current_account.getMaxReadTweetID()) {
				return TLEType.UNREAD;
			}
		} catch (NullPointerException e) {

		}
		return TLEType.READ;
	}

	/**
	 * Returns true if the given user is mentioned
	 * 
	 * @param user
	 *            User whose mention status is to be checked
	 * @return
	 */
	public boolean mentionsUser(User user) {
		if (entities != null) {
			for (int i = 0; i < entities.user_mentions.size(); i++) {
				if (entities.user_mentions.get(i).id == user.id) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String getSenderName() {
		if (user == null) {
			return "Da fouque?";
		}
		return user.name;
	}

	@Override
	public String getPlaceString() {
		if (place == null) {
			return null;
		}
		return place.getFullName();
	}

	/**
	 * Parses the tweet text for media links and provides a list of URL pairs of
	 * thumbnail and full size URLs
	 * 
	 * @return List of URL pairs
	 */
	public List<Pair<URL, URL>> getMediaList() {
		List<Pair<URL, URL>> result = new ArrayList<Pair<URL, URL>>();
		if (entities == null) {
			return result;
		}
		if (entities.media == null) {
			return result;
		}
		for (Media media : entities.media) {
			try {
				Pair<URL, URL> urls = new Pair<URL, URL>(new URL(
						media.media_url + ":thumb"), new URL(media.media_url));
				result.add(urls);
			} catch (MalformedURLException e) {
				continue;
			}
		}
		if (entities.urls == null) {
			return result;
		}
		for (Url url : entities.urls) {
			PictureService hoster = Utils.getPictureService(url);
			switch (hoster) {
			case TWITPIC:
				try {
					result.add(TwitpicApiAccess.getUrlPair(url));
				} catch (MalformedURLException e) {
					break;
				}
				break;
			case YFROG:
				try {
					result.add(YfrogApiAccess.getUrlPair(url));
				} catch (MalformedURLException e) {
					break;
				}
				break;
			case YOUTUBE:
				try {
					result.add(YoutubeApiAccess.getUrlPair(url));
				} catch (MalformedURLException e) {
					break;
				}
				break;
			case IMGUR:
				try {
					result.add(ImgurApiAccess.getUrlPair(url));
				} catch (MalformedURLException e) {
					break;
				}
				break;
			case IMGLY:
				try {
					result.add(ImglyApiAccess.getUrlPair(url));
				} catch (MalformedURLException e) {
					break;
				}
				break;
			case INSTAGRAM:
				try {
					result.add(InstagramApiAccess.getUrlPair(url));
				} catch (MalformedURLException e) {
					break;
				}
				break;
			case PLIXI:
				try {
					result.add(PlixiApiAccess.getUrlPair(url));
				} catch (MalformedURLException e) {
					break;
				}
				break;
			case LOCKERZ:
				try {
					result.add(LockerzApiAccess.getUrlPair(url));
				} catch (MalformedURLException e) {
					break;
				}
				break;
			case MOBYTO:
				try {
					result.add(MobytoApiAccess.getUrlPair(url));
				} catch (MalformedURLException e) {
					break;
				}
				break;
			case OWLY:
				try {
					result.add(OwlyApiAccess.getUrlPair(url));
				} catch (MalformedURLException e) {
					break;
				}
				break;
			case NONE:
			default:
				break;
			}
		}
		return result;
	}

	/**
	 * Checks if the app user is the tweet sender
	 * 
	 * @return true if the app user is the sender
	 */
	@Override
	public boolean isOwnMessage() {
		try {
			return (this.user.id == AccountManager.current_account.getUser().id);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 
	 * @return true if the tweet can be retweeted
	 */
	public boolean isRetweetable() {
		try {
			return !isOwnMessage() || !(this.user._protected);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 
	 * @return true if the tweet is an answer
	 */
	public boolean isConversationEndpoint() {
		return (this.in_reply_to_status_id != 0);
	}

}
