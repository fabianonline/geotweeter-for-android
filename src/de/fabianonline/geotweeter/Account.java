package de.fabianonline.geotweeter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import android.os.Handler;
import android.util.Log;

public class Account {
	protected final String LOG = "Account";
	private Token token = new Token("15006408-kZLGHgmKwptQDHp0Bl9daS0iCCViJnLXHUSlJ4lhM", "AaulxYnnw2WHdxxWaSlmcupM9Uoo2jpuJDEqLFkpiJY");
	private OAuthService service = new ServiceBuilder().provider(TwitterApi.class).apiKey("7tbUmgasX8QXazkxGMNw").apiSecret("F22QSxchkczthiUQomREXEu4zDA15mxiENNttkkA").debug().build();
	private TimelineElementAdapter elements;
	private Handler handler;
	
	public Account(TimelineElementAdapter elements) {
		handler = new Handler();
		this.elements = elements;
//		TimelineRefreshThread t = new TimelineRefreshThread(this);
//		new Thread(t).start();
	}
    
	private class TimelineRefreshThread implements Runnable {
		private Account account;
		
		public TimelineRefreshThread(Account parent) {
			this.account = parent;
		}
		
		@Override
		public void run() {
			OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.twitter.com/1/statuses/home_timeline.json");
		    service.signRequest(token, request);
		    Response response = request.send();
		    Log.d(LOG,response.getBody());
		    ObjectMapper mapper = new ObjectMapper();
		    Collection<Tweet> tweets = null;
		    try {
				tweets = mapper.readValue(response.getBody(), new TypeReference<Collection<Tweet>>() {});
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    for(Tweet t : tweets) {
		    	t.user.postProcess();
		    }
		    account.addTweets(tweets);
		}
	}

	public void addTweets(final Collection<Tweet> tweets) {
		// TODO Auto-generated method stub
		Log.d(LOG, String.valueOf(tweets.size()));
		handler.post(new Runnable() {
			public void run() {
				elements.addAll(tweets);
			}
		});
	}
}
