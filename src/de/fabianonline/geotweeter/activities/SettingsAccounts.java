package de.fabianonline.geotweeter.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import de.fabianonline.geotweeter.Account;
import de.fabianonline.geotweeter.AccountListElementAdapter;
import de.fabianonline.geotweeter.R;

public class SettingsAccounts extends Activity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pref_accounts);
		
		if (!Account.all_accounts.isEmpty()) {
//			ArrayList<UserElement> userElements = new ArrayList<UserElement>();
//			for (Account a : Account.all_accounts) {
//				userElements.add(a.getUser());
//			}
			
			ListView lv = (ListView)findViewById(R.id.lvAccounts);
			AccountListElementAdapter adapter = new AccountListElementAdapter(this, R.layout.account_list_element, Account.all_accounts);
			lv.setAdapter(adapter);
			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Account account = (Account)parent.getItemAtPosition(position);
					editUserSettings(account);
				}
			});

		}
				
		Button btnAddAccount = (Button)findViewById(R.id.btnAddAccount);
		btnAddAccount.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				addAccount();
			}
		});
		
	}

	protected void editUserSettings(Account account) {
		Intent accountPrefs = new Intent(this, AccountPrefsActivity.class);
		accountPrefs.putExtra("account", account);
		startActivity(accountPrefs);
		
	}

	protected void addAccount() {

		Intent intent = new Intent().setClass(this, AuthenticateAccountActivity.class);
		startActivity(intent);

	}
}
