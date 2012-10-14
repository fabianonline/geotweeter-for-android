package de.geotweeter.apiconn;

import java.util.ArrayList;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;

import de.geotweeter.Constants;
import de.geotweeter.Debug;
import de.geotweeter.SendableTweet;
import de.geotweeter.exceptions.TweetSendException;
import de.geotweeter.timelineelements.DirectMessage;
import de.geotweeter.timelineelements.TimelineElement;
import de.geotweeter.timelineelements.Tweet;

public class TwitterApiAccess {

	private static transient OAuthService service;
	private Token token;

	public TwitterApiAccess(Token token) {
		if (service == null) {
			ServiceBuilder builder = new ServiceBuilder()
			                             .provider(TwitterApi.class)
			                             .apiKey(Constants.API_KEY)
			                             .apiSecret(Constants.API_SECRET);
			if (Debug.LOG_OAUTH_STUFF) {
				builder = builder.debug();
			}
			service = builder.build();
		}
		this.token = token;
		
	}
	
	public ArrayList<TimelineElement> getMentions(long from_id, long to_id) throws OAuthException {
		return getMentions(from_id, to_id, 100);
	}
	
	public ArrayList<TimelineElement> getMentions(long from_id, long to_id, int count) throws OAuthException {
		OAuthRequest req = new OAuthRequest(Verb.GET, Constants.URI_MENTIONS);
		return getTimeline(req, from_id, to_id, count);
	}
	
	public ArrayList<TimelineElement> getHomeTimeline(long from_id, long to_id) throws OAuthException {
		return getHomeTimeline(from_id, to_id, 100);
	}
	
	public ArrayList<TimelineElement> getHomeTimeline(long from_id, long to_id, int count) throws OAuthException {
		OAuthRequest req = new OAuthRequest(Verb.GET, Constants.URI_HOME_TIMELINE);
		return getTimeline(req, from_id, to_id, count);
	}
	
	public ArrayList<TimelineElement> getUserTimeline(String screen_name) throws OAuthException {
		return getUserTimeline(screen_name, 0, 0);
	}
	
	public ArrayList<TimelineElement> getUserTimeline(long user_id) throws OAuthException {
		return getUserTimeline(user_id, 0, 0);
	}

	public ArrayList<TimelineElement> getUserTimeline(String screen_name, long from_id, long to_id) throws OAuthException {
		OAuthRequest req = new OAuthRequest(Verb.GET, Constants.URI_USER_TIMELINE);
		req.addQuerystringParameter("screen_name", screen_name);
		return getTimeline(req, from_id, to_id, 100);
	}
	
	public ArrayList<TimelineElement> getUserTimeline(long user_id, long from_id, long to_id) throws OAuthException {
		OAuthRequest req = new OAuthRequest(Verb.GET, Constants.URI_USER_TIMELINE);
		req.addQuerystringParameter("user_id", String.valueOf(user_id));
		return getTimeline(req, from_id, to_id, 100);
	}

	private ArrayList<TimelineElement> getTimeline(OAuthRequest req, long from_id, long to_id, int count) throws OAuthException {
		ArrayList<TimelineElement> elements = null;
		req.addQuerystringParameter("count", String.valueOf(count));
		if (from_id != 0) {
			req.addQuerystringParameter("since_id", String.valueOf(from_id));
		}
		if (to_id != 0) {
			req.addQuerystringParameter("max_id", String.valueOf(to_id));
		}
		service.signRequest(token, req);
		Response response;
		response = req.send();
		if (response.isSuccessful()) {
			elements = parseTweets(response.getBody());
		}
		return elements;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<TimelineElement> parseTweets(String json) {
		return (ArrayList<TimelineElement>)(ArrayList<?>)JSON.parseObject(json, new TypeReference<ArrayList<Tweet>>(){}, Feature.DisableCircularReferenceDetect);
	}

	public ArrayList<TimelineElement> getReceivedDMs(long from_id, long to_id) {
		return getReceivedDMs(from_id, to_id, 50);
	}
	
	public ArrayList<TimelineElement> getReceivedDMs(long from_id, long to_id, int count) {
		OAuthRequest req = new OAuthRequest(Verb.GET, Constants.URI_DIRECT_MESSAGES);
		return getDMTimeline(req, from_id, to_id, count);
	}
	
	public ArrayList<TimelineElement> getSentDMs(long from_id, long to_id) {
		return getSentDMs(from_id, to_id, 50);
	}
	
	public ArrayList<TimelineElement> getSentDMs(long from_id, long to_id, int count) {
		OAuthRequest req = new OAuthRequest(Verb.GET, Constants.URI_DIRECT_MESSAGES_SENT);
		return getDMTimeline(req, from_id, to_id, count);
	}

	private ArrayList<TimelineElement> getDMTimeline(OAuthRequest req, long from_id, long to_id, int count) throws OAuthException {
		ArrayList<TimelineElement> elements = null;
		req.addQuerystringParameter("count", String.valueOf(count));
		if (from_id != 0) {
			req.addQuerystringParameter("since_id", String.valueOf(from_id));
		}
		if (to_id != 0) {
			req.addQuerystringParameter("max_id", String.valueOf(to_id));
		}
		service.signRequest(token, req);
		Response response = req.send();
		if (response.isSuccessful()) {
			elements = parseDMs(response.getBody());
		}
		return elements;
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<TimelineElement> parseDMs(String json) {
		return (ArrayList<TimelineElement>)(ArrayList<?>)JSON.parseObject(json, new TypeReference<ArrayList<DirectMessage>>(){});
	}

	public Tweet getTweet(long id) throws OAuthException {
		Tweet result = null;
		OAuthRequest req = new OAuthRequest(Verb.GET, Constants.URI_SHOW_TWEET);
		req.addQuerystringParameter("id", String.valueOf(id));
		service.signRequest(token, req);
		Response response = req.send();
		if (response.isSuccessful()) {
			result = JSON.parseObject(response.getBody(), Tweet.class);		
		}
		return result;
	}
	
	public Tweet sendTweet(SendableTweet tweet) throws OAuthException, TweetSendException {
		Tweet result = null;
		OAuthRequest req = new OAuthRequest(Verb.POST, Constants.URI_UPDATE);
		req.addBodyParameter("status", tweet.text);
		
		if (tweet.location != null) {
			req.addBodyParameter("lat", String.valueOf(tweet.location.getLatitude()));
			req.addBodyParameter("long", String.valueOf(tweet.location.getLongitude()));
		}
		
		if (tweet.reply_to_status_id > 0) {
			req.addBodyParameter("in_reply_to_status_id", String.valueOf(tweet.reply_to_status_id));
		}
		
		service.signRequest(token, req);
		Response response = req.send();
		if (response.isSuccessful()) {
			result = JSON.parseObject(response.getBody(), Tweet.class);
		} else { 
			throw new TweetSendException();
		}

		return result;
	}
	
}
