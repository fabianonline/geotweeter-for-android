package de.fabianonline.geotweeter;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ListView;

public class TimelineActivity extends Activity {
	
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
