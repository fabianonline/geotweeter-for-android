package de.geotweeter.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.scribe.model.Token;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import de.geotweeter.Account;
import de.geotweeter.BackgroundImageLoader;
import de.geotweeter.Constants;
import de.geotweeter.MapOverlay;
import de.geotweeter.R;
import de.geotweeter.TimelineElementAdapter;
import de.geotweeter.User;
import de.geotweeter.Utils;
import de.geotweeter.timelineelements.DirectMessage;
import de.geotweeter.timelineelements.Media;
import de.geotweeter.timelineelements.TimelineElement;
import de.geotweeter.timelineelements.Tweet;
import de.geotweeter.timelineelements.Url;

public class TimelineActivity extends MapActivity {
	private final String LOG = "TimelineActivity";
	private int acc;
	public static Account current_account = null;
	public static BackgroundImageLoader background_image_loader = null;
	public static String reg_id = "";
	private MapView map;
	private static TimelineActivity instance = null;
	private static boolean isRunning = false;
	private static ListView timelineListView;
	public static HashMap<Long,TimelineElement> availableTweets;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Utils.setDesign(this);
		super.onCreate(savedInstanceState);
		instance = this;
		map = new MapView(this, Constants.MAPS_API_KEY);
		setContentView(R.layout.activity_timeline);
		background_image_loader = new BackgroundImageLoader(getApplicationContext());
		
		SharedPreferences pref = getSharedPreferences(Constants.PREFS_APP, 0);
		TimelineElement.tweetTimeStyle = pref.getString("pref_tweet_time_style", "dd.MM.yy HH:mm");
		
		timelineListView = (ListView) findViewById(R.id.timeline);
		registerForContextMenu(timelineListView);
		
		if (!isRunning) {
			acc = 0;
			ArrayList<User> auth_users = getAuthUsers();
			if (auth_users != null) {
				for (User u : auth_users) {
					createAccount(u);
				}
			}
		} else {
			for (Account acct : Account.all_accounts) {
				acct.start(true);
			}
		}
		
		
		timelineListView.setScrollingCacheEnabled(false);
		timelineListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//view.setBackgroundDrawable(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {0xFFFFFFFF, 0xFFCCCCCC }));
				showMapIfApplicable(parent, view, position, id);
			}
		});
		
