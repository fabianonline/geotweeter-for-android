package de.fabianonline.geotweeter.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import de.fabianonline.geotweeter.R;

public class GeneralPrefsActivity extends PreferenceActivity {

    @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        
        
    }
   

}
