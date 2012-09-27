package de.fabianonline.geotweeter.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import de.fabianonline.geotweeter.Constants;
import de.fabianonline.geotweeter.R;

public class GeneralPrefsActivity extends PreferenceActivity {

    @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Constants.PREFS_APP);
        addPreferencesFromResource(R.xml.settings);
    }
   

}
