package de.fabianonline.geotweeter;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AuthenticateAccount extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.authenticate_account);
		
		final OAuthService os = new ServiceBuilder()
				.provider(TwitterApi.class)
				.apiKey(Constants.API_KEY)
				.apiSecret(Constants.API_SECRET)
				.callback(Constants.OAUTH_CALLBACK)
				.build();
		
		final Token requestToken = os.getRequestToken();
		final String authURL = os.getAuthorizationUrl(requestToken);
		
		final WebView wv = (WebView)findViewById(R.id.wvAuthenticate);
		
		wv.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url){
				
				if (url.startsWith("oauth")) {
					wv.setVisibility(View.GONE);
					Uri uri = Uri.parse(url);
					String verifier = uri.getQueryParameter("oauth_verifier");
					Verifier v = new Verifier(verifier);
					
					Token accessToken = os.getAccessToken(requestToken, v);
					
					if (uri.getHost().equals("twitter")) {
						OAuthRequest req = new OAuthRequest(Verb.GET, Constants.URI_VERIFY_CREDENTIALS);
						req.addBodyParameter("skip_status", "true");
						os.signRequest(accessToken, req);
						Response response = req.send();
						if (response.getCode() == 200) {
							
							response.getBody();
						}
						
					}
					
					return true;
				}
				
				return super.shouldOverrideUrlLoading(view, url);
			}
		});
		
		wv.loadUrl(authURL);
		
	}
	
}
