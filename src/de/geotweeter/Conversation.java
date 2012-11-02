package de.geotweeter;

import android.os.AsyncTask;
import de.geotweeter.apiconn.TwitterApiAccess;
import de.geotweeter.timelineelements.TimelineElement;
import de.geotweeter.timelineelements.Tweet;

public class Conversation {

	private TimelineElementAdapter tea;
	private TwitterApiAccess api;
	
	public Conversation(TimelineElementAdapter tea, Account current_account) {
		this.tea = tea;
		api = current_account.getApi();
		current_account.pushTimeline(tea);
		new LoadConversationTask().execute(tea.getItem(0));
	}
	
	private class LoadConversationTask extends AsyncTask<TimelineElement, TimelineElement, Void> {

		@Override
		protected Void doInBackground(TimelineElement... params) {
			TimelineElement current_element = params[0];
			if (current_element.getClass() != Tweet.class) {
				throw new ClassCastException("Conversation should be based on a tweet");
			}
			Tweet current = (Tweet) current_element;
			while (current.in_reply_to_status_id != 0) {
				current = api.getTweet(current.in_reply_to_status_id);
				publishProgress(current);
			}
			return null;
		}
		
		protected void onProgressUpdate(TimelineElement... params) {
			tea.addAsFirst(params[0]);
		}
		
	}
	
}
