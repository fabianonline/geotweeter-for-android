package de.fabianonline.geotweeter;

import java.util.ArrayList;

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
import android.widget.ListView;
import android.widget.Toast;
import de.fabianonline.geotweeter.activities.NewTweetActivity;

public class TimelineActivity extends Activity {
	private final String LOG = "TimelineActivity";
	
	private TimelineElementAdapter ta;
	private ArrayList<TimelineElement> elements;
	private ArrayList<Account> accounts = new ArrayList<Account>();
	public static Account current_account = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        elements = new ArrayList<TimelineElement>();
        ta = new TimelineElementAdapter(this, R.layout.timeline_element, elements);
        ListView l = (ListView) findViewById(R.id.timeline);
        l.setAdapter(ta);
        l.setOnItemClickListener(new OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		Log.d(LOG, "In onItemClick");
        		view.setBackgroundDrawable(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {0xFFFFFFFF, 0xFFCCCCCC}));
        	}
        });
        
        addAccount(new Account(ta));
    }
    
    public void addAccount(Account acc) {
    	accounts.add(acc);
    	if (current_account == null) {
    		current_account = acc;
    	}
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
			Intent settingsActivity = new Intent(this, GeneralPrefsActivity.class);
			startActivity(settingsActivity);
			return true;
		}
    	return true;
    }
}
