package de.fabianonline.geotweeter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import android.location.Location;
import android.os.Handler;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;

import de.fabianonline.geotweeter.exceptions.TweetSendException;
import de.fabianonline.geotweeter.timelineelements.DirectMessage;
import de.fabianonline.geotweeter.timelineelements.TimelineElement;
import de.fabianonline.geotweeter.timelineelements.Tweet;

public class Account {
	
	protected final String LOG = "Account";
	public static ArrayList<Account> all_accounts = new ArrayList<Account>();
	private Token token;
	private OAuthService service = new ServiceBuilder()
											.provider(TwitterApi.class)
											.apiKey(Constants.API_KEY)
											.apiSecret(Constants.API_SECRET)
											.debug()
											.build();
	private Handler handler;
	private StreamRequest stream_request;
	private long max_read_tweet_id = 0;
	private long max_read_dm_id = 0;
	private long max_known_tweet_id = 0;
	private long min_known_tweet_id = -1;
	private long max_known_dm_id = 0;
	private long min_known_dm_id = -1;
	private User user;
	private TimelineElementAdapter elements;
	
	public Account(TimelineElementAdapter elements, Token token, User user) {
		this.token = token;
		this.user = user;
		handler = new Handler();
		this.elements = elements;
		new Thread(new TimelineRefreshThread(false)).start();
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
		private static final String LOG = "TimelineRefreshThread";
		protected boolean do_update_bottom = false;
		protected ArrayList<ArrayList<TimelineElement>> responses = new ArrayList<ArrayList<TimelineElement>>(4);
		protected ArrayList<TimelineElement> main_data = new ArrayList<TimelineElement>();
		protected int count_running_threads = 0;
		protected int count_errored_threads = 0;
		protected final Object parse_lock = new Object();
		
		public TimelineRefreshThread(boolean do_update_bottom) {
			this.do_update_bottom = do_update_bottom;
		}
		
		@Override
		public void run() {
			Log.d(LOG, "Starting run()...");
			OAuthRequest req_timeline     = new OAuthRequest(Verb.GET, "https://api.twitter.com/1/statuses/home_timeline.json");
			OAuthRequest req_mentions     = new OAuthRequest(Verb.GET, "https://api.twitter.com/1/statuses/mentions.json");
			OAuthRequest req_dms_received = new OAuthRequest(Verb.GET, "https://api.twitter.com/1/direct_messages.json");
			OAuthRequest req_dms_sent     = new OAuthRequest(Verb.GET, "https://api.twitter.com/1/direct_messages/sent.json");
			
			req_timeline.addQuerystringParameter("count", "100");
			req_mentions.addQuerystringParameter("count", "100");
			req_dms_received.addQuerystringParameter("count", "50");
			req_dms_sent.addQuerystringParameter("count", "50");
			
			if (max_known_tweet_id>0 && !do_update_bottom) {
				req_timeline.addQuerystringParameter("since_id", ""+max_known_tweet_id);
				req_mentions.addQuerystringParameter("since_id", ""+max_known_tweet_id);
			}
			if (do_update_bottom) {
				req_timeline.addQuerystringParameter("max_id", ""+(min_known_tweet_id-1));
				req_mentions.addQuerystringParameter("max_id", ""+(min_known_tweet_id-1));
			}
			
			if (max_known_dm_id>0 && !do_update_bottom) {
				req_dms_received.addQuerystringParameter("since_id", ""+max_known_dm_id);
				req_dms_sent.addQuerystringParameter("since_id", ""+max_known_dm_id);
			}
			if (min_known_dm_id>=0 && do_update_bottom) {
				req_dms_received.addQuerystringParameter("max_id", "" + (min_known_dm_id - 1));
				req_dms_sent.addQuerystringParameter("max_id", "" + (min_known_dm_id - 1));
			}
			
			/* Start all the requests */
			count_running_threads = 4;
			new Thread(new RunnableRequestTweetsExecutor(req_timeline, true), "FetchTimelineThread").start();
			new Thread(new RunnableRequestTweetsExecutor(req_mentions, false), "FetchMentionsThread").start();
			new Thread(new RunnableRequestDMsExecutor(req_dms_sent, false), "FetchSentDMThread").start();
			new Thread(new RunnableRequestDMsExecutor(req_dms_received, false), "FetchReceivedDMThread").start();
		}
		
		private void runAfterEachSuccesfullRequest(ArrayList<TimelineElement> elements, boolean is_main_data) {
			Log.d(LOG, "Started...");
			if (is_main_data) {
				main_data = elements;
			} else {
				responses.add(elements);
			}
			count_running_threads--;
			Log.d(LOG, "Remaining running threads: " + count_running_threads);
			if (count_running_threads==0) {
				runAfterAllRequestsCompleted();
			}
		}
		
		private void runAfterEachFailedRequest() {
			count_running_threads--;
			count_errored_threads++;
			// TODO Show error message
			if (count_running_threads==0) {
				runAfterAllRequestsCompleted();
			}
		}
		
		private void runAfterAllRequestsCompleted() {
			Log.d(LOG, "All Requests completed.");
			if (!main_data.isEmpty()) {
				responses.add(0, main_data);
			}
			if (count_errored_threads==0) {
				parseData(responses, do_update_bottom);
				//stream_request.start();
			} else {
				// TODO Try again after some time
				// TODO Show info message
			}
		}
		
		private class RunnableRequestTweetsExecutor implements Runnable {
			private final static String LOG = "RunnableRequestExecutor";
			private boolean is_main_data;
			private OAuthRequest request;
			
			public RunnableRequestTweetsExecutor(OAuthRequest request, boolean is_main_data) {
				this.is_main_data = is_main_data;
				this.request = request;
			}
			
			@Override
			public void run() {
				Log.d(LOG, "Started.");
				signRequest(request);
				Response response;
				try {
					long start_time = System.currentTimeMillis();
					response = request.send();
					Log.d(LOG, "Download finished: " + (System.currentTimeMillis()-start_time) + "ms");
				} catch (OAuthException e) {
					runAfterEachFailedRequest();
					return;
				}
				if (response.isSuccessful()) {
					Log.d(LOG, "Started parsing JSON...");
					long start_time = System.currentTimeMillis();
					ArrayList<TimelineElement> elements = null;
					synchronized(parse_lock) {
						elements = parse(response.getBody());
					}
					Log.d(LOG, "Finished parsing JSON. " + elements.size() + " elements at " + (System.currentTimeMillis()-start_time)/elements.size() + "ms/element");
					runAfterEachSuccesfullRequest(elements, is_main_data);
				} else {
					runAfterEachFailedRequest();
				}
			}
			
			@SuppressWarnings("unchecked")
			protected ArrayList<TimelineElement> parse(String json) {
				return (ArrayList<TimelineElement>)(ArrayList<?>)JSON.parseObject(json, new TypeReference<ArrayList<Tweet>>(){}, Feature.DisableCircularReferenceDetect);
			}
		}
		
		private class RunnableRequestDMsExecutor extends RunnableRequestTweetsExecutor {
			public RunnableRequestDMsExecutor(OAuthRequest request, boolean is_main_data) {
				super(request, is_main_data);
			}
			
			@SuppressWarnings("unchecked")
			@Override
			protected ArrayList<TimelineElement> parse(String json) {
				return (ArrayList<TimelineElement>)(ArrayList<?>)JSON.parseObject(json, new TypeReference<ArrayList<DirectMessage>>(){});
			}
		}
	}
	
