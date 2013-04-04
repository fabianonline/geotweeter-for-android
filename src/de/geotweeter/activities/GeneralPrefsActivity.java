package de.geotweeter.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;
import de.geotweeter.Constants;
import de.geotweeter.Geotweeter;
import de.geotweeter.R;
import de.geotweeter.TimelineElementAdapter;
import de.geotweeter.Utils;
import de.geotweeter.timelineelements.TimelineElement;

public class GeneralPrefsActivity extends PreferenceActivity {
	
	private boolean themeChanged;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Utils.setDesign(this);
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(Constants.PREFS_APP);
		addPreferencesFromResource(R.xml.settings);
		
		themeChanged = false;
		
		ListPreference tweetTime = (ListPreference) findPreference("pref_tweet_time_style");
		tweetTime.setSummary(tweetTime.getEntry());
		
		tweetTime.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				ListPreference tweetTime = (ListPreference) findPreference("pref_tweet_time_style");
				preference.setSummary(tweetTime.getEntries()[tweetTime.findIndexOfValue(newValue.toString())]);
				TimelineElement.tweetTimeStyle = newValue.toString();
				// TODO Refresh ListView on TimeStyle change (Rimgar)
//				TimelineActivity.current_account.getElements().notifyDataSetChanged();
				((TimelineElementAdapter) TimelineActivity.getTimelineListView().getAdapter()).notifyDataSetChanged();
				return true;
			}
		});
		
		
		ListPreference imageHoster = (ListPreference) findPreference("pref_image_hoster");
		imageHoster.setSummary(imageHoster.getEntry());
		imageHoster.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				ListPreference imageHoster = (ListPreference) findPreference("pref_image_hoster");
				preference.setSummary(imageHoster.getEntries()[imageHoster.findIndexOfValue(newValue.toString())]);
				return true;
			}
		});
		
		Preference imageCache = findPreference("pref_clear_image_cache");
		imageCache.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Geotweeter.getInstance().getBackgroundImageLoader().clearCache();
				Toast.makeText(getApplicationContext(), R.string.pref_image_cache_cleared, Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		
		findPreference("pref_dark_theme").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				themeChanged = ! themeChanged;
				return true;
			}
		});
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(themeChanged) {
			Log.d("Prefs", "themeChanged");
			new AlertDialog.Builder(TimelineActivity.getInstance())
				.setTitle(R.string.pref_theme_changed_title)
				.setMessage(R.string.pref_theme_changed_message)
				.setPositiveButton(R.string.pref_theme_changed_positive, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
							Activity tl = TimelineActivity.getInstance();
							Intent intent = tl.getIntent();
							tl.finish();
							tl.startActivity(intent);
						} else {
							TimelineActivity.getInstance().recreate();
						}
					}
				})
				.setNegativeButton(R.string.pref_theme_changed_negative, null)
				.show();
		}
	}
	
	
}
