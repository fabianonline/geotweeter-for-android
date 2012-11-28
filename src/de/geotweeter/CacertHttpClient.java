package de.geotweeter;

import java.io.InputStream;
import java.security.KeyStore;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import de.geotweeter.R;

import android.content.Context;

/**
 * Customized http client with support for own CACert certificates
 * 
 * @author Lutz Krumme (@el_emka)
 *
 */
public class CacertHttpClient extends DefaultHttpClient {

	final Context context;
	
	/**
	 * Creates a new http client
	 * 
	 * @param context Application context
	 */
	public CacertHttpClient(Context context) {
		this.context = context;
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	protected ClientConnectionManager createClientConnectionManager() {
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(
			new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		registry.register(new Scheme("https", newSslSocketFactory(), 443));
		return new SingleClientConnManager(getParams(), registry);
	}
	
	/**
	 * Creates a SSL socket factory with support for our local cacert certificate
	 * 
	 * @return The generated SSLSocketFactory
	 */
	private SSLSocketFactory newSslSocketFactory() {
		try {
			KeyStore trusted = KeyStore.getInstance("BKS");
			InputStream in = context.getResources().openRawResource(R.raw.cacert);
			try {
				trusted.load(in, Utils.getProperty("cacert.keystore.passphrase").toCharArray());
			} finally {
				in.close();
			}
			return new SSLSocketFactory(trusted);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
	
	
}
