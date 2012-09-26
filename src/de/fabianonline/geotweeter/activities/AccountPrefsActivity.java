package de.fabianonline.geotweeter.activities;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import de.fabianonline.geotweeter.Account;
import de.fabianonline.geotweeter.Constants;
import de.fabianonline.geotweeter.R;
import de.fabianonline.geotweeter.User;

public class AccountPrefsActivity extends PreferenceActivity {

	private Preference prefName;
	private Preference prefUrl;
	private Preference prefLoc;
	private Preference prefDesc;
	
    private User user;
    private Account account;
    private OnPreferenceChangeListener preferenceChangeListener = new OnPreferenceChangeListener() {
		
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if (preference.getKey().equals("pref_name")) {
				user.name = (String)newValue;
				preference.setSummary((String)newValue);
			}
			if (preference.getKey().equals("pref_url")) {
				user.url = (String)newValue;
				preference.setSummary((String)newValue);
			}
			if (preference.getKey().equals("pref_loc")) {
				user.location = (String)newValue;
				preference.setSummary((String)newValue);
			}
			if (preference.getKey().equals("pref_desc")) {
				user.description = (String)newValue;
				preference.setSummary((String)newValue);
			}
			
			return false;
		};
    };

	@SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        account = (Account) getIntent().getSerializableExtra("account");
        user = new User(account.getUser());
        
        addPreferencesFromResource(R.xml.account_settings);
        
        prefName = findPreference("pref_name");
        prefUrl = findPreference("pref_url");
        prefLoc = findPreference("pref_loc");
        prefDesc = findPreference("pref_desc");
        
        prefName.setDefaultValue(user.name);
        prefUrl.setDefaultValue(user.url);
        prefLoc.setDefaultValue(user.location);
        prefDesc.setDefaultValue(user.description);

        prefName.setSummary(user.name);
        prefUrl.setSummary(user.url);
        prefLoc.setSummary(user.location);
        prefDesc.setSummary(user.description);
        
        prefName.setOnPreferenceChangeListener(preferenceChangeListener);
        prefUrl.setOnPreferenceChangeListener(preferenceChangeListener);
        prefLoc.setOnPreferenceChangeListener(preferenceChangeListener);
        prefDesc.setOnPreferenceChangeListener(preferenceChangeListener);
            
        
    }

	@Override
	public void onBackPressed() {
		UpdateProfileTask submitTask = new UpdateProfileTask();
		submitTask.execute(user);
		
		super.onBackPressed();
	}
    
	private class UpdateProfileTask extends AsyncTask<User, Void, Boolean> {

		@Override
		protected Boolean doInBackground(User... arg0) {
			User user = arg0[0];
			OAuthService service = new ServiceBuilder()
				.provider(TwitterApi.class)
				.apiKey(Constants.API_KEY)
				.apiSecret(Constants.API_SECRET)
				.debug()
				.build();

			OAuthRequest request = new OAuthRequest(Verb.POST, Constants.URI_UPDATE_PROFILE);
			if (user.name != null) {
				request.addBodyParameter("name", user.name);
			}
			if (user.url != null) {
				request.addBodyParameter("url", user.url);
			}
			if (user.location != null) {
				request.addBodyParameter("location", user.location);
			}
			if (user.description != null) {
				request.addBodyParameter("description", user.description);
			}
			request.addBodyParameter("skip_status", "true");
			service.signRequest(account.getToken(), request);
			
			Response response = request.send();
			
			if (response.isSuccessful()) {
				account.setUser(user);
			} else {
				Log.e("AccountPrefsActivity", "update_profile gone wrong");
			}

			
			return null;
		}
		
	}
	
}
