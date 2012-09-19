package de.fabianonline.geotweeter;

import android.graphics.drawable.Drawable;

public abstract class TimelineElement {
	
	abstract public String getTextForDisplay();
	abstract public Drawable getAvatarDrawable();
	abstract public CharSequence getSourceText();
	
}
