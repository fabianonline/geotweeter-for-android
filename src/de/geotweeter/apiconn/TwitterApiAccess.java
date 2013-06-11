package de.geotweeter.apiconn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
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
import de.geotweeter.Constants.RequestType;
import de.geotweeter.Debug;
import de.geotweeter.SendableTweet;
import de.geotweeter.Utils;
import de.geotweeter.apiconn.twitter.DirectMessage;
import de.geotweeter.apiconn.twitter.Friendship;
import de.geotweeter.apiconn.twitter.Relationship;
import de.geotweeter.apiconn.twitter.Tweet;
import de.geotweeter.apiconn.twitter.TwitterConfig;
import de.geotweeter.apiconn.twitter.User;
import de.geotweeter.apiconn.twitter.UserIds;
import de.geotweeter.apiconn.twitter.Users;
import de.geotweeter.exceptions.APIRequestException;
import de.geotweeter.exceptions.BadConnectionException;
import de.geotweeter.exceptions.DestroyException;
import de.geotweeter.exceptions.FavException;
import de.geotweeter.exceptions.PermanentTweetSendException;
import de.geotweeter.exceptions.RetweetException;
import de.geotweeter.exceptions.TemporaryTweetSendException;
import de.geotweeter.exceptions.TweetAccessException;
import de.geotweeter.exceptions.TweetSendException;
import de.geotweeter.timelineelements.TimelineElement;

public class TwitterApiAccess {

	private static transient OAuthService service;
	private Token token;

	public TwitterApiAccess(Token token) {
		if (service == null) {
			ServiceBuilder builder = new ServiceBuilder()
					.provider(TwitterApi.class)
					.apiKey(Utils.getProperty("twitter.consumer.key"))
					.apiSecret(Utils.getProperty("twitter.consumer.secret"));
			if (Debug.LOG_OAUTH_STUFF) {
				builder = builder.debug();
			}
			service = builder.build();
		}
		this.token = token;

	}

	public OAuthRequest getVerifiedCredentials() {
		OAuthRequest req = new OAuthRequest(Verb.GET,
				Constants.URI_VERIFY_CREDENTIALS_1);
		service.signRequest(token, req);
		return req;
	}

	public void signRequest(OAuthRequest req) {
		service.signRequest(token, req);
	}

	public ArrayList<TimelineElement> getMentions(long from_id, long to_id)
			throws OAuthException, BadConnectionException {
		return getMentions(from_id, to_id, 100);
	}

	public ArrayList<TimelineElement> getMentions(long from_id, long to_id,
			int count) throws OAuthException, BadConnectionException {
		OAuthRequest req = new OAuthRequest(Verb.GET, Constants.URI_MENTIONS);
		try {
			return getTimeline(req, from_id, to_id, count);
		} catch (BadConnectionException e) {
			throw new BadConnectionException(RequestType.MENTIONS);
		}
	}

	public ArrayList<TimelineElement> getHomeTimeline(long from_id, long to_id)
			throws OAuthException, BadConnectionException {
		return getHomeTimeline(from_id, to_id, 100);
	}

	public ArrayList<TimelineElement> getHomeTimeline(long from_id, long to_id,
			int count) throws OAuthException, BadConnectionException {
		OAuthRequest req = new OAuthRequest(Verb.GET,
				Constants.URI_HOME_TIMELINE);
		try {
			return getTimeline(req, from_id, to_id, count);
		} catch (BadConnectionException e) {
			throw new BadConnectionException(RequestType.HOME_TIMELINE);
		}
	}

	public List<TimelineElement> getUserTimeline(String screen_name)
			throws OAuthException, BadConnectionException {
		return getUserTimeline(screen_name, 0, 0);
	}

	public List<TimelineElement> getUserTimeline(long user_id)
			throws OAuthException, BadConnectionException {
		return getUserTimeline(user_id, 0, 0);
	}

	public List<TimelineElement> getUserTimeline(String screen_name,
			long from_id, long to_id) throws OAuthException,
			BadConnectionException {
		OAuthRequest req = new OAuthRequest(Verb.GET,
				Constants.URI_USER_TIMELINE);
		req.addQuerystringParameter("screen_name", screen_name);
		try {
			return getTimeline(req, from_id, to_id, 100);
		} catch (BadConnectionException e) {
			throw new BadConnectionException(RequestType.USER_TIMELINE);
		}
	}

