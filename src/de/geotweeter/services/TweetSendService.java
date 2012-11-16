package de.geotweeter.services;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import de.geotweeter.Constants;
import de.geotweeter.R;
import de.geotweeter.SendableTweet;
import de.geotweeter.Utils;
import de.geotweeter.apiconn.TwitpicApiAccess;
import de.geotweeter.exceptions.TemporaryTweetSendException;
import de.geotweeter.exceptions.TweetSendException;

public class TweetSendService extends Service {
	private NotificationManager notificationManager;
	private static final String LOG = "TweetSendService";
	private final IBinder binder = new TweetSendBinder();
	private List<SendableTweet> tweets = new LinkedList<SendableTweet>();
	private Notification notification;
	private int i = 0;
	private TweetSenderThread tweetSenderThread;
	private int bindCount = 0;

	public class TweetSendBinder extends Binder {
		
		public TweetSendService getService() {
			return TweetSendService.this;
		}
		
	}
	
	@Override
	public void onCreate() {
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Log.d(LOG, "Started.");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startID) {
		Log.d(LOG, "Received startID " + startID + ": " + intent);
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		Log.d(LOG, "Destroyed.");
	}
	
	public IBinder onBind(Intent intent) {
		bindCount++;
		return binder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		bindCount--;
		return super.onUnbind(intent);
	}
	
	public void addSendableTweet(SendableTweet tweet) {
		tweets.add(tweet);
		updateNotification();
		if (tweetSenderThread == null) {
			tweetSenderThread = new TweetSenderThread();
			new Thread(tweetSenderThread, "TweetSenderThread").start();
		}
	}
	
	private void updateNotification() {
		if (notification == null) {
			notification = new Notification();
		}
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = "Sende Tweet " + (i+1) + "/" + tweets.size();
		notification.when = System.currentTimeMillis();
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.setLatestEventInfo(getApplicationContext(), notification.tickerText, "Sending...", null);
		notification.contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
		notificationManager.notify(Constants.SENDING_TWEET_STATUS_NOTIFICATION_ID, notification);
	}
	
	private void removeNotification() {
		Log.d(LOG, "Removing notification.");
		notificationManager.cancel(Constants.SENDING_TWEET_STATUS_NOTIFICATION_ID);
	}
	
	private class TweetSenderThread implements Runnable {
		@Override
		public void run() {
			SendableTweet tweet;
			while (i < tweets.size()) {
				updateNotification();
				tweet = tweets.get(i);
				try {
					if (tweet.remainingImages > 0) {
						sendTweetWithPic(tweet);
					} else {
						tweet.account.getApi().sendTweet(tweet);
					}
				} catch (TemporaryTweetSendException e) {
					Log.d(LOG, "TemporaryTweetSendException fired. Sleeping 60 seconds.");
					try { Thread.sleep(60000); } catch(InterruptedException e1) {}
					i--;
				} catch (Exception e) {
					// TODO Inform the user
					Log.e(LOG, "PermanentTweetException (or another exception) fired. Stopping.");
					e.printStackTrace();
					break;
				}
				i++;
			}
			tweets.clear();
			i = 0;
			
			removeNotification();
			
			if (bindCount == 0) {
				stopSelf();
			}
		}
	}
	
	public void sendTweetWithPic(SendableTweet tweet) throws TweetSendException, IOException {
		
		String imageHoster = tweet.imageHoster;
		long imageSize = tweet.imageSize;
		if (imageHoster.equals("twitter")) {
			if (imageSize < 0) {
				imageSize = Constants.PIC_SIZE_TWITTER;
			}
			
			ContentBody picture = null;
			File f = null;
			for (String image : tweet.images) {
				if(image != null) {
					f = new File(image);
				}
			}
			
			if (f.length() <= imageSize) {
				picture = new FileBody(f);
			} else {
				picture = new ByteArrayBody(Utils.reduceImageSize(f, imageSize), f.getName());
			}

			tweet.account.getApi().sendTweetWithPicture(tweet, picture);

		} else if (imageHoster.equals("twitpic")) {
			
			TwitpicApiAccess twitpicApi = new TwitpicApiAccess(tweet.account.getToken());
			
			for (int i = 0; i < tweet.images.length; i++) {
				if (tweet.images[i] != null) {
					File image = new File(tweet.images[i]);
					String twitpicUrl = twitpicApi.uploadImage(image, tweet.text.replaceAll("http://twitpic\\.com/\\w{6}", ""), imageSize);
					tweet.images[i] = null;
					tweet.remainingImages--;
					tweet.text = TwitpicApiAccess.replacePlaceholder(tweet.text, twitpicUrl, i);
					Log.d(LOG, "Added twitpic-URL to Tweet, Twitpic " + i);

				}
			}
			Log.d(LOG, "Send Twitpic-Tweet");
			tweet.account.getApi().sendTweet(tweet);
			Log.d(LOG, "Finished: Send Twitpic-Tweet");
		}
	}

}
