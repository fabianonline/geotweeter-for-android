package de.geotweeter.activities;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alibaba.fastjson.JSON;

import de.geotweeter.Constants;
import de.geotweeter.Debug;
import de.geotweeter.Geotweeter;
import de.geotweeter.R;
import de.geotweeter.Utils;
import de.geotweeter.apiconn.twitter.User;

public class AuthenticateAccountActivity extends Activity {

	public static final String LOG = "AuthenticateAccountActivity";

	final OAuthService os = new ServiceBuilder().provider(TwitterApi.class)
			.apiKey(Utils.getProperty("twitter.consumer.key"))
			.apiSecret(Utils.getProperty("twitter.consumer.secret"))
			.callback(Constants.OAUTH_CALLBACK).build();

	private Activity self;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Utils.setDesign(this);
		super.onCreate(savedInstanceState);
		self = this;
		setContentView(R.layout.authenticate_account);

		AuthenticationTask task = new AuthenticationTask();
		task.execute();

	}

	protected void storeAccessData(User authUser, Token accessToken) {
		SharedPreferences sp = getSharedPreferences(Constants.PREFS_APP, 0);
		Editor ed = sp.edit();
		ed.putString("access_token." + String.valueOf(authUser.id),
				accessToken.getToken());
		ed.putString("access_secret." + String.valueOf(authUser.id),
				accessToken.getSecret());
		String accountString = sp.getString("accounts", "");
		String[] accounts = accountString.trim().split(" ");
		Boolean found = false;
		for (String id : accounts) {
			if (id.equals(String.valueOf(authUser.id))) {
				found = true;
				break;
			}
		}
		if (!found) {
			accountString += (String.valueOf(authUser.id) + " ");
			ed.putString("accounts", accountString);
		}
		ed.commit();

		authUser.storeUser(getApplicationContext());

	}

	private class AuthenticationTask extends AsyncTask<Void, Boolean, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {

			Debug.log(LOG, "Authentication Task executed");

			final Token requestToken = os.getRequestToken();
			final String authURL = os.getAuthorizationUrl(requestToken)
					+ "&force_login=true";

			final WebView wv = (WebView) findViewById(R.id.wvAuthenticate);

			WebSettings settings = wv.getSettings();
			settings.setSaveFormData(false);
			settings.setSavePassword(false);

			wv.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {

					if (url.startsWith("oauth")) {
						wv.setVisibility(View.GONE);

						VerificationTask task = new VerificationTask();
						task.execute(requestToken, url);

						return true;
					}

					return super.shouldOverrideUrlLoading(view, url);
				}
			});

			wv.loadUrl(authURL);
			return true;
		}

	}

	private class VerificationTask extends AsyncTask<Object, Void, User> {

		@Override
		protected User doInBackground(Object... params) {
			User authUser = null;
			Token requestToken = (Token) params[0];
			String url = (String) params[1];

			Uri uri = Uri.parse(url);
			String verifier = uri.getQueryParameter("oauth_verifier");
			Verifier v = new Verifier(verifier);

			Token accessToken = os.getAccessToken(requestToken, v);

			if (uri.getHost().equals("twitter")) {
				OAuthRequest req = new OAuthRequest(Verb.GET,
						Constants.URI_VERIFY_CREDENTIALS);
				req.addHeader("skip_status", "true");
				os.signRequest(accessToken, req);
				Response response = req.send();
				if (response.getCode() == 200) {
					authUser = JSON.parseObject(response.getBody(), User.class);
					storeAccessData(authUser, accessToken);
				}

			}

			return authUser;

		}

		@Override
		protected void onPostExecute(User u) {
			if (u != null) {
				/*
				 * TODO: getInstance() kann null zurückliefern, da gehört noch
				 * Arbeit hin!
				 */
				Geotweeter.getInstance().getAccountManager()
						.createAccount(u, new Handler());
			}
			self.finish();
		}

	}

}
