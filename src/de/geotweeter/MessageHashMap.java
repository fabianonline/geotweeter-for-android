package de.geotweeter;

import java.io.Serializable;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.geotweeter.timelineelements.DirectMessage;
import de.geotweeter.timelineelements.TLEComparator;
import de.geotweeter.timelineelements.TimelineElement;

/**
 * A map of direct message conversations seperated by their respondents
 *  
 * @author Lutz Krumme (@el_emka)
 *
 */
public class MessageHashMap implements Serializable {

	private static final long serialVersionUID = -8082023280815328794L;
	private HashMap<Long, List<DirectMessage>> data_store;
	private long owner_id;
	
	/**
	 * Creates a new MessageHashMap
	 * 
	 * @param owner_id The twitter id of the user's account
	 */
	public MessageHashMap(long owner_id) {
		data_store = new HashMap<Long, List<DirectMessage>>();
		this.owner_id = owner_id;
	}
	
	/**
	 * Returns the available conversation of the user with a given respondent
	 * 
	 * @param respondent_id Twitter id of the respondent
	 * @return The user's conversation with the respondent
	 */
	public List<DirectMessage> getConversation(long respondent_id) {
		return data_store.get(respondent_id);
	}
	
	/**
	 * Adds a single direct message to the correct conversation
	 * 
	 * @param msg Direct message to be added
	 */
	public void addMessage(DirectMessage msg) {
		long respondent_id;
		if (msg.sender.id == owner_id) {
			respondent_id = msg.recipient.id;
		} else if (msg.recipient.id == owner_id) {
			respondent_id = msg.sender.id;
		} else {
			throw new AccessControlException("Message does not belong to given user's timeline");
		}
		List<DirectMessage> existing = data_store.get(respondent_id);
		if (existing == null) {
			existing = new ArrayList<DirectMessage>();
		}
		existing.add(msg);
		Collections.sort(existing, new TLEComparator());
		data_store.put(respondent_id, existing);
	}
	
	/**
	 * Adds a list of direct messages to their respective conversations
	 * 
	 * @param msgs List of direct messages to be added
	 */
	public void addMessages(List<TimelineElement> msgs) {
		if (msgs != null) {
			for (TimelineElement tle : msgs) {
				addMessage((DirectMessage) tle);
			}
		}
	}

	/**
	 * Returns the Twitter id of the user's account
	 * 
	 * @return Twitter id of the owner
	 */
	public long getOwnerId() {
		return owner_id;
	}
	
}
