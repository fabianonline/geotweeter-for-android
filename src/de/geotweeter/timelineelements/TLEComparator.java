package de.geotweeter.timelineelements;

import java.util.Comparator;

public class TLEComparator implements Comparator<TimelineElement> {

	@Override
	public int compare(TimelineElement lhs, TimelineElement rhs) {
		return ((lhs.created_at.getTime() > rhs.created_at.getTime()) ? -1 : 1);
	}

}
