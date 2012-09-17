package de.fabianonline.geotweeter;

import java.net.URI;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;

public abstract class TimelineElement {
	abstract public String getTextForDisplay();
	abstract public Drawable getAvatarDrawable();
	abstract public CharSequence getSourceText();
}
