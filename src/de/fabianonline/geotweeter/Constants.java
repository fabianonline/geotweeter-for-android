package de.fabianonline.geotweeter;

import java.util.regex.Pattern;

import android.app.Application;

public class Constants extends Application {

	public static final String PREFS_APP = "geotweeter_general";
	public static final String PREFS_ACCOUNTS = "geotweeter_accounts";
	
	public static final String API_KEY = "7tbUmgasX8QXazkxGMNw";
	public static final String API_SECRET = "F22QSxchkczthiUQomREXEu4zDA15mxiENNttkkA";
	public static final String OAUTH_CALLBACK = "oauth://twitter";
	
	public static final String URI_VERIFY_CREDENTIALS   = "https://api.twitter.com/1.1/account/verify_credentials.json";
	public static final String URI_UPDATE_PROFILE       = "https://api.twitter.com/1.1/account/update_profile.json";
	public static final String URI_USER_STREAM          = "https://userstream.twitter.com/1.1/user.json";
	public static final String URI_HOME_TIMELINE        = "https://api.twitter.com/1.1/statuses/home_timeline.json";
	public static final String URI_MENTIONS             = "https://api.twitter.com/1.1/statuses/mentions_timeline.json";
	public static final String URI_DIRECT_MESSAGES      = "https://api.twitter.com/1.1/direct_messages.json";
	public static final String URI_DIRECT_MESSAGES_SENT = "https://api.twitter.com/1.1/direct_messages/sent.json";
	public static final String URI_UPDATE               = "https://api.twitter.com/1.1/statuses/update.json";
	
	public static final String URI_TWEETMARKER_LASTREAD = "https://api.tweetmarker.net/v1/lastread?collection=timeline,mentions,messages&username=";
	public static final String TWEETMARKER_KEY = "GT-F181AC70B051";
	
	public static final Pattern REGEXP_FIND_SOURCE = Pattern.compile(">(.+)</a>");
	public static final String GCM_SENDER_ID = "540800208547";
	public static final String GCM_SERVER_URL = "https://home.fabianonline.de/geotweeter-gcm/";
	
	public static final String MAPS_API_KEY = "0rSU2R8cwncwINNWuOQ4nOC3CxWFEUTEkYMiApA";
	
	
	
}
