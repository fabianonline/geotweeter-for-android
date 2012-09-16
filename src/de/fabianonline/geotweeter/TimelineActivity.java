package de.fabianonline.geotweeter;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

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
        		view.setBackgroundDrawable(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {0xFFFFFFFF, 0xFFCCCCCC}));
        	}
        });
        
        addItems();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_timeline, menu);
        return true;
    }
    
    private void addItems() {
    	elements.add(new TimelineElement("fabianonline", "Longcat<br /><br />is<br /><br />long."));
    	elements.add(new TimelineElement("fabianonline", "Schau mal, mit<br />Zeilenumbrüchen."));
    	elements.add(new TimelineElement("fabianonline", "Und ein Dritter."));
    	elements.add(new TimelineElement("fabianonline", "Und noch einer."));
    	elements.add(new TimelineElement("fabianonline", "Hallo Welt. Dies ist ein Test."));
    }
}
