package de.geotweeter.activities;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;
import de.geotweeter.Constants;
import de.geotweeter.R;
import de.geotweeter.Utils;
import de.geotweeter.timelineelements.TimelineElement;

public class GeneralPrefsActivity extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Utils.setDesign(this);
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(Constants.PREFS_APP);
		addPreferencesFromResource(R.xml.settings);
		
		ListPreference tweetTime = (ListPreference) findPreference("pref_tweet_time_style");
		tweetTime.setSummary(tweetTime.getEntry());
		
		tweetTime.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				ListPreference tweetTime = (ListPreference) findPreference("pref_tweet_time_style");
				preference.setSummary(tweetTime.getEntries()[tweetTime.findIndexOfValue(newValue.toString())]);
				TimelineElement.tweetTimeStyle = newValue.toString();
				TimelineActivity.current_account.getElements().notifyDataSetChanged();
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
				TimelineActivity.background_image_loader.clearCache();
				Toast.makeText(getApplicationContext(), R.string.pref_image_cache_cleared, Toast.LENGTH_SHORT).show();
				return true;
			}
		});
	}
   

}
