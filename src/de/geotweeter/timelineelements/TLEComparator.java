package de.geotweeter.timelineelements;

import java.util.Comparator;

/**
 * Provides a comparator for TimelineElements
 * 
 * @author Lutz Krumme (@el_emka)
 * 
 */
public class TLEComparator implements Comparator<TimelineElement> {

	@Override
	public int compare(TimelineElement lhs, TimelineElement rhs) {
		/*
		 * Due to the fact that the function needs to return an int, we cannot
		 * just subtract the rhs time from the lhs time
		 */
		return (lhs.created_at.getTime() == rhs.created_at.getTime() ? 0
				: (lhs.created_at.getTime() > rhs.created_at.getTime()) ? -1
						: 1);
	}

}
