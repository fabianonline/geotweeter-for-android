package de.fabianonline.geotweeter;

import java.util.ArrayList;
import java.util.List;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import android.location.Location;
import android.os.Handler;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import de.fabianonline.geotweeter.exceptions.TweetSendException;

public class Account {
	protected final String LOG = "Account";
	public static ArrayList<Account> all_accounts = new ArrayList<Account>();
	private Token token;
	private OAuthService service = new ServiceBuilder().provider(TwitterApi.class).apiKey(Constants.API_KEY).apiSecret(Constants.API_SECRET).debug().build();
	private TimelineElementAdapter elements;
	private Handler handler;
	private StreamRequest stream_request;
	private long max_read_tweet_id = 0;
	private long max_read_dm_id = 0;
	private long max_known_tweet_id = 0;
	private long min_known_tweet_id = -1;
	private long max_known_dm_id = 0;
	private long min_known_dm_id = -1;
	
	public Account(TimelineElementAdapter elements, Token token) {
		this.token = token;
		handler = new Handler();
		this.elements = elements;
		TimelineRefreshThread t = new TimelineRefreshThread();
		new Thread(t).start();
		stream_request = new StreamRequest(this);
		//stream_request.start();
		all_accounts.add(this);
	}
	
	public void signRequest(OAuthRequest request) {
		service.signRequest(token, request);
	}
	
	public void stopStream() {
		stream_request.stop();
	}
    
	private class TimelineRefreshThread implements Runnable {
		protected boolean do_update_bottom = false;
		protected ArrayList<ArrayList<TimelineElement>> responses = new ArrayList<ArrayList<TimelineElement>>();
		protected ArrayList<TimelineElement> main_data = new ArrayList<TimelineElement>();
		protected int count_running_threads = 0;
		protected int count_errored_threads = 0;
		
		public TimelineRefreshThread(boolean do_update_bottom) {
			this.do_update_bottom = do_update_bottom;
		}
		
		@Override
		public void run() {
			
			
			
		}
		
		private class RunnableAfterAllRequestsCompleted {
			public void run() {
				if (!main_data.isEmpty()) {
					responses.add(0, main_data);
				}
				if (count_errored_threads==0) {
					parseData(responses);
					stream_request.start();
				} else {
					// TODO Try again after some time
					// TODO Show info message
				}
			}
		}
		
		private class RunnableAfterEachSuccessfulRequest {
			private boolean is_main_data;
			
			public RunnableAfterEachSuccessfulRequest(boolean is_main_data) {
				this.is_main_data = is_main_data;
			}

			public void run(ArrayList<TimelineElement> elements) {
				if (is_main_data) {
					main_data = elements;
				} else {
					responses.add(elements);
				}
				count_running_threads--;
				if (count_running_threads==0) {
					new RunnableAfterAllRequestsCompleted().run();
				}
			}
		}
		
		private class RunnableAfterEachErroredRequest {
			public void run() {
				count_running_threads--;
				count_errored_threads++;
				// TODO Show error message
				if (count_running_threads==0) {
					new RunnableAfterAllRequestsCompleted().run();
				}
			}
		}
	}
	
