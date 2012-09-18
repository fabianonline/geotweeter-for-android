package de.fabianonline.geotweeter.activities;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
		
		Button btnAddAccount = (Button)findViewById(R.id.btnAddAccount);
		btnAddAccount.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				addAccount();
			}
		});
		
	}

	protected void addAccount() {
		final OAuthService os = new ServiceBuilder()
		        .provider(TwitterApi.class)
		        .apiKey(Constants.API_KEY)
		        .apiSecret(Constants.API_SECRET)
		        .callback(Constants.OAUTH_CALLBACK)
		        .build();
		
		final Token requestToken = os.getRequestToken();
		

	}
}
