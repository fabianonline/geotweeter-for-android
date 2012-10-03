package de.fabianonline.geotweeter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;

import android.util.Log;
import de.fabianonline.geotweeter.exceptions.UnknownJSONObjectException;

public class StreamRequest {
	private StreamRequestThread thread;
	
	protected Account account;
	
	public StreamRequest(Account account) {
		this.account = account;
	}
	
	public void start() {
		thread = new StreamRequestThread();
		new Thread(thread, "StreamRequestThread").start();
	}
	
	public void stop() {
		try {
			if (thread != null && thread.stream != null) {
				thread.stream.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private class StreamRequestThread implements Runnable {
		private static final String LOG = "StreamRequestThread";
		private final Pattern part_finder_pattern = Pattern.compile("([0-9]+)([\n\r]+.+)$", Pattern.DOTALL);
		public InputStream stream;
		String buffer = "";
		
		public void run() {
			startRequest();
		}
		
		public void startRequest() {
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
					//Log.d(LOG, "Got line: " + String.valueOf(small_buffer));
					buffer += ch[0]; // String.valueOf(small_buffer) + "\n";
					if (ch[0]=='\n' || ch[0]=='\r') {
						processBuffer();
					}
				}
			} catch (IOException e) {
				// TODO: Connection was killed. If necessary, restart it.
			}
			Log.d(LOG, "Stream beendet");
		}
		
		/**
		 * Processes the stream's buffer (as in "looks for seperate JSON objects and parses them").
		 */
		public void processBuffer() {
			Matcher m;
			while ((m = part_finder_pattern.matcher(buffer))!=null && m.find()) {
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
	}
}
