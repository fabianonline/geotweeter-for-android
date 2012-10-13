package de.geotweeter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import de.geotweeter.exceptions.UnknownJSONObjectException;

public class StreamRequest {
	private StreamRequestThread thread = new StreamRequestThread();
	private boolean keepRunning = true;
	private Handler handler = new Handler();
	private static final String LOG = "StreamRequest";
	private boolean doRestart = true;
	
	protected Account account;
	
	public StreamRequest(Account account) {
		this.account = account;
	}
	
	public void start() {
		if (doRestart) {
			new Thread(thread, "StreamRequestThread").start();
		} else {
			Log.d(LOG, "start() called but doRestart is false.");
		}
	}
	
	public void stop(boolean restart) {
		keepRunning = false;
		this.doRestart = restart;
		try {
			if (thread != null && thread.stream != null) {
				thread.stream.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (thread.timer != null) {
			thread.timer.cancel();
		}
	}
	
	private class StreamRequestThread implements Runnable {
		private static final String LOG = "StreamRequestThread";
		private final Pattern part_finder_pattern = Pattern.compile("([0-9]+)([\n\r]+.+)$", Pattern.DOTALL);
		public InputStream stream;
		String buffer = "";
		private long lastNewlineReceivedAt = 0;
		private long lastDataReceivedAt = 0;
		private Timer timer = null;
		private long reconnectDelay = 10000;
		
		public void run() {
			if (timer != null) {
				timer.cancel();
			}
			timer = new Timer(true);
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (Debug.LOG_STREAM_CHECKS) {
						Log.d("StreamCheckNewlineTimeoutTask", "Running. " + (System.currentTimeMillis() - lastNewlineReceivedAt));
					}
					if (lastNewlineReceivedAt > 0 && (System.currentTimeMillis() - lastNewlineReceivedAt) > 40000) {
						// We should get a newline every 30 seconds. If that didn't happen -> reconnect.
						try {
							stream.close();
						} catch (IOException e) {}
					}
				}
			}, 0, 15000);
			
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (Debug.LOG_STREAM_CHECKS) {
						Log.d("StreamCheckDataTimeoutTask", "Running. " + (System.currentTimeMillis() - lastDataReceivedAt));
					}
					if (lastDataReceivedAt > 0 && (System.currentTimeMillis() - lastDataReceivedAt) > 600000) {
						// We didn't get a single tweet for more than 10 minutes -> reconnect.
						try {
							stream.close();
						} catch (IOException e) {}
					}
				}
			}, 0, 60000);
			
			startRequest();
		}
		
		public void startRequest() {
			while (true) {
				Log.d(LOG, "Starting Stream.");
				buffer = "";
				char ch[] = new char[1]; 
				String line;
				OAuthRequest request = new OAuthRequest(Verb.GET, Constants.URI_USER_STREAM);
				request.addQuerystringParameter("delimited", "length");
				account.signRequest(request);
				Response response = request.send();
				stream = response.getStream();
				InputStreamReader reader = new InputStreamReader(stream);
				Log.d(LOG, "Waiting for first data.");
				try {
					while (reader.read(ch) > 0) {
						lastNewlineReceivedAt = System.currentTimeMillis();
						buffer += ch[0];
						if (ch[0]=='\n' || ch[0]=='\r') {
							processBuffer();
						}
					}
				} catch (IOException e) {
					// TODO: Connection was killed. If necessary, restart it.
				}
				Log.d(LOG, "Stream beendet");
				lastDataReceivedAt = 0;
				lastNewlineReceivedAt = 0;
				if (!keepRunning) {
					return;
				} else {
					try {
						Thread.sleep(reconnectDelay);
					} catch (InterruptedException e) {}
					reconnectDelay *= 1.5;
				}
				account.start(false);
			}
		}
		
		/**
		 * Processes the stream's buffer (as in "looks for seperate JSON objects and parses them").
		 */
		public void processBuffer() {
			Matcher m;
			while ((m = part_finder_pattern.matcher(buffer))!=null && m.find()) {
				lastDataReceivedAt = System.currentTimeMillis();
				reconnectDelay = 10000;
				String text = m.group(2);
				int bytes = Integer.parseInt(m.group(1));
				if (text.length()>=bytes) {
					buffer = text.substring(bytes);
					try {
						account.addTweet(Utils.jsonToNativeObject(text.substring(0, bytes)));
					} catch (UnknownJSONObjectException ex) {
						// Ignore it.
					} catch (JSONException ex) {
						// Ignore it.
					}
				} else {
					return;
				}
			}
		}
		
		public void killTimers() {
			timer.cancel();
		}
	}
}