	protected void parseData(ArrayList<ArrayList<TimelineElement>> responses) {
	};
			/*

		# `error` is run whenever a request finished with an error.
		# Set some default parameters for all request.
		default_parameters = {
			# We want to get RTs in the timeline
			include_rts: true
			count: 200
			# Entities contain information about unshortened t.co-Links and
			# so on. Naturally, we want those, too.
			include_entities: true
			page: 1
			# If `@max_known_tweet_id` is set, we already know some tweets.
			# So we set `since_id` to only get new tweets.
			since_id: @max_known_tweet_id unless @max_known_tweet_id=="0" || options.fill_bottom?
			max_id: @min_known_tweet_id.decrement() if options.fill_bottom?
		}

		# Define all the necessary requests to be made. `extra_parameters` can
		# be set to override the `default_parameters` for this request.
		requests = [
			{
				url: "statuses/home_timeline.json"
				name: "home_timeline"
				main_data: true
			}
			{
				url: "statuses/mentions.json"
				name: "mentions"
			}
			{
				url: "direct_messages.json"
				name: "Received DMs"
				extra_parameters: {
					# You (at least I) don't get soo much DMs, so 100 is
					# enough.
					count: 100
					# DMs have their own IDs, so we use `@max_known_dm_id`
					# here.
					since_id: @max_known_dm_id if @max_known_dm_id? && !options.fill_bottom?
					max_id: @min_known_dm_id.decrement() if @min_known_dm_id? && options.fill_bottom?
				}
			}
			{
				url: "direct_messages/sent.json"
				name: "Sent DMs"
				extra_parameters: {
					count: 100
					since_id: @max_known_dm_id if @max_known_dm_id? && !options.fill_bottom?
					max_id: @min_known_dm_id.decrement() if @min_known_dm_id? && options.fill_bottom?
				}
			}
		]

		# Number of threads to be started. Will be reduced by one every time
		# one of the threads finishes. If zero, all requests are done.
		threads_running = requests.length

		# Do the actual requests.
		for request in requests
			parameters = {}
			# Construct a new parameters object with `default_parameters`
			# extended by `extra_parameters`.
			parameters[key] = value for key, value of default_parameters when value
			parameters[key] = value for key, value of request.extra_parameters when value
			additional_info = {name: request.name}
			additional_info.main_data = true if request.main_data
			@twitter_request(request.url, {
				method: "GET"
				parameters: parameters
				dataType: "text"
				silent: true
				additional_info: additional_info
				success: success
				error: error
			})

	# `parse_data` gets an array of (arrays of) responses from the Twitter API
	# (generated by `fill_list` and `StreamRequest`), sorts them, gets matching
	# objects, calls `get_html` on them and outputs the HTML.
	parse_data: (json, options={}) ->
		# If we didn't get an array of responses, we create one.
		json = [json] unless json.constructor==Array
		responses = []
		# We still have only JSON code, no real object. So we go through the
		# array and parse all the JSON.
		for json_data in json
			try temp = $.parseJSON(json_data)
			continue unless temp?
			if temp.constructor == Array
				# Did we get an array of messages or just one?
				if temp.length>0
					temp_elements = []
					# `TwitterMessage.get_object` will determine the type of
					# an object and return a matching "real" object.
					# This is done for alle elements of this array.
					temp_elements.push(TwitterMessage.get_object(data, this)) for data in temp
					responses.push(temp_elements)
			else
				# Just one message... So let's try to parse it.
				object = TwitterMessage.get_object(temp, this)
				# We could get an `undefined` back (this happens e.g. to that
				# friends array you get on connecting to the Streaming API).
				# Since that would be the only object, we just go on.
				continue unless object?
				responses.push([object])
		return if responses.length==0
		return if responses.length==1 && responses[0][0]==null

		# By now we have all requests in a 2-dimensional array `responses`.
		# The first dimension comes from multiple requests in `fill_array`.
		# Data from a `StreamRequest` will always have just one element in
		# the first dimension.
		# Anyway, we have to get through the multiple arrays and sort the
		# tweets in there. Let's go.
		all_objects = []
		html = $('<div>')
		last_id = ""
		while responses.length > 0
			# Save the Date and the index of the array holding the newest
			# element.
			newest_date = null
			newest_index = null
			for index, array of responses
				object = array[0]
				if newest_date==null || object.get_date()>newest_date
					newest_date = object.get_date()
					newest_index = index
			# Retrieve the first object of the winning array (a.k.a. the newest
			# element).
			array = responses[newest_index]
			object = array.shift()
			# If this array has become empty, we remove it from `responses`.
			responses.splice(newest_index, 1) if array.length==0

			# Wenn `clip` aktiviert ist und das erste ("wichtigste") Array leer
			# ist, brechen wir ab.
			if array.length==0 && newest_index=="0" && options.clip?
				# Vorher müssen wir jedoch zusehen, `max_known_tweet_id` und
				# `max_known_dm_id` zu setzen (falls sie noch leer sind).
				if @max_known_tweet_id == "0"
					for array in responses
						object = array[0]
						@max_known_tweet_id = object.id if object.constructor==Tweet && object.id.is_bigger_than(@max_known_tweet_id)
				if @max_known_dm_id == "0"
					for array in responses
						object = array[0]
						@max_known_dm_id = object.id if object.constructor==DirectMessage && object.id.is_bigger_than(@max_known_dm_id)
				# Jetzt können wir aber wirklich abbrechen.
				break

			this_id = object.id
			# Add the html to the temporary html code. But look out for duplicate
			# tweets (e.g. mentions from friends will be in `home_timeline` as
			# well as in `mentions`).
			html.append(object.get_html()) unless this_id==old_id
			all_objects.push(object)
			# If we have a `Tweet` or `DirectMessage`, note it's `id`, if
			# necessary.
			if object.constructor==Tweet
				@max_known_tweet_id=object.id if object.id.is_bigger_than(@max_known_tweet_id)
				@my_last_tweet_id=object.id if object.sender.id==@user.id && object.id.is_bigger_than(@my_last_tweet_id)
				@min_known_tweet_id=object.id unless @min_known_tweet_id? && object.id.is_bigger_than(@min_known_tweet_id)
			if object.constructor==DirectMessage
				@max_known_dm_id=object.id if object.id.is_bigger_than(@max_known_dm_id)
				@min_known_dm_id=object.id unless @min_known_dm_id? && object.id.is_bigger_than(@min_known_dm_id)
			# Save this id for recognizing duplicates in the next round.
			old_id = this_id
		# After we are done with all tweets, add the collected html to the DOM.
		@add_html(html, options.fill_bottom?)
		# Update the counter to display the right number of unread tweets.
		@update_user_counter()
		# Call `add_to_collections` (again) in order to correctly link this tweet
		all_objects.reverse()
		object.add_to_collections?() for object in all_objects */
			/*OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.twitter.com/1/statuses/home_timeline.json");
		    signRequest(request);
		    Response response = request.send();
		    Log.d(LOG,response.getBody());*/
			String result = "{\"created_at\":\"Mon Sep 17 21:43:03 +0000 2012\",\"id\":247812938398842882,\"id_str\":\"247812938398842882\",\"text\":\"2. Fußball-Bundesliga: Köln und St. Pauli trennen sich torlos http://t.co/h9AwKCkJ\",\"source\":\"<a href=\\\"http://www.tagesschau.de\\\" rel=\\\"nofollow\\\">tagesschau.de<003c/a>\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":5734902,\"id_str\":\"5734902\",\"name\":\"tagesschau\",\"screen_name\":\"tagesschau\",\"location\":\"Hamburg\",\"url\":\"http://www.tagesschau.de\",\"description\":\"Die Nachrichten der ARD\",\"protected\":false,\"followers_count\":82720,\"friends_count\":4,\"listed_count\":3528,\"created_at\":\"Thu May 03 08:42:42 +0000 2007\",\"favourites_count\":0,\"utc_offset\":3600,\"time_zone\":\"Berlin\",\"geo_enabled\":false,\"verified\":true,\"statuses_count\":51426,\"lang\":\"de\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"5985DF\",\"profile_background_image_url\":\"http://a0.twimg.com/images/themes/theme1/bg.png\",\"profile_background_image_url_https\":\"https://si0.twimg.com/images/themes/theme1/bg.png\",\"profile_background_tile\":true,\"profile_image_url\":\"http://a0.twimg.com/profile_images/1704199445/mzl.lbaptnoh_normal.png\",\"profile_image_url_https\":\"https://si0.twimg.com/profile_images/1704199445/mzl.lbaptnoh_normal.png\",\"profile_link_color\":\"0000FF\",\"profile_sidebar_border_color\":\"00044B\",\"profile_sidebar_fill_color\":\"E2EBF7\",\"profile_text_color\":\"00044B\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":true,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false}," +
					"{\"created_at\":\"Mon Sep 17 21:38:41 +0000 2012\",\"id\":247811839210188800,\"id_str\":\"247811839210188800\",\"text\":\"Didn't think you needed a boat? Think again: http://t.co/l6OjzNDj\",\"source\":\"<a href=\\\"http://www.tweetdeck.com\\\" rel=\\\"nofollow\\\">TweetDeck</a>\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":12611642,\"id_str\":\"12611642\",\"name\":\"thinkgeek\",\"screen_name\":\"thinkgeek\",\"location\":\"Fairfax, VA\",\"url\":\"http://www.thinkgeek.com\",\"description\":\"Cool products for technophiles, geeks, and the occasional monkey. Follow @thinkgeekspam for our new product feed.\",\"protected\":false,\"followers_count\":588445,\"friends_count\":273336,\"listed_count\":15444,\"created_at\":\"Wed Jan 23 20:36:46 +0000 2008\",\"favourites_count\":102,\"utc_offset\":-18000,\"time_zone\":\"Eastern Time (US & Canada)\",\"geo_enabled\":false,\"verified\":true,\"statuses_count\":60725,\"lang\":\"en\",\"contributors_enabled\":true,\"is_translator\":false,\"profile_background_color\":\"000000\",\"profile_background_image_url\":\"http://a0.twimg.com/profile_background_images/155178143/twitter_bg_new.jpg\",\"profile_background_image_url_https\":\"https://si0.twimg.com/profile_background_images/155178143/twitter_bg_new.jpg\",\"profile_background_tile\":false,\"profile_image_url\":\"http://a0.twimg.com/profile_images/2548703056/016e1127zt35duxbyl9b_normal.png\",\"profile_image_url_https\":\"https://si0.twimg.com/profile_images/2548703056/016e1127zt35duxbyl9b_normal.png\",\"profile_link_color\":\"359DC8\",\"profile_sidebar_border_color\":\"1A1A1A\",\"profile_sidebar_fill_color\":\"1A1A1A\",\"profile_text_color\":\"9E730B\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":true,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":231,\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false}," +
					"{\"created_at\":\"Mon Sep 17 21:37:54 +0000 2012\",\"id\":247811644443463680,\"id_str\":\"247811644443463680\",\"text\":\"Wer macht denn sowas? O.o https://t.co/iPFKci6\",\"source\":\"web\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":44625441,\"id_str\":\"44625441\",\"name\":\"Juri\",\"screen_name\":\"tuxwurf\",\"location\":\"Münster\",\"url\":\"http://tuxwurf.de\",\"description\":\"<<du alter Poet mit einem Hang zum magischen Realismus...>>\",\"protected\":true,\"followers_count\":715,\"friends_count\":311,\"listed_count\":104,\"created_at\":\"Thu Jun 04 14:20:20 +0000 2009\",\"favourites_count\":73,\"utc_offset\":3600,\"time_zone\":\"Berlin\",\"geo_enabled\":true,\"verified\":false,\"statuses_count\":35932,\"lang\":\"de\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"131516\",\"profile_background_image_url\":\"http://a0.twimg.com/images/themes/theme14/bg.gif\",\"profile_background_image_url_https\":\"https://si0.twimg.com/images/themes/theme14/bg.gif\",\"profile_background_tile\":true,\"profile_image_url\":\"http://a0.twimg.com/profile_images/2461094497/qihbrcfdbx7ank016xju_normal.jpeg\",\"profile_image_url_https\":\"https://si0.twimg.com/profile_images/2461094497/qihbrcfdbx7ank016xju_normal.jpeg\",\"profile_link_color\":\"009999\",\"profile_sidebar_border_color\":\"EEEEEE\",\"profile_sidebar_fill_color\":\"EFEFEF\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":true,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false}," +
					"{\"created_at\":\"Mon Sep 17 21:36:39 +0000 2012\",\"id\":247811327823855617,\"id_str\":\"247811327823855617\",\"text\":\"Earthquake M 5.0, Vanuatu: September 17, 2012 21:15:45 GMT\",\"source\":\"<a href=\\\"http://www.google.com/\\\" rel=\\\"nofollow\\\">Google</a>\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":354165935,\"id_str\":\"354165935\",\"name\":\"EQ\",\"screen_name\":\"Latestquake\",\"location\":\"\",\"url\":null,\"description\":\"USGS Latest Earthquakes: Feeds & Data\",\"protected\":false,\"followers_count\":624,\"friends_count\":0,\"listed_count\":4,\"created_at\":\"Sat Aug 13 07:21:18 +0000 2011\",\"favourites_count\":1,\"utc_offset\":0,\"time_zone\":\"Edinburgh\",\"geo_enabled\":false,\"verified\":false,\"statuses_count\":7917,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"1A1B1F\",\"profile_background_image_url\":\"http://a0.twimg.com/profile_background_images/351520164/Earthquake__1_.jpg\",\"profile_background_image_url_https\":\"https://si0.twimg.com/profile_background_images/351520164/Earthquake__1_.jpg\",\"profile_background_tile\":false,\"profile_image_url\":\"http://a0.twimg.com/profile_images/1602675032/brokenearth_normal.jpg\",\"profile_image_url_https\":\"https://si0.twimg.com/profile_images/1602675032/brokenearth_normal.jpg\",\"profile_link_color\":\"2FC2EF\",\"profile_sidebar_border_color\":\"181A1E\",\"profile_sidebar_fill_color\":\"252429\",\"profile_text_color\":\"666666\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":true,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorited\":false,\"retweeted\":false}," +
					"{\"created_at\":\"Mon Sep 17 21:27:11 +0000 2012\",\"id\":247808945782132737,\"id_str\":\"247808945782132737\",\"text\":\"Check out our boy! <<@grantimahara: In which I welcome @Revision3 to our @Discovery family... GANGNAM STYLE. VIDEO http://t.co/1kxQyDpJ>>\",\"source\":\"<a href=\\\"http://twitter.com/download/iphone\\\" rel=\\\"nofollow\\\">Twitter for iPhone</a>\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":60101131,\"id_str\":\"60101131\",\"name\":\"Tory Belleci\",\"screen_name\":\"ToryBelleci\",\"location\":\"San Francisco\",\"url\":\"http://www.facebook.com/pages/Tory-Belleci/151737058228942\",\"description\":\"Mythbuster, ninja assassin, and a guy who just likes to blow stuff up.\",\"protected\":false,\"followers_count\":165606,\"friends_count\":122,\"listed_count\":3373,\"created_at\":\"Sat Jul 25 17:13:26 +0000 2009\",\"favourites_count\":0,\"utc_offset\":-28800,\"time_zone\":\"Pacific Time (US & Canada)\",\"geo_enabled\":false,\"verified\":true,\"statuses_count\":607,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_image_url\":\"http://a0.twimg.com/profile_background_images/240060197/Twitter_background.jpg\",\"profile_background_image_url_https\":\"https://si0.twimg.com/profile_background_images/240060197/Twitter_background.jpg\",\"profile_background_tile\":true,\"profile_image_url\":\"http://a0.twimg.com/profile_images/1372346118/Hi-ResMYTHBUSTERStory_normal.jpg\",\"profile_image_url_https\":\"https://si0.twimg.com/profile_images/1372346118/Hi-ResMYTHBUSTERStory_normal.jpg\",\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":true,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":45,\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false}," +
					"{\"created_at\":\"Mon Sep 17 21:25:28 +0000 2012\",\"id\":247808513764642817,\"id_str\":\"247808513764642817\",\"text\":\"Did you guys see Grant's video?  He needs his own show.  #amazingviralvideo\",\"source\":\"<a href=\\\"http://twitter.com/download/iphone\\\" rel=\\\"nofollow\\\">Twitter for iPhone</a>\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":143244854,\"id_str\":\"143244854\",\"name\":\"Kari Byron\",\"screen_name\":\"KariByron\",\"location\":\"\",\"url\":null,\"description\":\"Host of MythBusters and Head Rush. Artist.\",\"protected\":false,\"followers_count\":216783,\"friends_count\":197,\"listed_count\":4021,\"created_at\":\"Thu May 13 00:11:57 +0000 2010\",\"favourites_count\":38,\"utc_offset\":-28800,\"time_zone\":\"Pacific Time (US & Canada)\",\"geo_enabled\":false,\"verified\":true,\"statuses_count\":910,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"022330\",\"profile_background_image_url\":\"http://a0.twimg.com/images/themes/theme15/bg.png\",\"profile_background_image_url_https\":\"https://si0.twimg.com/images/themes/theme15/bg.png\",\"profile_background_tile\":false,\"profile_image_url\":\"http://a0.twimg.com/profile_images/2422938880/yt375mf4pdj3g34zj5eg_normal.jpeg\",\"profile_image_url_https\":\"https://si0.twimg.com/profile_images/2422938880/yt375mf4pdj3g34zj5eg_normal.jpeg\",\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"A8C7F7\",\"profile_sidebar_fill_color\":\"C0DFEC\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":true,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":14,\"favorited\":false,\"retweeted\":false}," +
					"{\"created_at\":\"Mon Sep 17 21:24:16 +0000 2012\",\"id\":247808213179838465,\"id_str\":\"247808213179838465\",\"text\":\"\\\"Innocence of Muslims\\\": Breiter Widerstand gegen Filmvorführung http://t.co/SD5f0vgZ\",\"source\":\"<a href=\\\"http://www.tagesschau.de\\\" rel=\\\"nofollow\\\">tagesschau.de</a>\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":5734902,\"id_str\":\"5734902\",\"name\":\"tagesschau\",\"screen_name\":\"tagesschau\",\"location\":\"Hamburg\",\"url\":\"http://www.tagesschau.de\",\"description\":\"Die Nachrichten der ARD\",\"protected\":false,\"followers_count\":82720,\"friends_count\":4,\"listed_count\":3528,\"created_at\":\"Thu May 03 08:42:42 +0000 2007\",\"favourites_count\":0,\"utc_offset\":3600,\"time_zone\":\"Berlin\",\"geo_enabled\":false,\"verified\":true,\"statuses_count\":51426,\"lang\":\"de\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"5985DF\",\"profile_background_image_url\":\"http://a0.twimg.com/images/themes/theme1/bg.png\",\"profile_background_image_url_https\":\"https://si0.twimg.com/images/themes/theme1/bg.png\",\"profile_background_tile\":true,\"profile_image_url\":\"http://a0.twimg.com/profile_images/1704199445/mzl.lbaptnoh_normal.png\",\"profile_image_url_https\":\"https://si0.twimg.com/profile_images/1704199445/mzl.lbaptnoh_normal.png\",\"profile_link_color\":\"0000FF\",\"profile_sidebar_border_color\":\"00044B\",\"profile_sidebar_fill_color\":\"E2EBF7\",\"profile_text_color\":\"00044B\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":true,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":3,\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false}," +
					"{\"created_at\":\"Mon Sep 17 21:20:38 +0000 2012\",\"id\":247807298628616192,\"id_str\":\"247807298628616192\",\"text\":\"@marcbrewer Ok\",\"source\":\"<a href=\\\"http://www.tweetcaster.com\\\" rel=\\\"nofollow\\\">TweetCaster for Android</a>\",\"truncated\":false,\"in_reply_to_status_id\":247804231736778752,\"in_reply_to_status_id_str\":\"247804231736778752\",\"in_reply_to_user_id\":15999183,\"in_reply_to_user_id_str\":\"15999183\",\"in_reply_to_screen_name\":\"marcbrewer\",\"user\":{\"id\":81554965,\"id_str\":\"81554965\",\"name\":\"Jonas Sell\",\"screen_name\":\"johnassel\",\"location\":\"Dortmund\",\"url\":\"http://johnassel.de\",\"description\":\"Student @ FH Dortmund, Androidianer, Geocacher - den Nick spricht man btw Dschonässäl ;-)\",\"protected\":false,\"followers_count\":253,\"friends_count\":322,\"listed_count\":22,\"created_at\":\"Sun Oct 11 09:09:15 +0000 2009\",\"favourites_count\":73,\"utc_offset\":3600,\"time_zone\":\"Berlin\",\"geo_enabled\":true,\"verified\":false,\"statuses_count\":43687,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"798AB3\",\"profile_background_image_url\":\"http://a0.twimg.com/profile_background_images/602819867/mwdgntbisvo6xpzncbft.jpeg\",\"profile_background_image_url_https\":\"https://si0.twimg.com/profile_background_images/602819867/mwdgntbisvo6xpzncbft.jpeg\",\"profile_background_tile\":false,\"profile_image_url\":\"http://a0.twimg.com/profile_images/2586645258/zf1di463r7cba445hyzi_normal.png\",\"profile_image_url_https\":\"https://si0.twimg.com/profile_images/2586645258/zf1di463r7cba445hyzi_normal.png\",\"profile_link_color\":\"009999\",\"profile_sidebar_border_color\":\"EEEEEE\",\"profile_sidebar_fill_color\":\"EFEFEF\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":true,\"follow_request_sent\":null,\"notifications\":null},\"geo\":{\"type\":\"Point\",\"coordinates\":[51.4640584,7.4708008]},\"coordinates\":{\"type\":\"Point\",\"coordinates\":[7.4708008,51.4640584]},\"place\":{\"id\":\"b4fadeb3a3a29e2f\",\"url\":\"http://api.twitter.com/1/geo/id/b4fadeb3a3a29e2f.json\",\"place_type\":\"city\",\"name\":\"Dortmund\",\"full_name\":\"Dortmund, Dortmund\",\"country_code\":\"DE\",\"country\":\"Germany\",\"bounding_box\":{\"type\":\"Polygon\",\"coordinates\":[[[7.302443,51.415504],[7.638168,51.415504],[7.638168,51.599943],[7.302443,51.599943]]]},\"attributes\":{}},\"contributors\":null,\"retweet_count\":0,\"favorited\":false,\"retweeted\":false}," +
					"{\"created_at\":\"Mon Sep 17 21:19:47 +0000 2012\",\"id\":247807082152222720,\"id_str\":\"247807082152222720\",\"text\":\"Earthquake M 2.8, Central California: September 17, 2012 21:12:29 GMT\",\"source\":\"<a href=\\\"http://www.google.com/\\\" rel=\\\"nofollow\\\">Google</a>\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":354165935,\"id_str\":\"354165935\",\"name\":\"EQ\",\"screen_name\":\"Latestquake\",\"location\":\"\",\"url\":null,\"description\":\"USGS Latest Earthquakes: Feeds & Data\",\"protected\":false,\"followers_count\":624,\"friends_count\":0,\"listed_count\":4,\"created_at\":\"Sat Aug 13 07:21:18 +0000 2011\",\"favourites_count\":1,\"utc_offset\":0,\"time_zone\":\"Edinburgh\",\"geo_enabled\":false,\"verified\":false,\"statuses_count\":7917,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"1A1B1F\",\"profile_background_image_url\":\"http://a0.twimg.com/profile_background_images/351520164/Earthquake__1_.jpg\",\"profile_background_image_url_https\":\"https://si0.twimg.com/profile_background_images/351520164/Earthquake__1_.jpg\",\"profile_background_tile\":false,\"profile_image_url\":\"http://a0.twimg.com/profile_images/1602675032/brokenearth_normal.jpg\",\"profile_image_url_https\":\"https://si0.twimg.com/profile_images/1602675032/brokenearth_normal.jpg\",\"profile_link_color\":\"2FC2EF\",\"profile_sidebar_border_color\":\"181A1E\",\"profile_sidebar_fill_color\":\"252429\",\"profile_text_color\":\"666666\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":true,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorited\":false,\"retweeted\":false}," +
					"{\"created_at\":\"Mon Sep 17 21:17:27 +0000 2012\",\"id\":247806496786767872,\"id_str\":\"247806496786767872\",\"text\":\"RT @ennolenze: Hat einer der Verfolger mal für Frontex gearbeitet und interesse an einem Gespräch?\",\"source\":\"<a href=\\\"http://tapbots.com/tweetbot\\\" rel=\\\"nofollow\\\">Tweetbot for iOS</a>\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":14305613,\"id_str\":\"14305613\",\"name\":\"Philip Brechler\",\"screen_name\":\"plaetzchen\",\"location\":\"Berlin, Germany\",\"url\":\"http://plaetzchen.cc\",\"description\":\"IT Specialist, iOS Developer @hoccer, Geek, Pirate.\",\"protected\":false,\"followers_count\":3456,\"friends_count\":1249,\"listed_count\":325,\"created_at\":\"Fri Apr 04 19:42:20 +0000 2008\",\"favourites_count\":389,\"utc_offset\":3600,\"time_zone\":\"Berlin\",\"geo_enabled\":true,\"verified\":false,\"statuses_count\":38247,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"263F78\",\"profile_background_image_url\":\"http://a0.twimg.com/profile_background_images/82790642/twitter_back.jpg\",\"profile_background_image_url_https\":\"https://si0.twimg.com/profile_background_images/82790642/twitter_back.jpg\",\"profile_background_tile\":false,\"profile_image_url\":\"http://a0.twimg.com/profile_images/1557456954/avatar_neu_klein_normal.png\",\"profile_image_url_https\":\"https://si0.twimg.com/profile_images/1557456954/avatar_neu_klein_normal.png\",\"profile_link_color\":\"2FC2EF\",\"profile_sidebar_border_color\":\"181A1E\",\"profile_sidebar_fill_color\":\"252429\",\"profile_text_color\":\"666666\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":true,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweeted_status\":{\"created_at\":\"Mon Sep 17 21:17:13 +0000 2012\",\"id\":247806438720819200,\"id_str\":\"247806438720819200\",\"text\":\"Hat einer der Verfolger mal für Frontex gearbeitet und interesse an einem Gespräch?\",\"source\":\"<a href=\\\"http://www.tweetdeck.com\\\" rel=\\\"nofollow\\\">TweetDeck</a>\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":15761692,\"id_str\":\"15761692\",\"name\":\"Enno Lenze\",\"screen_name\":\"ennolenze\",\"location\":\"52.373625,9.986435\",\"url\":\"http://enno-lenze.de\",\"description\":\"Verleger, CCC, Piratenpartei, Motorrad, Party, Berlin und alles dazwischen.   Oder per Jabber: enno@jabber.verbrennung.org \",\"protected\":false,\"followers_count\":1858,\"friends_count\":164,\"listed_count\":103,\"created_at\":\"Thu Aug 07 10:02:44 +0000 2008\",\"favourites_count\":1583,\"utc_offset\":3600,\"time_zone\":\"Berlin\",\"geo_enabled\":true,\"verified\":false,\"statuses_count\":13621,\"lang\":\"de\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"000000\",\"profile_background_image_url\":\"http://a0.twimg.com/profile_background_images/20897392/mqgritandflame.br.jpg\",\"profile_background_image_url_https\":\"https://si0.twimg.com/profile_background_images/20897392/mqgritandflame.br.jpg\",\"profile_background_tile\":true,\"profile_image_url\":\"http://a0.twimg.com/profile_images/1675362176/enno_normal.jpg\",\"profile_image_url_https\":\"https://si0.twimg.com/profile_images/1675362176/enno_normal.jpg\",\"profile_link_color\":\"FF8105\",\"profile_sidebar_border_color\":\"FF8105\",\"profile_sidebar_fill_color\":\"FFF69E\",\"profile_text_color\":\"111111\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":5,\"favorited\":false,\"retweeted\":false},\"retweet_count\":5,\"favorited\":false,\"retweeted\":false}," +
					"{\"created_at\":\"Mon Sep 17 21:15:09 +0000 2012\",\"id\":247805919688278016,\"id_str\":\"247805919688278016\",\"text\":\"Hey, look what we've created! Neatorama 2013 Daily Desk Calendar http://t.co/xP5yM8Ul\",\"source\":\"<a href=\\\"http://www.tweetdeck.com\\\" rel=\\\"nofollow\\\">TweetDeck</a>\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":14512559,\"id_str\":\"14512559\",\"name\":\"neatorama\",\"screen_name\":\"neatorama\",\"location\":\"Southern Cali\",\"url\":\"http://www.neatorama.com\",\"description\":\"Neatorama's Official tweetage. Neato stuff, 140 characters at a time.\",\"protected\":false,\"followers_count\":17663,\"friends_count\":10258,\"listed_count\":985,\"created_at\":\"Thu Apr 24 14:53:42 +0000 2008\",\"favourites_count\":4,\"utc_offset\":-18000,\"time_zone\":\"Eastern Time (US & Canada)\",\"geo_enabled\":false,\"verified\":false,\"statuses_count\":11167,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_image_url\":\"http://a0.twimg.com/profile_background_images/82341804/large2.jpg\",\"profile_background_image_url_https\":\"https://si0.twimg.com/profile_background_images/82341804/large2.jpg\",\"profile_background_tile\":true,\"profile_image_url\":\"http://a0.twimg.com/profile_images/789687979/Picture_2_normal.png\",\"profile_image_url_https\":\"https://si0.twimg.com/profile_images/789687979/Picture_2_normal.png\",\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":true,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false}," +
					//"{\"created_at\":\"Mon Sep 17 21:14:56 +0000 2012\",\"id\":247805863186796544,\"id_str\":\"247805863186796544\",\"text\":\"I have driven 69 metres in the last three days. And had to stop there because NASA LOVES TO SNICKER AT NUMBERS.\",\"source\":\"<a href=\\\"http://itunes.apple.com/us/app/twitter/id409789998?mt=12\\\" rel=\\\"nofollow\\\">Twitter for Mac</a>\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":740109097,\"id_str\":\"740109097\",\"name\":\"SarcasticRover\",\"screen_name\":\"SarcasticRover\",\"location\":\"4th Rock From the Sun\",\"url\":\"http://www.sarcasticrover.com\",\"description\":\"I'm on Mars, whoop-dee-fricken-doo.\nNot the real @MarsCuriosity.\nProfile Pic by the amazing @mandystobo !\",\"protected\":false,\"followers_count\":96002,\"friends_count\":443,\"listed_count\":990,\"created_at\":\"Mon Aug 06 07:58:05 +0000 2012\",\"favourites_count\":61,\"utc_offset\":-25200,\"time_zone\":\"Mountain Time (US & Canada)\",\"geo_enabled\":false,\"verified\":false,\"statuses_count\":866,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"0099B9\",\"profile_background_image_url\":\"http://a0.twimg.com/images/themes/theme4/bg.gif\",\"profile_background_image_url_https\":\"https://si0.twimg.com/images/themes/theme4/bg.gif\",\"profile_background_tile\":false,\"profile_image_url\":\"http://a0.twimg.com/profile_images/2489313748/kcd8565sp9t7e0h3n39i_normal.jpeg\",\"profile_image_url_https\":\"https://si0.twimg.com/profile_images/2489313748/kcd8565sp9t7e0h3n39i_normal.jpeg\",\"profile_link_color\":\"0099B9\",\"profile_sidebar_border_color\":\"5ED4DC\",\"profile_sidebar_fill_color\":\"95E8EC\",\"profile_text_color\":\"3C3940\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":true,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":105,\"favorited\":false,\"retweeted\":false}," +
					//"{\"created_at\":\"Mon Sep 17 21:12:17 +0000 2012\",\"id\":247805194904141824,\"id_str\":\"247805194904141824\",\"text\":\"Going the Distance (not necessarily going for speed). Over the last three sols, I've driven 226 feet (about 69 meters) #MSL\",\"source\":\"<a href=\\\"http://itunes.apple.com/us/app/twitter/id409789998?mt=12\\\" rel=\\\"nofollow\\\">Twitter for Mac</a>\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":15473958,\"id_str\":\"15473958\",\"name\":\"Curiosity Rover\",\"screen_name\":\"MarsCuriosity\",\"location\":\"Gale Crater, Mars\",\"url\":\"http://mars.jpl.nasa.gov/msl/\",\"description\":\"NASA's latest mission to Mars. I arrived at the Red Planet, Aug. 5, 2012 PDT (Aug.6 UTC).\",\"protected\":false,\"followers_count\":1150421,\"friends_count\":153,\"listed_count\":12389,\"created_at\":\"Thu Jul 17 21:18:10 +0000 2008\",\"favourites_count\":51,\"utc_offset\":-28800,\"time_zone\":\"Pacific Time (US & Canada)\",\"geo_enabled\":false,\"verified\":true,\"statuses_count\":1353,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"0099B9\",\"profile_background_image_url\":\"http://a0.twimg.com/profile_background_images/3274232/MSL_Bg_v1_export.jpg\",\"profile_background_image_url_https\":\"https://si0.twimg.com/profile_background_images/3274232/MSL_Bg_v1_export.jpg\",\"profile_background_tile\":false,\"profile_image_url\":\"http://a0.twimg.com/profile_images/2588037225/sgjzyb4ewvsqiqlroxqn_normal.jpeg\",\"profile_image_url_https\":\"https://si0.twimg.com/profile_images/2588037225/sgjzyb4ewvsqiqlroxqn_normal.jpeg\",\"profile_link_color\":\"0099B9\",\"profile_sidebar_border_color\":\"5ED4DC\",\"profile_sidebar_fill_color\":\"95E8EC\",\"profile_text_color\":\"3C3940\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":true,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":270,\"favorited\":false,\"retweeted\":false}," +
					//"{\"created_at\":\"Mon Sep 17 21:08:41 +0000 2012\",\"id\":247804288796073984,\"id_str\":\"247804288796073984\",\"text\":\"Jetzt noch das aktuelle Chaosradio\",\"source\":\"<a href=\\\"http://www.tweetdeck.com\\\" rel=\\\"nofollow\\\">TweetDeck</a>\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":15999183,\"id_str\":\"15999183\",\"name\":\"Marc\",\"screen_name\":\"marcbrewer\",\"location\":\"Germany, NRW\",\"url\":null,\"description\":\"Student (Informatik), Webworker, Entwickler\",\"protected\":false,\"followers_count\":140,\"friends_count\":129,\"listed_count\":10,\"created_at\":\"Tue Aug 26 16:57:28 +0000 2008\",\"favourites_count\":55,\"utc_offset\":-10800,\"time_zone\":\"Greenland\",\"geo_enabled\":true,\"verified\":false,\"statuses_count\":13125,\"lang\":\"de\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"131516\",\"profile_background_image_url\":\"http://a0.twimg.com/images/themes/theme14/bg.gif\",\"profile_background_image_url_https\":\"https://si0.twimg.com/images/themes/theme14/bg.gif\",\"profile_background_tile\":true,\"profile_image_url\":\"http://a0.twimg.com/profile_images/2214555721/190303449107569855_55131446_normal.png\",\"profile_image_url_https\":\"https://si0.twimg.com/profile_images/2214555721/190303449107569855_55131446_normal.png\",\"profile_link_color\":\"009999\",\"profile_sidebar_border_color\":\"EEEEEE\",\"profile_sidebar_fill_color\":\"EFEFEF\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":true,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorited\":false,\"retweeted\":false}," +
					//"{\"created_at\":\"Mon Sep 17 21:08:27 +0000 2012\",\"id\":247804231736778752,\"id_str\":\"247804231736778752\",\"text\":\"Upload läuft weiter @johnassel\",\"source\":\"<a href=\\\"http://www.tweetdeck.com\\\" rel=\\\"nofollow\\\">TweetDeck</a>\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":15999183,\"id_str\":\"15999183\",\"name\":\"Marc\",\"screen_name\":\"marcbrewer\",\"location\":\"Germany, NRW\",\"url\":null,\"description\":\"Student (Informatik), Webworker, Entwickler\",\"protected\":false,\"followers_count\":140,\"friends_count\":129,\"listed_count\":10,\"created_at\":\"Tue Aug 26 16:57:28 +0000 2008\",\"favourites_count\":55,\"utc_offset\":-10800,\"time_zone\":\"Greenland\",\"geo_enabled\":true,\"verified\":false,\"statuses_count\":13125,\"lang\":\"de\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"131516\",\"profile_background_image_url\":\"http://a0.twimg.com/images/themes/theme14/bg.gif\",\"profile_background_image_url_https\":\"https://si0.twimg.com/images/themes/theme14/bg.gif\",\"profile_background_tile\":true,\"profile_image_url\":\"http://a0.twimg.com/profile_images/2214555721/190303449107569855_55131446_normal.png\",\"profile_image_url_https\":\"https://si0.twimg.com/profile_images/2214555721/190303449107569855_55131446_normal.png\",\"profile_link_color\":\"009999\",\"profile_sidebar_border_color\":\"EEEEEE\",\"profile_sidebar_fill_color\":\"EFEFEF\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":true,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorited\":false,\"retweeted\":false}," +
					//"{\"created_at\":\"Mon Sep 17 21:05:25 +0000 2012\",\"id\":247803469367484416,\"id_str\":\"247803469367484416\",\"text\":\"Mit 5 Leuten machts gleich mehr Bock #Arma2 :D\",\"source\":\"<a href=\\\"http://www.tweetdeck.com\\\" rel=\\\"nofollow\\\">TweetDeck</a>\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":15999183,\"id_str\":\"15999183\",\"name\":\"Marc\",\"screen_name\":\"marcbrewer\",\"location\":\"Germany, NRW\",\"url\":null,\"description\":\"Student (Informatik), Webworker, Entwickler\",\"protected\":false,\"followers_count\":140,\"friends_count\":129,\"listed_count\":10,\"created_at\":\"Tue Aug 26 16:57:28 +0000 2008\",\"favourites_count\":55,\"utc_offset\":-10800,\"time_zone\":\"Greenland\",\"geo_enabled\":true,\"verified\":false,\"statuses_count\":13125,\"lang\":\"de\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"131516\",\"profile_background_image_url\":\"http://a0.twimg.com/images/themes/theme14/bg.gif\",\"profile_background_image_url_https\":\"https://si0.twimg.com/images/themes/theme14/bg.gif\",\"profile_background_tile\":true,\"profile_image_url\":\"http://a0.twimg.com/profile_images/2214555721/190303449107569855_55131446_normal.png\",\"profile_image_url_https\":\"https://si0.twimg.com/profile_images/2214555721/190303449107569855_55131446_normal.png\",\"profile_link_color\":\"009999\",\"profile_sidebar_border_color\":\"EEEEEE\",\"profile_sidebar_fill_color\":\"EFEFEF\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":true,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorited\":false,\"retweeted\":false}," +
					//"{\"created_at\":\"Mon Sep 17 21:00:14 +0000 2012\",\"id\":247802162552397824,\"id_str\":\"247802162552397824\",\"text\":\"Es ist 23:00 Uhr. Temperatur: 12.1°C (-0.7°C). \nVergleich zu gestern: -1.2°C.\",\"source\":\"<a href=\\\"http://wetter.f00bian.de\\\" rel=\\\"nofollow\\\">Hennener Wetter</a>\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":325160483,\"id_str\":\"325160483\",\"name\":\"Wetter in IS-Hennen\",\"screen_name\":\"WetterInHennen\",\"location\":\"\",\"url\":null,\"description\":\"\",\"protected\":false,\"followers_count\":5,\"friends_count\":1,\"listed_count\":2,\"created_at\":\"Mon Jun 27 20:52:54 +0000 2011\",\"favourites_count\":0,\"utc_offset\":-10800,\"time_zone\":\"Greenland\",\"geo_enabled\":true,\"verified\":false,\"statuses_count\":10085,\"lang\":\"de\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_image_url\":\"http://a0.twimg.com/images/themes/theme1/bg.png\",\"profile_background_image_url_https\":\"https://si0.twimg.com/images/themes/theme1/bg.png\",\"profile_background_tile\":false,\"profile_image_url\":\"http://a0.twimg.com/sticky/default_profile_images/default_profile_4_normal.png\",\"profile_image_url_https\":\"https://si0.twimg.com/sticky/default_profile_images/default_profile_4_normal.png\",\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":true,\"default_profile_image\":true,\"following\":true,\"follow_request_sent\":null,\"notifications\":null},\"geo\":{\"type\":\"Point\",\"coordinates\":[51.446646,7.646494]},\"coordinates\":{\"type\":\"Point\",\"coordinates\":[7.646494,51.446646]},\"place\":{\"id\":\"c2a82d0b6fbcde87\",\"url\":\"http://api.twitter.com/1/geo/id/c2a82d0b6fbcde87.json\",\"place_type\":\"poi\",\"name\":\"Letteweg, Hennen\",\"full_name\":\"Letteweg, Hennen, Iserlohn\",\"country_code\":\"DE\",\"country\":\"Germany\",\"bounding_box\":{\"type\":\"Polygon\",\"coordinates\":[[[7.645819,51.446607],[7.645819,51.446607],[7.645819,51.446607],[7.645819,51.446607]]]},\"attributes\":{\"street_address\":\"Letteweg\"}},\"contributors\":null,\"retweet_count\":0,\"favorited\":false,\"retweeted\":false}," +
					//"{\"created_at\":\"Mon Sep 17 21:00:02 +0000 2012\",\"id\":247802112673718272,\"id_str\":\"247802112673718272\",\"text\":\"Es ist jetzt 23 Uhr.\",\"source\":\"<a href=\\\"http://leumund.ch/dienstleistungsboter-in-twitter-001129\\\" rel=\\\"nofollow\\\">zurvollenstunde</a>\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":14383393,\"id_str\":\"14383393\",\"name\":\"zurvollenstunde\",\"screen_name\":\"zurvollenstunde\",\"location\":\"GMT+1\",\"url\":\"http://leumund.ch/2008/technologiebloggen/dienstleistungsboter-in-twitter/\",\"description\":\"Immer auf die volle Stunde eine Nachricht mit der Zeit. \",\"protected\":false,\"followers_count\":7347,\"friends_count\":0,\"listed_count\":333,\"created_at\":\"Mon Apr 14 10:11:30 +0000 2008\",\"favourites_count\":2,\"utc_offset\":3600,\"time_zone\":\"Bern\",\"geo_enabled\":false,\"verified\":false,\"statuses_count\":38481,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_image_url\":\"http://a0.twimg.com/images/themes/theme1/bg.png\",\"profile_background_image_url_https\":\"https://si0.twimg.com/images/themes/theme1/bg.png\",\"profile_background_tile\":false,\"profile_image_url\":\"http://a0.twimg.com/profile_images/1200663831/10_normal.jpg\",\"profile_image_url_https\":\"https://si0.twimg.com/profile_images/1200663831/10_normal.jpg\",\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":true,\"default_profile_image\":false,\"following\":true,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":1,\"favorited\":false,\"retweeted\":false}," +
					//"{\"created_at\":\"Mon Sep 17 20:58:43 +0000 2012\",\"id\":247801783303409664,\"id_str\":\"247801783303409664\",\"text\":\"Hat jemand sowas wie ein female XLR/Male 3,5mm Klinke Kabel?\",\"source\":\"<a href=\\\"http://mobile.twitter.com\\\" rel=\\\"nofollow\\\">Mobile Web</a>\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":44625441,\"id_str\":\"44625441\",\"name\":\"Juri\",\"screen_name\":\"tuxwurf\",\"location\":\"Münster\",\"url\":\"http://tuxwurf.de\",\"description\":\"<<du alter Poet mit einem Hang zum magischen Realismus...>>\",\"protected\":true,\"followers_count\":715,\"friends_count\":311,\"listed_count\":104,\"created_at\":\"Thu Jun 04 14:20:20 +0000 2009\",\"favourites_count\":73,\"utc_offset\":3600,\"time_zone\":\"Berlin\",\"geo_enabled\":true,\"verified\":false,\"statuses_count\":35932,\"lang\":\"de\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"131516\",\"profile_background_image_url\":\"http://a0.twimg.com/images/themes/theme14/bg.gif\",\"profile_background_image_url_https\":\"https://si0.twimg.com/images/themes/theme14/bg.gif\",\"profile_background_tile\":true,\"profile_image_url\":\"http://a0.twimg.com/profile_images/2461094497/qihbrcfdbx7ank016xju_normal.jpeg\",\"profile_image_url_https\":\"https://si0.twimg.com/profile_images/2461094497/qihbrcfdbx7ank016xju_normal.jpeg\",\"profile_link_color\":\"009999\",\"profile_sidebar_border_color\":\"EEEEEE\",\"profile_sidebar_fill_color\":\"EFEFEF\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":true,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorited\":false,\"retweeted\":false}," +
					"{\"created_at\":\"Mon Sep 17 20:58:40 +0000 2012\",\"id\":247801771207061504,\"id_str\":\"247801771207061504\",\"text\":\"#SFGiants Matt Cain hosted a @patxispizza party for the Petaluma little league team. Check out the video.  https://t.co/4lPqZ7PZ\",\"source\":\"<a href=\\\"http://twitter.com/download/iphone\\\" rel=\\\"nofollow\\\">Twitter for iPhone</a>\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":60101131,\"id_str\":\"60101131\",\"name\":\"Tory Belleci\",\"screen_name\":\"ToryBelleci\",\"location\":\"San Francisco\",\"url\":\"http://www.facebook.com/pages/Tory-Belleci/151737058228942\",\"description\":\"Mythbuster, ninja assassin, and a guy who just likes to blow stuff up.\",\"protected\":false,\"followers_count\":165606,\"friends_count\":122,\"listed_count\":3373,\"created_at\":\"Sat Jul 25 17:13:26 +0000 2009\",\"favourites_count\":0,\"utc_offset\":-28800,\"time_zone\":\"Pacific Time (US & Canada)\",\"geo_enabled\":false,\"verified\":true,\"statuses_count\":607,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_image_url\":\"http://a0.twimg.com/profile_background_images/240060197/Twitter_background.jpg\",\"profile_background_image_url_https\":\"https://si0.twimg.com/profile_background_images/240060197/Twitter_background.jpg\",\"profile_background_tile\":true,\"profile_image_url\":\"http://a0.twimg.com/profile_images/1372346118/Hi-ResMYTHBUSTERStory_normal.jpg\",\"profile_image_url_https\":\"https://si0.twimg.com/profile_images/1372346118/Hi-ResMYTHBUSTERStory_normal.jpg\",\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":true,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":4,\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false}";
			/*result = result + "," + result;
			result = result + "," + result;
			result = result + "," + result;
			result = result + "," + result;*/
			result = "[" + result + "]";
			
		    Log.d(LOG, "" + result.length() + " Bytes");
		    Log.d(LOG, "Starting parsing JSON...");
		    
			final List<Tweet> tweets = JSON.parseObject(result, new TypeReference<List<Tweet>>() {});
		    
		    Log.d(LOG, "Finished parsing JSON.");
		    Log.d(LOG, "" + tweets.size() + " Entries");
		    
		    handler.post(new Runnable() {
				@Override
				public void run() {
					//elements.addAll(tweets);
				}
			});
		}
	}

	public void addTweet(final Tweet tweet) {
		Log.d(LOG, "Adding Tweet.");
		//elements.add(tweet);
		// TODO Auto-generated method stub
		handler.post(new Runnable() {
			public void run() {
				elements.addAsFirst(tweet);
			}
		});
	}

	public void sendTweet(String text, Location location) throws TweetSendException {
		OAuthRequest request = new OAuthRequest(Verb.POST, "https://api.twitter.com/1/statuses/update.json");
		request.addBodyParameter("status", text);
		if (location!=null) {
			request.addBodyParameter("lat", String.valueOf(location.getLatitude()));
			request.addBodyParameter("long", String.valueOf(location.getLongitude()));
		}
		signRequest(request);
		Response response = request.send();
		if (!response.isSuccessful()) throw new TweetSendException();
	}

	public void addTweetFromJSON(String json) {
	    Tweet t = JSON.parseObject(json, Tweet.class);
	    Log.d(LOG, "" + t.id);
	    if (t.id % 50 == 0 && t.id>0) addTweet(t);
	}
}