	public List<TimelineElement> getUserTimeline(long user_id, long from_id,
			long to_id) throws OAuthException, BadConnectionException {
		OAuthRequest req = new OAuthRequest(Verb.GET,
				Constants.URI_USER_TIMELINE);
		req.addQuerystringParameter("user_id", String.valueOf(user_id));
		try {
			return getTimeline(req, from_id, to_id, 100);
		} catch (BadConnectionException e) {
			throw new BadConnectionException(RequestType.USER_TIMELINE);
		}
	}

	private ArrayList<TimelineElement> getTimeline(OAuthRequest req,
			long from_id, long to_id, int count) throws OAuthException,
			BadConnectionException {
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
			try {
				elements = parseTweets(response.getBody());
			} catch (IllegalStateException e) {
				throw new BadConnectionException();
			}
		} else {
			// TODO Do something more than just chickening out.
			elements = new ArrayList<TimelineElement>();
		}
		return elements;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<TimelineElement> parseTweets(String json) {
		return (ArrayList<TimelineElement>) (ArrayList<?>) JSON.parseObject(
				json, new TypeReference<ArrayList<Tweet>>() {
				}, Feature.DisableCircularReferenceDetect);
	}

	@SuppressWarnings("unchecked")
	private ArrayList<User> parseUsers(String json) {
		return (ArrayList<User>) (ArrayList<?>) JSON.parseObject(json,
				new TypeReference<ArrayList<User>>() {
				}, Feature.DisableCircularReferenceDetect);
	}

	public ArrayList<TimelineElement> getReceivedDMs(long from_id, long to_id)
			throws BadConnectionException {
		return getReceivedDMs(from_id, to_id, 50);
	}

	public ArrayList<TimelineElement> getReceivedDMs(long from_id, long to_id,
			int count) throws BadConnectionException {
		OAuthRequest req = new OAuthRequest(Verb.GET,
				Constants.URI_DIRECT_MESSAGES);
		try {
			return getDMTimeline(req, from_id, to_id, count);
		} catch (BadConnectionException e) {
			throw new BadConnectionException(RequestType.DM_RCVD);
		}
	}

	public ArrayList<TimelineElement> getSentDMs(long from_id, long to_id)
			throws BadConnectionException {
		return getSentDMs(from_id, to_id, 50);
	}

	public ArrayList<TimelineElement> getSentDMs(long from_id, long to_id,
			int count) throws BadConnectionException {
		OAuthRequest req = new OAuthRequest(Verb.GET,
				Constants.URI_DIRECT_MESSAGES_SENT);
		try {
			return getDMTimeline(req, from_id, to_id, count);
		} catch (BadConnectionException e) {
			throw new BadConnectionException(RequestType.DM_SENT);
		}
	}

	private ArrayList<TimelineElement> getDMTimeline(OAuthRequest req,
			long from_id, long to_id, int count) throws OAuthException,
			BadConnectionException {
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
			try {
				elements = parseDMs(response.getBody());
			} catch (IllegalStateException e) {
				throw new BadConnectionException();
			}
		}
		return elements;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<TimelineElement> parseDMs(String json) {
		return (ArrayList<TimelineElement>) (ArrayList<?>) JSON.parseObject(
				json, new TypeReference<ArrayList<DirectMessage>>() {
				});
	}

