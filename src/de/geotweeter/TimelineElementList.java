/**
 * 
 */
package de.geotweeter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

import de.geotweeter.activities.TimelineActivity;
import de.geotweeter.apiconn.twitter.Hashtag;
import de.geotweeter.apiconn.twitter.Tweet;
import de.geotweeter.timelineelements.ProtectedAccount;
import de.geotweeter.timelineelements.SilentAccount;
import de.geotweeter.timelineelements.TLEComparator;
import de.geotweeter.timelineelements.TimelineElement;
import de.geotweeter.timelineelements.UserMention;

/**
 * @author Julian Kuerby
 *
 */
public class TimelineElementList extends Observable {

	private List<TimelineElement> items;
	private HashMap<Long, TimelineElement> available = new HashMap<Long, TimelineElement>();

	private enum TLEHandlingType {
		SPECIAL, KNOWN, NORMAL
	}

	public TimelineElementList() {
		items = new ArrayList<TimelineElement>();
	}

	/**
	 * Adds a timeline element to the adapter
	 * 
	 * @param t
	 *            The element to be added
	 */
	public void addAsFirst(TimelineElement t) {
		if (!available.containsKey(t.getID())) {
			if (!items.isEmpty()) {
				if (t.olderThan(items.get(0))) {
					items.add(0, t);
					Collections.sort(items, new TLEComparator());
				} else {
					items.add(0, t);
				}
			} else {
				items.add(0, t);
			}
			processNewTLE(t);
			setChanged();
			notifyObservers();
		}
	}

	/**
	 * Adds timeline elements to the adapter
	 * 
	 * @param elements
	 *            The list of elements to be added
	 */
	public void addAllAsFirst(List<TimelineElement> elements, boolean sort) {
		for (TimelineElement t : elements) {

			TLEHandlingType type = TLEHandlingType.NORMAL;
			if (t instanceof SilentAccount || t instanceof ProtectedAccount) {
				type = TLEHandlingType.SPECIAL;
			} else if (available.containsKey(t.getID())) {
				type = TLEHandlingType.KNOWN;
			}

			switch (type) {
			case NORMAL:
				processNewTLE(t);
			case SPECIAL:
				items.add(t);
				if (sort) {
					Collections.sort(items, new TLEComparator());
				}
				setChanged();
				notifyObservers();
			}
		}
	}

	/**
	 * @param elm
	 */
	public void add(TimelineElement elm) {
		items.add(elm);
		setChanged();
		notifyObservers();
	}

	/**
	 * Processes a TimelineElement
	 * 
	 * @param tle
	 *            The TimelineElement to be processed
	 */
	private void processNewTLE(TimelineElement tle) {
		available.put(tle.getID(), tle);
		// TODO Move the following to Geotweeter.java (Rimgar)
		if (tle instanceof Tweet) {
			try {
				for (Hashtag ht : ((Tweet) tle).entities.hashtags) {
					Geotweeter.getInstance().getAutoCompletionContent()
							.add("#" + ht.text);
				}
			} catch (NullPointerException e) {
				// just continue
			}
			Geotweeter.getInstance().getAutoCompletionContent()
					.add("@" + tle.getSenderScreenName());
			try {
				for (UserMention mention : ((Tweet) tle).entities.user_mentions) {
					Geotweeter.getInstance().getAutoCompletionContent()
							.add("@" + mention.screen_name);
				}
			} catch (NullPointerException e) {
				// Just continue
			}
		}
	}
	
	public void remove(TimelineElement object) {
		if (items.remove(object)) {
			setChanged();
			notifyObservers();
		}
	}
	
	/**
	 * Replaces an element in the timeline element list
	 * 
	 * @param oldTle
	 * @param newTle
	 */
	public void replace(TimelineElement oldTle, TimelineElement newTle) {
		if (Collections.replaceAll(items, oldTle, newTle)) {
			TimelineActivity.addToAvailableTLE(newTle);
			setChanged();
			notifyObservers();
		}
	}

	public List<TimelineElement> getList() {
		return items;
	}

	/**
	 * @return
	 */
	public boolean isEmpty() {
		return items.isEmpty();
	}

	/**
	 * 
	 */
	public void notifyObserversFromOutside() {
		setChanged();
		super.notifyObservers();
	}
}
