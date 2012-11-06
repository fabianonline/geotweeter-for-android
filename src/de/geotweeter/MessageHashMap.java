package de.geotweeter;

import java.io.Serializable;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import de.geotweeter.timelineelements.DirectMessage;
import de.geotweeter.timelineelements.TLEComparator;
import de.geotweeter.timelineelements.TimelineElement;

public class MessageHashMap implements Serializable {

	private static final long serialVersionUID = -8082023280815328794L;
	private HashMap<Long, ArrayList<DirectMessage>> data_store;
	private long owner_id;
	
	public MessageHashMap(long owner_id) {
		data_store = new HashMap<Long, ArrayList<DirectMessage>>();
		this.owner_id = owner_id;
	}
	
	public ArrayList<DirectMessage> getConversation(long respondent_id) {
		return data_store.get(respondent_id);
	}
	
	public void addMessage(DirectMessage msg) {
		long respondent_id;
		if (msg.sender.id == owner_id) {
			respondent_id = msg.recipient.id;
		} else if (msg.recipient.id == owner_id) {
			respondent_id = msg.sender.id;
		} else {
			throw new AccessControlException("Message does not belong to given user's timeline");
		}
		ArrayList<DirectMessage> existing = data_store.get(respondent_id);
		if (existing == null) {
			existing = new ArrayList<DirectMessage>();
		}
		existing.add(msg);
		Collections.sort(existing, new TLEComparator());
		data_store.put(respondent_id, existing);
	}
	
	public void addMessages(ArrayList<TimelineElement> msgs) {
		for (TimelineElement tle : msgs) {
			addMessage((DirectMessage) tle);
		}
	}

	public long getOwnerId() {
		return owner_id;
	}
	
}
