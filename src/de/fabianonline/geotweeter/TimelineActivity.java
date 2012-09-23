package de.fabianonline.geotweeter;

import java.util.ArrayList;

import org.scribe.model.Token;

import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;
import de.fabianonline.geotweeter.activities.NewTweetActivity;
import de.fabianonline.geotweeter.timelineelements.TimelineElement;

public class TimelineActivity extends Activity {
	private final String LOG = "TimelineActivity";
	private TimelineElementAdapter ta;
	private ArrayList<TimelineElement> elements;
	private ArrayList<Account> accounts = new ArrayList<Account>();
	public static Account current_account = null;
	public static BackgroundImageLoader background_image_loader = null;
	public static String reg_id;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		elements = new ArrayList<TimelineElement>();
		ta = new TimelineElementAdapter(this, R.layout.timeline_element, elements);
		background_image_loader = new BackgroundImageLoader(getApplicationContext());
		ListView l = (ListView) findViewById(R.id.timeline);
		l.setAdapter(ta);
		l.setOnItemClickListener(new OnItemClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				view.setBackgroundDrawable(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {0xFFFFFFFF, 0xFFCCCCCC }));
			}
		});
		addAccount(new Account(ta, new Token("aa", "aa")));
		l.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (elements.get(position).isReplyable()) {
					Intent replyIntent = new Intent(TimelineActivity.this, NewTweetActivity.class);
					replyIntent.putExtra("de.fabianonline.geotweeter.NewTweetActivity.reply_to_tweet_id", elements.get(position).getID());
					replyIntent.putExtra("de.fabianonline.geotweeter.NewTweetActivity.reply_to_user", elements.get(position).getSenderScreenName());
					startActivity(replyIntent);
					return true;
				} else {
					return false;
				}
			}
		});
		
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		reg_id = GCMRegistrar.getRegistrationId(this);
		if (reg_id.equals("")) {
			GCMRegistrar.register(this, Constants.GCM_SENDER_ID);
		} else {
			Log.d(LOG, "Already registered.");
		}
		Log.d(LOG, ""+reg_id);
	}

	public void onDestroy() {
		super.onDestroy();
		for (Account acct : Account.all_accounts) {
			acct.stopStream();
		}
	}

	public void addAccount(Account acc) {
		accounts.add(acc);
		if (current_account == null) {
			current_account = acc;
		}
		acc.registerForGCMMessages();
	}

	public void newTweetClickHandler(View v) {
		startActivity(new Intent(this, NewTweetActivity.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_timeline, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent settingsActivity = new Intent(this,
					GeneralPrefsActivity.class);
			startActivity(settingsActivity);
			return true;
		}
		return true;
	}
}
