package de.fabianonline.geotweeter.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import de.fabianonline.geotweeter.Constants;
import de.fabianonline.geotweeter.R;

public class SettingsAccounts extends Activity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pref_accounts);
		
		SharedPreferences sp = getSharedPreferences(Constants.PREFS_APP, 0);
		
		
		
		String accountSet = sp.getString("accounts", null);
		
		if (accountSet != null) {
			String[] accounts = accountSet.split(",");
			ListView lv = (ListView)findViewById(R.id.lvAccounts);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.pref_accounts, accounts);
			lv.setAdapter(adapter);
		}
		
	}
}