	public Tweet getTweet(long id) throws OAuthException, TweetAccessException,
			BadConnectionException {
		Tweet result = null;
		OAuthRequest req = new OAuthRequest(Verb.GET, Constants.URI_SHOW_TWEET);
		req.addQuerystringParameter("id", String.valueOf(id));
		service.signRequest(token, req);
		Response response = req.send();
		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(), Tweet.class);
			} catch (IllegalStateException e) {
				throw new BadConnectionException(RequestType.SINGLE_TWEET);
			}
		} else {
			if (response.getCode() == 403) {
				throw new TweetAccessException();
			}
		}
		return result;
	}

	public Tweet sendTweet(SendableTweet tweet) throws OAuthException,
			TweetSendException {
		Tweet result = null;
		OAuthRequest req;
		if (tweet.dmRecipient == null) {
			/* Normal tweet (as in 'not a direct message') */
			req = new OAuthRequest(Verb.POST, Constants.URI_UPDATE);
			req.setReadTimeout(20, TimeUnit.SECONDS);
			if (tweet.location != null) {
				req.addBodyParameter("lat",
						String.valueOf(tweet.location.getLatitude()));
				req.addBodyParameter("long",
						String.valueOf(tweet.location.getLongitude()));
			}

			if (tweet.reply_to_status_id > 0) {
				req.addBodyParameter("in_reply_to_status_id",
						String.valueOf(tweet.reply_to_status_id));
			}
			req.addBodyParameter("status", tweet.text);
		} else {
			/* direct message */
			req = new OAuthRequest(Verb.POST, Constants.URI_SEND_DIRECT_MESSAGE);
			req.addBodyParameter("screen_name", tweet.dmRecipient);
			req.addBodyParameter("text", tweet.text);
		}

		service.signRequest(token, req);

		Response response;

		try {
			response = req.send();
		} catch (OAuthException e) {
			// TODO In the next scribe version will be more differentiated
			// Exception classes for
			// connection problems and so on. We really should use that.
			throw new TemporaryTweetSendException(e);
		}

		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(), Tweet.class);
			} catch (IllegalStateException e) {
				throw new TemporaryTweetSendException();
			}
		} else {
			if (response.getCode() >= 500) {
				throw new TemporaryTweetSendException("Server side error "
						+ String.valueOf(response.getCode()));
			} else {
				throw new PermanentTweetSendException("http error code "
						+ String.valueOf(response.getCode()));
			}
		}

		return result;
	}

	public Tweet sendTweetWithPicture(SendableTweet tweet, ContentBody picture)
			throws OAuthException, TweetSendException, IOException {
		Tweet result = null;
		OAuthRequest req = new OAuthRequest(Verb.POST,
				Constants.URI_UPDATE_WITH_MEDIA);
		MultipartEntity entity = new MultipartEntity();
		entity.addPart("status",
				new StringBody(tweet.text, Charset.defaultCharset()));
		entity.addPart("media", picture);

		if (tweet.location != null) {
			entity.addPart(
					"lat",
					new StringBody(String.valueOf(tweet.location.getLatitude())));
			entity.addPart(
					"long",
					new StringBody(
							String.valueOf(tweet.location.getLongitude())));
		}

		if (tweet.reply_to_status_id > 0) {
			entity.addPart("in_reply_to_status_id",
					new StringBody(String.valueOf(tweet.reply_to_status_id)));
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		entity.writeTo(out);

		req.addPayload(out.toByteArray());
		req.addHeader(entity.getContentType().getName(), entity
				.getContentType().getValue());

		service.signRequest(token, req);

		Response response;
		try {
			response = req.send();
		} catch (OAuthException e) {
			// TODO In the next scribe version will be more differentiated
			// Exception classes for
			// connection problems and so on. We really should use that.
			throw new TemporaryTweetSendException(e);
		}

		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(), Tweet.class);
			} catch (IllegalStateException e) {
				throw new TemporaryTweetSendException();
			}
		} else {
			if (response.getCode() >= 500)
				throw new TemporaryTweetSendException("Server side error "
						+ String.valueOf(response.getCode()));
			else {
				throw new PermanentTweetSendException("http error code "
						+ String.valueOf(response.getCode()));
			}
		}

		return result;
	}

	public Tweet destroyMessage(long id) throws UnsupportedEncodingException,
			DestroyException, BadConnectionException {
		Tweet result = null;
		OAuthRequest req = new OAuthRequest(Verb.POST,
				Constants.URI_DELETE_DIRECT_MESSAGE);
		req.addBodyParameter("id", String.valueOf(id));

		service.signRequest(token, req);

		Response response;
		try {
			response = req.send();
		} catch (OAuthException ex) {
			throw new DestroyException();
		}

		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(), Tweet.class);
			} catch (IllegalStateException e) {
				throw new BadConnectionException(RequestType.DELETE_DM);
			}
		} else {
			throw new DestroyException();
		}
		return result;
	}

	public Tweet destroyTweet(long id) throws OAuthException, DestroyException,
			BadConnectionException {
		Tweet result = null;
		String uri = Constants.URI_DESTROY.replace(":id", String.valueOf(id));
		OAuthRequest req = new OAuthRequest(Verb.POST, uri);

		service.signRequest(token, req);
		Response response = req.send();

		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(), Tweet.class);
			} catch (IllegalStateException e) {
				throw new BadConnectionException(RequestType.DELETE_TWEET);
			}
		} else {
			throw new DestroyException();
		}

		return result;
	}

	public Tweet retweet(long id) throws OAuthException, RetweetException,
			BadConnectionException {
		Tweet result = null;
		String uri = Constants.URI_RETWEET.replace(":id", String.valueOf(id));
		OAuthRequest req = new OAuthRequest(Verb.POST, uri);

		service.signRequest(token, req);
		Response response = req.send();

		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(), Tweet.class);
			} catch (IllegalStateException e) {
				throw new BadConnectionException(RequestType.RETWEET);
			}
		} else {
			throw new RetweetException();
		}

		return result;
	}

	public Tweet fav(long id) throws FavException, BadConnectionException {
		Tweet result = null;
		OAuthRequest req = new OAuthRequest(Verb.POST, Constants.URI_FAV);
		req.addBodyParameter("id", String.valueOf(id));

		service.signRequest(token, req);
		Response response = req.send();

		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(), Tweet.class);
			} catch (IllegalStateException e) {
				throw new BadConnectionException(RequestType.FAV);
			}
		} else {
			throw new FavException(false);
		}

		return result;
	}

	public Tweet defav(long id) throws FavException, BadConnectionException {
		Tweet result = null;
		OAuthRequest req = new OAuthRequest(Verb.POST, Constants.URI_DEFAV);
		req.addBodyParameter("id", String.valueOf(id));

		service.signRequest(token, req);
		Response response = req.send();

		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(), Tweet.class);
			} catch (IllegalStateException e) {
				throw new BadConnectionException(RequestType.DEFAV);
			}
		} else {
			throw new FavException(true);
		}

		return result;
	}

	public User reportSpam(long id) throws APIRequestException,
			BadConnectionException {
		User result = null;
		OAuthRequest req = new OAuthRequest(Verb.POST,
				Constants.URI_REPORT_SPAM);
		req.addBodyParameter("user_id", String.valueOf(id));

		service.signRequest(token, req);
		Response response = req.send();

		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(), User.class);
			} catch (IllegalStateException e) {
				throw new BadConnectionException(RequestType.REPORT_SPAM);
			}
		} else {
			throw new APIRequestException(RequestType.REPORT_SPAM,
					response.getCode(), response.getBody());
		}

		return result;
	}

	public User block(long id) throws APIRequestException,
			BadConnectionException {
		User result = null;
		OAuthRequest req = new OAuthRequest(Verb.POST, Constants.URI_BLOCK);
		req.addBodyParameter("user_id", String.valueOf(id));
		req.addBodyParameter("skip_status", "t");

		service.signRequest(token, req);
		Response response = req.send();

		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(), User.class);
			} catch (IllegalStateException e) {
				throw new BadConnectionException(RequestType.BLOCK);
			}
		} else {
			throw new APIRequestException(RequestType.BLOCK,
					response.getCode(), response.getBody());
		}

		return result;
	}

	public User unblock(long id) throws APIRequestException,
			BadConnectionException {
		User result = null;
		OAuthRequest req = new OAuthRequest(Verb.POST, Constants.URI_UNBLOCK);
		req.addBodyParameter("user_id", String.valueOf(id));
		req.addBodyParameter("skip_status", "t");

		service.signRequest(token, req);
		Response response = req.send();

		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(), User.class);
			} catch (IllegalStateException e) {
				throw new BadConnectionException(RequestType.UNBLOCK);
			}
		} else {
			throw new APIRequestException(RequestType.UNBLOCK,
					response.getCode(), response.getBody());
		}

		return result;
	}

	public User follow(long id) throws APIRequestException,
			BadConnectionException {
		User result = null;
		OAuthRequest req = new OAuthRequest(Verb.POST, Constants.URI_FOLLOW);
		req.addBodyParameter("user_id", String.valueOf(id));

		service.signRequest(token, req);
		Response response = req.send();

		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(), User.class);
			} catch (IllegalStateException e) {
				throw new BadConnectionException(RequestType.FOLLOW);
			}
		} else {
			throw new APIRequestException(RequestType.FOLLOW,
					response.getCode(), response.getBody());
		}

		return result;
	}

	public User unfollow(long id) throws APIRequestException,
			BadConnectionException {
		User result = null;
		OAuthRequest req = new OAuthRequest(Verb.POST, Constants.URI_UNFOLLOW);
		req.addBodyParameter("user_id", String.valueOf(id));

		service.signRequest(token, req);
		Response response = req.send();

		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(), User.class);
			} catch (IllegalStateException e) {
				throw new BadConnectionException(RequestType.UNFOLLOW);
			}
		} else {
			throw new APIRequestException(RequestType.UNFOLLOW,
					response.getCode(), response.getBody());
		}

		return result;
	}

	public UserIds getFriendIds(long id) throws BadConnectionException,
			APIRequestException {
		return getFriendIds(id, -1);
	}

	public UserIds getFriendIds(long id, long cursor)
			throws BadConnectionException, APIRequestException {
		UserIds result = null;

		OAuthRequest req = new OAuthRequest(Verb.GET,
				Constants.URI_GET_FRIEND_IDS);
		req.addQuerystringParameter("user_id", String.valueOf(id));
		req.addQuerystringParameter("cursor", String.valueOf(cursor));
		req.addQuerystringParameter("stringify_ids", "false");

		service.signRequest(token, req);
		Response response = req.send();

		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(), UserIds.class);
			} catch (IllegalStateException e) {
				throw new BadConnectionException(RequestType.FRIEND_IDS);
			}
		} else {
			throw new APIRequestException(RequestType.FRIEND_IDS,
					response.getCode(), response.getBody());
		}

		return result;
	}

	public UserIds getFollowerIds(long id) throws BadConnectionException,
			APIRequestException {
		return getFollowerIds(id, -1);
	}

	public UserIds getFollowerIds(long id, long cursor)
			throws BadConnectionException, APIRequestException {
		UserIds result = null;

		OAuthRequest req = new OAuthRequest(Verb.GET,
				Constants.URI_GET_FOLLOWER_IDS);
		req.addQuerystringParameter("user_id", String.valueOf(id));
		req.addQuerystringParameter("cursor", String.valueOf(cursor));
		req.addQuerystringParameter("stringify_ids", "false");

		service.signRequest(token, req);
		Response response = req.send();

		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(), UserIds.class);
			} catch (IllegalStateException e) {
				throw new BadConnectionException(RequestType.FOLLOWER_IDS);
			}
		} else {
			throw new APIRequestException(RequestType.FOLLOWER_IDS,
					response.getCode(), response.getBody());
		}

		return result;
	}

	public List<User> lookupUsers(List<Long> ids) {
		ArrayList<User> result = null;
		OAuthRequest req = new OAuthRequest(Verb.POST,
				Constants.URI_LOOKUP_USERS);
		String idList = ids.toString();
		idList = (String) idList.subSequence(1, idList.length() - 1);
		req.addBodyParameter("user_id", idList);

		return result;
	}

	public Relationship getRelationship(long source, long target)
			throws BadConnectionException, APIRequestException {
		Friendship result = null;
		OAuthRequest req = new OAuthRequest(Verb.GET,
				Constants.URI_FRIENDSHIP_SHOW);
		req.addQuerystringParameter("source_id", String.valueOf(source));
		req.addQuerystringParameter("target_id", String.valueOf(target));

		service.signRequest(token, req);
		Response response = req.send();

		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(), Friendship.class);
			} catch (IllegalStateException e) {
				throw new BadConnectionException(RequestType.RELATIONSHIP);
			}
		} else {
			throw new APIRequestException(RequestType.RELATIONSHIP,
					response.getCode(), response.getBody());
		}

		return result.relationship;
	}

	public Relationship getRelationship(String source, String target)
			throws APIRequestException, BadConnectionException {
		Friendship result = null;
		OAuthRequest req = new OAuthRequest(Verb.GET,
				Constants.URI_FRIENDSHIP_SHOW);
		req.addQuerystringParameter("source_screen_name", source);
		req.addQuerystringParameter("target_screen_name", target);

		service.signRequest(token, req);
		Response response = req.send();

		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(), Friendship.class);
			} catch (IllegalStateException e) {
				throw new BadConnectionException(RequestType.RELATIONSHIP);
			}
		} else {
			throw new APIRequestException(RequestType.RELATIONSHIP,
					response.getCode(), response.getBody());
		}

		return result.relationship;
	}

	public Users getFollowers(String screenName) throws APIRequestException,
			BadConnectionException {
		return getFollowers(screenName, -1);
	}

	public Users getFollowers(String screenName, long cursor)
			throws APIRequestException, BadConnectionException {
		Users result = null;
		OAuthRequest req = new OAuthRequest(Verb.GET,
				Constants.URI_FOLLOWER_LIST);
		req.addQuerystringParameter("screen_name", screenName);
		req.addQuerystringParameter("cursor", String.valueOf(cursor));
		req.addQuerystringParameter("skip_status", "false");

		service.signRequest(token, req);
		Response response = req.send();

		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(), Users.class);
			} catch (IllegalStateException e) {
				throw new BadConnectionException(RequestType.FOLLOWERS);
			}
		} else {
			throw new APIRequestException(RequestType.FOLLOWERS,
					response.getCode(), response.getBody());
		}

		return result;
	}

	public Users getFollowing(String screenName) throws APIRequestException,
			BadConnectionException {
		return getFollowing(screenName, -1);
	}

	public Users getFollowing(String screenName, long cursor)
			throws APIRequestException, BadConnectionException {
		Users result = null;
		OAuthRequest req = new OAuthRequest(Verb.GET,
				Constants.URI_FOLLOWING_LIST);
		req.addQuerystringParameter("screen_name", screenName);
		req.addQuerystringParameter("cursor", String.valueOf(cursor));
		req.addQuerystringParameter("skip_status", "false");

		service.signRequest(token, req);
		Response response = req.send();

		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(), Users.class);
			} catch (IllegalStateException e) {
				throw new BadConnectionException(RequestType.FRIENDS);
			}
		} else {
			throw new APIRequestException(RequestType.FRIENDS,
					response.getCode(), response.getBody());
		}

		return result;
	}

	public User getUser(long id) throws APIRequestException,
			BadConnectionException {
		User result = null;
		OAuthRequest req = new OAuthRequest(Verb.GET, Constants.URI_SINGLE_USER);
		req.addQuerystringParameter("user_id", String.valueOf(id));
		req.addQuerystringParameter("include_entities", "true");

		service.signRequest(token, req);
		Response response = req.send();

		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(), User.class);
			} catch (IllegalStateException e) {
				throw new BadConnectionException(RequestType.SINGLE_USER);
			}
		} else {
			throw new APIRequestException(RequestType.SINGLE_USER,
					response.getCode(), response.getBody());
		}

		return result;
	}

	public User getUser(String screenName) throws APIRequestException,
			BadConnectionException {
		User result = null;
		OAuthRequest req = new OAuthRequest(Verb.GET, Constants.URI_SINGLE_USER);
		req.addQuerystringParameter("screen_name", screenName);
		req.addQuerystringParameter("include_entities", "true");

		service.signRequest(token, req);
		Response response = req.send();

		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(), User.class);
			} catch (IllegalStateException e) {
				throw new BadConnectionException(RequestType.SINGLE_USER);
			}
		} else {
			throw new APIRequestException(RequestType.SINGLE_USER,
					response.getCode(), response.getBody());
		}

		return result;
	}

	public TwitterConfig getConfiguration() throws APIRequestException,
			BadConnectionException {
		TwitterConfig result = null;
		OAuthRequest req = new OAuthRequest(Verb.GET,
				Constants.URI_CONFIGURATION);

		service.signRequest(token, req);
		Response response = req.send();

		if (response.isSuccessful()) {
			try {
				result = JSON.parseObject(response.getBody(),
						TwitterConfig.class);
			} catch (IllegalStateException e) {
				throw new BadConnectionException(RequestType.CONFIG);
			}
		} else {
			throw new APIRequestException(RequestType.CONFIG,
					response.getCode(), response.getBody());
		}

		return result;
	}

}
