package de.fabianonline.geotweeter.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import de.fabianonline.geotweeter.AccountListElementAdapter;
import de.fabianonline.geotweeter.AuthenticateAccount;
import de.fabianonline.geotweeter.Constants;
import de.fabianonline.geotweeter.R;
import de.fabianonline.geotweeter.User;
import de.fabianonline.geotweeter.UserElement;

public class SettingsAccounts extends Activity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pref_accounts);
		
		SharedPreferences sp = getSharedPreferences(Constants.PREFS_APP, 0);
		
		String accountSet = sp.getString("accounts", null);
		
		
		if (accountSet != null) {
			String[] accounts = accountSet.split(" ");
			ArrayList<User> accountArray = User.getPersistentData(getApplicationContext(), accounts);
			ArrayList<UserElement> userElements = new ArrayList<UserElement>();
			for (User u : accountArray) {
				userElements.add(u);
			}
			ListView lv = (ListView)findViewById(R.id.lvAccounts);
			AccountListElementAdapter adapter = new AccountListElementAdapter(this, R.layout.account_list_element, userElements);
			lv.setAdapter(adapter);
		}
		
		Button btnAddAccount = (Button)findViewById(R.id.btnAddAccount);
		btnAddAccount.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				addAccount();
			}
		});
		
	}

	protected void addAccount() {

		Intent intent = new Intent().setClass(this, AuthenticateAccount.class);
		startActivity(intent);

	}
}
