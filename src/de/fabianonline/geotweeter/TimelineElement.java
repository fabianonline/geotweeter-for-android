package de.fabianonline.geotweeter;

import android.graphics.drawable.Drawable;

public abstract class TimelineElement {
	
	abstract public String getTextForDisplay();
	abstract public CharSequence getSourceText();
	abstract public String getAvatarSource();
}
