package de.fabianonline.geotweeter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import android.util.Log;

public class StreamRequest {
	private StreamRequestThread thread;
	
	protected Account account;
	
	public StreamRequest(Account account) {
		this.account = account;
	}
	
	public void start() {
		thread = new StreamRequestThread();
		new Thread(thread).start();
	}
	
	public void stop() {
		try {
			thread.stream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private class StreamRequestThread implements Runnable {
		private static final String LOG = "StreamRequestThread";
		private final Pattern part_finder_pattern = Pattern.compile("([0-9]+)([\n\r]+.+)$");
		public InputStream stream;
		String buffer = "";
		public void run() {
			startRequest();
		}
		
		public void startRequest() {
			buffer = "";
			String line;
			OAuthRequest request = new OAuthRequest(Verb.GET, "https://userstream.twitter.com/1.1/user.json");
			request.addQuerystringParameter("delimited", "length");
			account.signRequest(request);
			Response response = request.send();
			stream = response.getStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			try {
				while ((line = reader.readLine()) != null) {
					buffer += "\n" + line;
					processBuffer();
					try {
						/* Kurze Pause, um den anderen Threads etwas Zeit zu verschaffen.
						 * Im normalen Betrieb sollte über den Stream eh nicht so viel
						 * Traffic kommen, dass es Probleme gibt.
						 * Beim Sample-Stream dagegen ist das was anderes... */
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				// TODO: Connection was killed. If necessary, restart it.
			}
		    Log.d(LOG, "Ende");
		}
		
		/**
		 * Processes the stream's buffer (as in "looks for seperate JSON objects and parses them").
		 */
		public void processBuffer() {
			buffer = buffer.trim();
			while (buffer.length()>16) {
				Matcher m = part_finder_pattern.matcher(buffer);
				if (m.find()) {
					String text = m.group(2);
					int bytes = Integer.parseInt(m.group(1)) - 1;
					if (text.length()>=bytes) {
						buffer = text.substring(bytes);
						account.elements.addAsFirst(Utils.jsonToNativeObject(text.substring(0, bytes)));
					}
				} else {
					return;
				}
			}
		}
	}
}
