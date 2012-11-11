
package de.geotweeter.activities;

import java.util.Observable;
import java.util.Observer;

import de.geotweeter.Account;
import de.geotweeter.Constants;
import de.geotweeter.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup.LayoutParams;

/**
 * @author Julian Kuerby
 *
 */
public class AccountSwitcherRadioButton extends RadioButton implements Observer {
	
	public enum Message {
		UNREAD, REFRESH_START, REFRESH_FINISHED
	}
	
	private static final int checkedColorDark = Color.WHITE;
	private static final int checkedColorLight = Color.BLACK;
	private static final int uncheckedColor = Color.GRAY;
	
	private final Account account;
	private final Context context;
	private boolean useDarkTheme;
	
	public AccountSwitcherRadioButton(Context context, Account account) {
		super(context);
		this.account = account;
		this.context = context;
		useDarkTheme = context.getSharedPreferences(Constants.PREFS_APP, 0).getBoolean("pref_dark_theme", false);
		
		LayoutParams layout = new LayoutParams(LayoutParams.WRAP_CONTENT, 48);
		layout.setMargins(pixelToDIP(5), pixelToDIP(2), pixelToDIP(1), 0);
		setLayoutParams(layout);
		setPadding(pixelToDIP(35), pixelToDIP(4), pixelToDIP(4), pixelToDIP(4));
		if (useDarkTheme) {
			setBackgroundResource(R.drawable.account_switcher_selector);
		} else {
			setBackgroundResource(R.drawable.account_switcher_selector_light);
		}
		setText("(" + account.getUnreadTweetsSize() + ")");
		setTextColor(uncheckedColor);
		
		setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					if (useDarkTheme) {
						setTextColor(checkedColorDark);
					} else {
						setTextColor(checkedColorLight);
					}
				} else {
					setTextColor(uncheckedColor);
				}
			}
		});
		
		account.addObserver(this);
	}
	
	public void setButtonBitmap(Bitmap bitmap) {
		super.setButtonDrawable(new AlphaBitmapDrawable(getResources(), bitmap));
	}
	
	@Override
	public void update(Observable observable, Object data) {
		if (data instanceof Message) {
			if (data == Message.UNREAD) {
				setText("(" + account.getUnreadTweetsSize() + ")");
			} else if (data == Message.REFRESH_START) {
				
			} else if (data == Message.REFRESH_FINISHED) {
				
			}
		}
	}
	
	private int pixelToDIP(int pixel) {
		return (int) (pixel * context.getResources().getDisplayMetrics().density + 0.5f);
	}
	
	private class AlphaBitmapDrawable extends BitmapDrawable {
		private boolean checked;
		
		public AlphaBitmapDrawable(Resources res, Bitmap bitmap) {
			super(res, bitmap);
			setAlpha(Constants.UNCHECKED_ALPHA_VALUE);
			checked = false;
		}

		@Override
		protected boolean onStateChange(int[] state) {
			if(state != null) {
				for (int s : state) {
					if (s == android.R.attr.state_checked) {
						if (!checked) {
							setAlpha(Constants.CHECKED_ALPHA_VALUE);
							checked = true;
							return true;
						}
						return false;
					}
				}
			}
			if (checked) {
				setAlpha(Constants.UNCHECKED_ALPHA_VALUE);
				checked = false;
				return true;
			}
			return false;
		}	
	}

}
