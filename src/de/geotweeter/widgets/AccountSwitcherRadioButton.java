
package de.geotweeter.widgets;

import java.util.Observable;
import java.util.Observer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup.LayoutParams;
import de.geotweeter.Account;
import de.geotweeter.Constants;
import de.geotweeter.Geotweeter;
import de.geotweeter.R;
import de.geotweeter.Utils;

/**
 * @author Julian Kuerby
 *
 */
@SuppressLint("ViewConstructor")
public class AccountSwitcherRadioButton extends RadioButton implements Observer {
	
	public enum Message {
		UNREAD, REFRESH_START, REFRESH_FINISHED
	}
	
	private static final int CHECKED_COLOR_DARK = Color.WHITE;
	private static final int CHECKED_COLOR_LIGHT = Color.BLACK;
	private static final int UNCHECKED_COLOR = Color.GRAY;
	private static final int HEIGHT = 48;
	
	private final Account account;
	private final Context context;
	
	public AccountSwitcherRadioButton(Context context, Account account) {
		super(context);
		this.account = account;
		this.context = context;
		
		LayoutParams layout = new LayoutParams(LayoutParams.WRAP_CONTENT, HEIGHT);
		layout.setMargins(Utils.convertDipToPixel(5), Utils.convertDipToPixel(2), Utils.convertDipToPixel(1), 0);
		setLayoutParams(layout);
		setPadding(HEIGHT + Utils.convertDipToPixel(5), Utils.convertDipToPixel(4), Utils.convertDipToPixel(4), Utils.convertDipToPixel(4));
		if (Geotweeter.getInstance().useDarkTheme()) {
			setBackgroundResource(R.drawable.listelement_background_dark_dm);
		} else {
			setBackgroundResource(R.drawable.listelement_background_light_dm);
		}
		setText("(" + account.getUnreadTweetsSize() + ")");
		setTextColor(UNCHECKED_COLOR);
		
		setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					if (Geotweeter.getInstance().useDarkTheme()) {
						setTextColor(CHECKED_COLOR_DARK);
					} else {
						setTextColor(CHECKED_COLOR_LIGHT);
					}
				} else {
					setTextColor(UNCHECKED_COLOR);
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
				if(Geotweeter.getInstance().useDarkTheme()) {
					setBackgroundResource(R.drawable.listelement_background_dark_dm);
				} else {
					setBackgroundResource(R.drawable.listelement_background_light_dm);
				}
			} else if (data == Message.REFRESH_FINISHED) {
				if(Geotweeter.getInstance().useDarkTheme()) {
					setBackgroundResource(R.drawable.listelement_background_dark_my);
				} else {
					setBackgroundResource(R.drawable.listelement_background_light_my);
				}
			}
		}
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
