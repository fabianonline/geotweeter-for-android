package de.geotweeter;

import java.security.AccessControlException;
import java.util.ArrayList;

import android.os.AsyncTask;
import de.geotweeter.activities.TimelineActivity;
import de.geotweeter.apiconn.TwitterApiAccess;
import de.geotweeter.timelineelements.DirectMessage;
import de.geotweeter.timelineelements.TimelineElement;
import de.geotweeter.timelineelements.Tweet;

public class Conversation {

	private TimelineElementAdapter tea;
	private TwitterApiAccess api;
	private boolean backwards;
	private MessageHashMap dm_conversations;
	
	public Conversation(TimelineElementAdapter tea, Account current_account, boolean backwards, boolean onStack) {
		this.tea = tea;
		this.backwards = backwards;
		api = current_account.getApi();
		dm_conversations = current_account.getDMConversations();
		if (onStack) {
			current_account.pushTimeline(tea);
		}
		if (!tea.isEmpty()) {
			new LoadConversationTask().execute(tea.getItem(0));
		}
	}
	
	private class LoadConversationTask extends AsyncTask<TimelineElement, TimelineElement, Void> {

		@Override
		protected Void doInBackground(TimelineElement... params) {
			if (params == null) {
				throw new NullPointerException("Conversation Task parameter is null");
			}
			TimelineElement current_element = params[0];
			if (current_element.getClass() != Tweet.class) {
				ArrayList<DirectMessage> messages = dm_conversations.getConversation(getRespondent(current_element));
				for (DirectMessage msg : messages) {
					publishProgress(msg);
				}
				return null;
			}
			Tweet current = (Tweet) current_element;
			while (current.in_reply_to_status_id != 0) {
				long predecessor_id = current.in_reply_to_status_id;
				try {
					current = (Tweet) TimelineActivity.availableTweets.get(predecessor_id);
				} catch (NullPointerException e) {
					current = null;
				}
				if (current == null) {
					current = api.getTweet(predecessor_id);
				}				
				publishProgress(current);
			}
			return null;
		}

		private long getRespondent(TimelineElement current_element) {
			assert (current_element.getClass() == DirectMessage.class);
			DirectMessage current_msg = (DirectMessage) current_element;
			if (current_msg.sender.id == dm_conversations.getOwnerId()) {
				return current_msg.recipient.id;
			} else if (current_msg.recipient.id == dm_conversations.getOwnerId()) {
				return current_msg.sender.id;
			} else {
				/* Shouldn't actually happen */ 
				throw new AccessControlException("Message does not belong to given user's timeline");
			}
		}

		protected void onProgressUpdate(TimelineElement... params) {
			if (tea.getItem(0).getID() == params[0].getID()) {
				return;
			}
			if (backwards) {
				tea.add(params[0]);
			} else {
				tea.addAsFirst(params[0]);
			}
		}
		
	}
	
}
