package de.fabianonline.geotweeter;

import java.util.ArrayList;
import java.util.prefs.Preferences;

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

public class TimelineActivity extends Activity {
	private final String LOG = "TimelineActivity";
	
	private TimelineElementAdapter ta;
	private ArrayList<TimelineElement> elements;

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
        		Toast.makeText(getBaseContext(), "onClick", Toast.LENGTH_LONG).show();
        		view.setBackgroundDrawable(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {0xFFFFFFFF, 0xFFCCCCCC}));
        	}
        });
        new Account(ta);
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
			Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
			startActivity(settingsActivity);
			return true;
		}
    	return true;
    }
}
