package de.fabianonline.geotweeter.activities;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import de.fabianonline.geotweeter.R;
import de.fabianonline.geotweeter.User;

public class AccountPrefsActivity extends PreferenceActivity {

    @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        User user = (User) getIntent().getSerializableExtra("user");
        
        addPreferencesFromResource(R.xml.account_settings);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Editor ed = prefs.edit();
        ed.putString("pref_name", user.name);
        ed.putString("pref_url", user.url);
        ed.putString("pref_loc", user.location);
        ed.putString("pref_desc", user.description);
        ed.commit();
    }

	
}
