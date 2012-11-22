package de.geotweeter.widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.widget.EditText;

public class ProtectedPlaceholderEditText extends EditText {

	private Pattern placeholder;
	private boolean placeholder_selected;
	private boolean own_call = false;
	private boolean dpad_action = false;

	public ProtectedPlaceholderEditText(Context context) {
		this(context, (Pattern) null);
	}
	
	public ProtectedPlaceholderEditText(Context context, AttributeSet attrs) {
		this(context, attrs, (Pattern) null);
	}
	
	public ProtectedPlaceholderEditText(Context context, AttributeSet attrs, int defStyle) {
		this(context, attrs, defStyle, (Pattern) null);
	}
		
	public ProtectedPlaceholderEditText(Context context, Pattern placeholder) {
		super(context);
		this.placeholder = placeholder;
	}
	
	public ProtectedPlaceholderEditText(Context context, AttributeSet attrs, Pattern placeholder) {
		super(context, attrs);
		this.placeholder = placeholder;
	}
	
	public ProtectedPlaceholderEditText(Context context, AttributeSet attrs, int defStyle, Pattern placeholder) {
		super(context, attrs, defStyle);
		this.placeholder = placeholder;
	}
	
	@Override
	protected void onSelectionChanged(int selStart, int selEnd) {
		if ((placeholder_selected && dpad_action) || placeholder == null) {
			dpad_action = false;
			placeholder_selected = false;
			sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED);
			return;
		}
		placeholder_selected = false;
		if (own_call) {
			own_call  = false;
			return;
		}
		Matcher matcher = placeholder.matcher(getText());
		if (selStart == -1) {
			return;
		}
		while (matcher.find()) {
			int pattern_start = matcher.start();
			int pattern_end = matcher.end();
			if (pattern_start > selEnd) {
				continue;
			}
			if (pattern_end < selStart) {
				continue;
			}
			own_call = true;
			setSelection(Math.min(selStart, pattern_start), Math.max(selEnd, pattern_end));
			placeholder_selected = true;
			return;
		}
	}

	public boolean isPlaceholderSelected() {
		return placeholder_selected;
	}

	public void setPlaceholderSelected(boolean placeholder_selected) {
		this.placeholder_selected = placeholder_selected;
	}

	public Pattern getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(Pattern placeholder) {
		this.placeholder = placeholder;
	}

	public boolean isDpadAction() {
		return dpad_action;
	}

	public void setDpadAction(boolean dpad_action) {
		this.dpad_action = dpad_action;
	}

	
}
