package de.geotweeter.activities;

import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import de.geotweeter.R;
import de.geotweeter.Utils;

public class AboutActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Utils.setDesign(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_about);
        
        try {
			((TextView) findViewById(R.id.txtVersion)).setText("Version " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {}
        
        ((TextView) findViewById(R.id.txtAuthors)).setText("Julian Kürby (@Rimgar_)\nLutz Krumme (@el_emka)\nFabian Schlenz (@fabianonline)");
        addLicenses();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public class LibraryListElement {
    	public String name;
    	public String type;
    	public String authorName;
    	public String license;
    	public String url;
    	public String licenseText;
    	
		public LibraryListElement(String name, String authorName, String type, String license, String url) {
			super();
			this.name = name;
			this.authorName = authorName;
			this.type = type;
			this.license = license;
			this.url = url;
		}
	}
    

    private void addLicenses() {
    	LibraryListElement l;
    	
    	l = new LibraryListElement("Silk Icons", "Mark James", "Icons", "CC BY 2.5", "http://www.famfamfam.com/lab/icons/silk/");
    	addToList(l);
    	
    	l = new LibraryListElement("Scribe", "Pablo Fernandez", "Library", "MIT License", "https://github.com/fernandezpablo85/scribe-java");
    	l.licenseText = "The MIT License\n\nCopyright (c) 2010 Pablo Fernandez\n\nPermission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the \"Software\"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:\n\nThe above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.";
    	addToList(l);
    	
    	l = new LibraryListElement("FastJSON", "Alibaba Group", "Library", "Apache License 2.0", "https://github.com/fernandezpablo85/scribe-java");
    	l.licenseText = "Copyright 1999-2101 Alibaba Group.\n\nLicensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License.\nYou may obtain a copy of the License at\n\nhttp://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\nSee the License for the specific language governing permissions and limitations under the License.";
    	addToList(l);
    }
    
    private void addToList(LibraryListElement elm) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.settings_about_libraries_element, null);
		
		((TextView) view.findViewById(R.id.txtLibraryName)).setText(elm.name);
		((TextView) view.findViewById(R.id.txtAuthorName)).setText(elm.authorName);
		((TextView) view.findViewById(R.id.txtURL)).setText(elm.url);
		((TextView) view.findViewById(R.id.txtType)).setText(elm.type);
		((TextView) view.findViewById(R.id.txtLicense)).setText(elm.license);
		
		if (elm.licenseText != null) {
			((TextView) view.findViewById(R.id.txtLicenseText)).setText(elm.licenseText);
			view.findViewById(R.id.txtLicenseText).setVisibility(View.VISIBLE);
		} else {
			view.findViewById(R.id.txtLicenseText).setVisibility(View.GONE);
		}
		
		((LinearLayout) findViewById(R.id.layoutList)).addView(view);
    }
}