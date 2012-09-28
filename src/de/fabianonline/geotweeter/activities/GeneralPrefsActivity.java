package de.fabianonline.geotweeter.activities;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import de.fabianonline.geotweeter.Constants;
import de.fabianonline.geotweeter.R;
import de.fabianonline.geotweeter.timelineelements.TimelineElement;

public class GeneralPrefsActivity extends PreferenceActivity {

    @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
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
    }
   

}
