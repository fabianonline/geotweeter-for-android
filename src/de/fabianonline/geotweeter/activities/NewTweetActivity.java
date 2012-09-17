package de.fabianonline.geotweeter.activities;

import de.fabianonline.geotweeter.R;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

public class NewTweetActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_tweet);
		
		((EditText)findViewById(R.id.tweet_text)).addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void afterTextChanged(Editable s) {
				TextView t = (TextView) findViewById(R.id.textCharsRemaining);
				int remaining = 140-s.length();
				t.setText(String.valueOf(remaining));
				if (remaining<0) {
					t.setTextColor(0xFFFF0000);
				} else {
					t.setTextColor(0xFF00FF00);
				}
			}
		});
	}
}