	protected void parseData(ArrayList<ArrayList<TimelineElement>> responses, boolean do_clip) {
		Log.d(LOG, "parseData started.");
		final ArrayList<TimelineElement> all_elements = new ArrayList<TimelineElement>();
		long last_id = 0;
		while(responses.size()>0) {
			Date newest_date = null;
			int newest_index = -1;
			for (int i=0; i<responses.size(); i++) {
				TimelineElement element = responses.get(i).get(0);
				if (newest_date==null || element.getDate().after(newest_date)) {
					newest_date = element.getDate();
					newest_index = i;
				}
			}
			TimelineElement element = responses.get(newest_index).remove(0);
			if (responses.get(newest_index).size()==0) {
				responses.remove(newest_index);

				if (newest_index==0) {
					if (max_known_tweet_id==0) {
						for(ArrayList<TimelineElement> array : responses) {
							TimelineElement first_element = array.get(0);
							if (first_element instanceof Tweet && ((Tweet) first_element).id>max_known_tweet_id) {
								max_known_tweet_id = ((Tweet)first_element).id;
							}
						}
					}
					if (max_known_dm_id==0) {
						for(ArrayList<TimelineElement> array : responses) {
							TimelineElement first_element = array.get(0);
							if (first_element instanceof DirectMessage && first_element.getID()>max_known_dm_id) {
								max_known_dm_id = first_element.getID();
							}
						}
					}
					Log.d(LOG, "Breaking!");
					break;
				}
			}
			
			long element_id = element.getID();
			
			if (element_id != last_id) {
				all_elements.add(element);
			}
			last_id = element_id;
			
			if (element instanceof Tweet) {
				if (element_id > max_known_tweet_id) {
					max_known_tweet_id = element_id;
				}
				if (min_known_tweet_id == -1 || element_id < min_known_tweet_id) {
					min_known_tweet_id = element_id;
				}
			} else if (element instanceof DirectMessage) {
				if (element_id > max_known_dm_id) {
					max_known_dm_id = element_id;
				}
				if (min_known_dm_id == -1 || element_id < min_known_dm_id) {
					min_known_dm_id = element_id;
				}
			}
		}
		
		Log.d(LOG, "parseData is almost done. " + all_elements.size() + " elements.");
		handler.post(new Runnable() {
			@Override
			public void run() {
				elements.addAllAsFirst(all_elements);
			}
		});
	}