//		timelineListView.setOnItemLongClickListener(new OnItemLongClickListener() {
//			@Override
//			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//				
//				if (current_account.getElements().getItem(position).isReplyable()) {
//					Intent replyIntent = new Intent(TimelineActivity.this, NewTweetActivity.class);
//					replyIntent.putExtra("de.geotweeter.reply_to_tweet", current_account.getElements().getItem(position));
//					startActivity(replyIntent);
//					return true;
//				} else {
//					return false;
//				}
//			}
//		});
		
		if (!isRunning) {
			if (current_account != null) {
				timelineListView.setAdapter(current_account.getElements());
				GCMRegistrar.checkDevice(this);
				GCMRegistrar.checkManifest(this);
				reg_id = GCMRegistrar.getRegistrationId(this);
				if (reg_id.equals("")) {
					GCMRegistrar.register(this, Constants.GCM_SENDER_ID);
				} else {
					for (Account acct : Account.all_accounts) {
						acct.registerForGCMMessages();
					}
				}
			} else {
				Intent addAccountIntent = new Intent(TimelineActivity.this, SettingsAccounts.class);
				startActivity(addAccountIntent);
			}
		} else {
			timelineListView.setAdapter(current_account.getElements());
		}
		
		isRunning = true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.timeline) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
			menu.setHeaderTitle(R.string.context_menu_title);
			final TimelineElement te = current_account.getElements().getItem(info.position);
			if (te.isReplyable()) {
				menu.add(R.string.respond_to_tweet).setOnMenuItemClickListener(new OnMenuItemClickListener() {
					
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						return respondToTimelineElement(te);
					}
				}); 
			}
			
			if (te instanceof Tweet) {
				Tweet tweet = (Tweet) te;
				if (tweet.entities != null) {
					/* TODO: User-Infoscreen */
//					if (tweet.entities.user_mentions != null) {
//						for (UserMention um : tweet.entities.user_mentions) {
//							menu.add('@' + um.screen_name);
//						}
//					}
					if (tweet.entities.urls != null) {
						for (final Url url : tweet.entities.urls) {
							menu.add(url.display_url).setOnMenuItemClickListener(new OnMenuItemClickListener() {
								
								@Override
								public boolean onMenuItemClick(MenuItem item) {
									return openURL(url.url);
								}
							});
						}
					}
					
					if (tweet.entities.media != null) {
						for (final Media media : tweet.entities.media) {
							menu.add(media.display_url).setOnMenuItemClickListener(new OnMenuItemClickListener() {
								
								@Override
								public boolean onMenuItemClick(MenuItem item) {
									return openURL(media.media_url);
								}
							});
						}
					}
					
					/* TODO: Twitter-Suche implementieren */
//					if (tweet.entities.hashtags != null) {
//						for (Hashtag ht : tweet.entities.hashtags) {
//							menu.add('#' + ht.text);
//						}
//					}
				}
			}
		}
	}
	
	protected boolean openURL(String url) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
		return true;
	}

	protected boolean respondToTimelineElement(TimelineElement te) {
		Intent replyIntent = new Intent(TimelineActivity.this, NewTweetActivity.class);
		replyIntent.putExtra("de.geotweeter.reply_to_tweet", te);
		startActivity(replyIntent);
		return true;
	}

	protected void showMapIfApplicable(AdapterView<?> parent, View view,
			int position, long id) {
		TimelineElement te = current_account.getElements().getItem(position);
		if (map.getParent() != null) {
			FrameLayout mapContainer = (FrameLayout) map.getParent();
			mapContainer.removeAllViews();
			RelativeLayout mapAndControls = (RelativeLayout) mapContainer.getParent();
			mapAndControls.setVisibility(View.GONE);
			if (mapAndControls.getParent() == view) {
				return;
			}
		}
		
		if (te instanceof Tweet) {
			Tweet tweet = (Tweet) te;
			if (tweet.coordinates != null) {
				float lon = tweet.coordinates.coordinates.get(0);
				float lat = tweet.coordinates.coordinates.get(1);
				GeoPoint coords = new GeoPoint((int) (lat*1e6), (int) (lon*1e6));
				
				View mapAndControls = view.findViewById(R.id.map_and_controls);
				FrameLayout mapContainer = (FrameLayout) view.findViewById(R.id.map_fragment_container);
				
				TextView zoomIn = (TextView) view.findViewById(R.id.zoom_in);
				TextView zoomOut = (TextView) view.findViewById(R.id.zoom_out);
				
				mapContainer.addView(map);
				
				List<Overlay> overlays = map.getOverlays();
				Drawable marker = MapOverlay.getLocationMarker(background_image_loader.loadBitmap(tweet.user.profile_image_url_https));
				MapOverlay overlay = new MapOverlay(marker);
				OverlayItem overlayItem = new OverlayItem(coords, null, null);
				overlay.addOverlay(overlayItem);
				overlays.add(overlay);
				
				map.setBuiltInZoomControls(false);
				map.getController().setCenter(coords);
				map.getController().setZoom(15);
				map.setVisibility(View.VISIBLE);
				
				zoomIn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						map.getController().zoomIn();
					}
				});
				zoomOut.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						map.getController().zoomOut();
					}
				});
				
				mapAndControls.setVisibility(View.VISIBLE);
			}
		}
	}

	public void onDestroy() {
		super.onDestroy();
		for (Account acct : Account.all_accounts) {
			try {
				acct.stopStream();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		instance = null;
	}
	
	public void onPause() {
		super.onPause();
		for (Account acct : Account.all_accounts) {
			acct.persistTweets(getApplicationContext());
		}
	}
	
	public void nextAccountHandler(View v) {
		acc = (acc + 1) % Account.all_accounts.size();
		current_account = Account.all_accounts.get(acc);
//		elements = current_account.getElements().getItems();
		ListView l = (ListView) findViewById(R.id.timeline);
		l.setAdapter(current_account.getElements());
		Log.d(LOG, "Changed Account: " + current_account.getUser().screen_name);
	}

	private Token getUserToken(User u) {
		SharedPreferences sp = getSharedPreferences(Constants.PREFS_APP, 0);
		return new Token(sp.getString("access_token."+String.valueOf(u.id), null), 
						  sp.getString("access_secret."+String.valueOf(u.id), null));
	}

	private ArrayList<User> getAuthUsers() {
		ArrayList<User> result = null;
		
		SharedPreferences sp = getSharedPreferences(Constants.PREFS_APP, 0);
		String accountString = sp.getString("accounts", null);
		
		if (accountString != null) {
			String[] accounts = accountString.split(" ");
			result = User.getPersistentData(getApplicationContext(), accounts);
		}
		
		return result;
	}
	
	public void createAccount(User u) {
		TimelineElementAdapter ta = new TimelineElementAdapter(this, 
				   R.layout.timeline_element, 
				   new ArrayList<TimelineElement>());
		Account acct = new Account(ta, getUserToken(u), u, getApplicationContext(), true);
		addAccount(acct);
	}
	
	public void addAccount(Account acc) {
		if (current_account == null) {
			current_account = acc;
//			elements = acc.getElements().getItems();
		}
		if (!reg_id.equals("")) {
			acc.registerForGCMMessages();
		}
		
		if (timelineListView.getAdapter() == null) {
			timelineListView.setAdapter(acc.getElements());
		}
	}

	public void newTweetClickHandler(View v) {
		startActivity(new Intent(this, NewTweetActivity.class));
	}
	
	public void markReadClickHandler(View v) {
		ListView list = (ListView)findViewById(R.id.timeline);
		TimelineElementAdapter elements = (TimelineElementAdapter)list.getAdapter();
		int pos = list.getFirstVisiblePosition()+1;
		if (pos == 1) {
			pos = 0;
		}
		TimelineElement current;
		long new_max_read_tweet_id = 0;
		long new_max_read_dm_id = 0;
		long new_max_read_mention_id = 0;
		while (pos < elements.getCount()) {
			current = elements.getItem(pos);
			if (current instanceof DirectMessage) {
				if (new_max_read_dm_id == 0) {
					new_max_read_dm_id = current.getID();
				}
			} else if (current instanceof Tweet) {
				if (new_max_read_mention_id == 0 && ((Tweet)current).mentionsUser(current_account.getUser())) {
					new_max_read_mention_id = current.getID();
				}
				if (new_max_read_tweet_id == 0) {
					new_max_read_tweet_id = current.getID();
				}
			}
			if (new_max_read_mention_id>0 && new_max_read_dm_id>0 && new_max_read_tweet_id>0) {
				break;
			}
			pos++;
		}
		current_account.setMaxReadIDs(new_max_read_tweet_id, new_max_read_mention_id, new_max_read_dm_id);
	}
	
	public void scrollDownHandler(View v) {
		ListView lvList = (ListView)findViewById(R.id.timeline);
		TimelineElementAdapter elements = (TimelineElementAdapter) lvList.getAdapter();
		int pos = 0;
		while (pos < elements.getCount()) {
			TimelineElement element = elements.getItem(pos);
			if (element instanceof Tweet && !(element instanceof DirectMessage)) {
				if (element.getID() < current_account.getMaxReadTweetID()) {
					break;
				}
			}
			pos++;
		}
		lvList.smoothScrollToPosition(pos);
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

	@Override
	protected boolean isRouteDisplayed() {
		/* Die Methode muss hier hin wegen MapActivity */
		return false;
	}
	
	public static TimelineActivity getInstance() {
		return instance;
	}
	
	public void refreshTimelineClickHandler(View v) {
		for (Account acct : Account.all_accounts) {
			acct.stopStream();
			acct.start(false);
		}
	}
	
	public static BackgroundImageLoader getBackgroundImageLoader(Context context) {
		if (background_image_loader == null) {
			background_image_loader = new BackgroundImageLoader(context);
		}
		return background_image_loader;
	}
}
