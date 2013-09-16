package de.geotweeter.apiconn.twitter;

import java.io.Serializable;

import de.geotweeter.Constants.TLEType;
import de.geotweeter.timelineelements.TimelineElement;

public class StreamDeleteRequest extends TimelineElement implements
		Serializable {

	private static final long serialVersionUID = 1688974022097284892L;
	public Delete delete;

	@Override
	public String getTextForDisplay() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSourceText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAvatarSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getID() {
		return delete.status.id;
	}

	@Override
	public String getSenderScreenName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSenderName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOwnMessage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TLEType getType() {
		return TLEType.DELETE;
	}

	/* (non-Javadoc)
	 * @see de.geotweeter.timelineelements.TimelineElement#getTitleForDisplay()
	 */
	@Override
	public String getTitleForDisplay() {
		// TODO Auto-generated method stub
		return null;
	}

}