	public void addTweet(final TimelineElement elm) {
		Log.d(LOG, "Adding Tweet.");
		//elements.add(tweet);
		handler.post(new Runnable() {
			public void run() {
				elements.addAsFirst(elm);
			}
		});
	}

	public void sendTweet(String text, Location location, long reply_to_id) throws TweetSendException {
		OAuthRequest request = new OAuthRequest(Verb.POST, "https://api.twitter.com/1/statuses/update.json");
		request.addBodyParameter("status", text);
		
		if (location != null) {
			request.addBodyParameter("lat", String.valueOf(location.getLatitude()));
			request.addBodyParameter("long", String.valueOf(location.getLongitude()));
		}
		
		if (reply_to_id > 0) {
			request.addBodyParameter("in_reply_to_status_id", String.valueOf(reply_to_id));
		}
		signRequest(request);
		Response response = request.send();
		
		if (!response.isSuccessful()) { 
			throw new TweetSendException();
		}
	}

	public void registerForGCMMessages() {
		Log.d(LOG, "Registering...");
		new Thread(new Runnable() {
			public void run() {
				HttpClient http_client = new DefaultHttpClient();
				HttpPost http_post = new HttpPost(Constants.GCM_SERVER_URL);
				try {
					List<NameValuePair> name_value_pair = new ArrayList<NameValuePair>(3);
					name_value_pair.add(new BasicNameValuePair("reg_id", TimelineActivity.reg_id));
					name_value_pair.add(new BasicNameValuePair("token", token.getToken()));
					name_value_pair.add(new BasicNameValuePair("secret", token.getSecret()));
					http_post.setEntity(new UrlEncodedFormEntity(name_value_pair));
					http_client.execute(http_post);
				} catch(ClientProtocolException e) {
					e.printStackTrace();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}, "register").start();
	}
}
