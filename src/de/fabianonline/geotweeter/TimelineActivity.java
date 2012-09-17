package de.fabianonline.geotweeter;

import java.util.ArrayList;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import android.os.Bundle;
import android.os.Debug;
import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.Menu;
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
}
