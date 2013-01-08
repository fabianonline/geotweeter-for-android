package de.geotweeter.activities;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
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
    	
    	l = new LibraryListElement("Entypo pictograms", "Daniel Bruce", "Icons", " SIL Open Font License", "http://www.entypo.com/");
    	l.licenseText = "Copyright (c) Daniel Bruce (info@entypo.com),with Reserved Font Name Entypo.\n\nThis Font Software is licensed under the SIL Open Font License, Version 1.1.This license is copied below, and is also available with a FAQ at:http://scripts.sil.org/OFL\n\n-----------------------------------------------------------\nSIL OPEN FONT LICENSE Version 1.1 - 26 February 2007\n-----------------------------------------------------------\n\nPREAMBLEThe goals of the Open Font License (OFL) are to stimulate worldwidedevelopment of collaborative font projects, to support the font creationefforts of academic and linguistic communities, and to provide a free andopen framework in which fonts may be shared and improved in partnershipwith others.\n\nThe OFL allows the licensed fonts to be used, studied, modified andredistributed freely as long as they are not sold by themselves. Thefonts, including any derivative works, can be bundled, embedded, redistributed and/or sold with any software provided that any reservednames are not used by derivative works. The fonts and derivatives,however, cannot be released under any other type of license. Therequirement for fonts to remain under this license does not applyto any document created using the fonts or their derivatives.\n\nDEFINITIONS\"Font Software\" refers to the set of files released by the CopyrightHolder(s) under this license and clearly marked as such. This mayinclude source files, build scripts and documentation.\n\n\"Reserved Font Name\" refers to any names specified as such after thecopyright statement(s).\n\n\"Original Version\" refers to the collection of Font Software components asdistributed by the Copyright Holder(s).\n\n\"Modified Version\" refers to any derivative made by adding to, deleting,or substituting -- in part or in whole -- any of the components of theOriginal Version, by changing formats or by porting the Font Software to anew environment.\n\n\"Author\" refers to any designer, engineer, programmer, technicalwriter or other person who contributed to the Font Software.\n\nPERMISSION & CONDITIONSPermission is hereby granted, free of charge, to any person obtaininga copy of the Font Software, to use, study, copy, merge, embed, modify,redistribute, and sell modified and unmodified copies of the FontSoftware, subject to the following conditions:\n\n1) Neither the Font Software nor any of its individual components,in Original or Modified Versions, may be sold by itself.\n\n2) Original or Modified Versions of the Font Software may be bundled,redistributed and/or sold with any software, provided that each copycontains the above copyright notice and this license. These can beincluded either as stand-alone text files, human-readable headers orin the appropriate machine-readable metadata fields within text orbinary files as long as those fields can be easily viewed by the user.\n\n3) No Modified Version of the Font Software may use the Reserved FontName(s) unless explicit written permission is granted by the correspondingCopyright Holder. This restriction only applies to the primary font name aspresented to the users.\n\n4) The name(s) of the Copyright Holder(s) or the Author(s) of the FontSoftware shall not be used to promote, endorse or advertise anyModified Version, except to acknowledge the contribution(s) of theCopyright Holder(s) and the Author(s) or with their explicit writtenpermission.\n\n5) The Font Software, modified or unmodified, in part or in whole,must be distributed entirely under this license, and must not bedistributed under any other license. The requirement for fonts toremain under this license does not apply to any document createdusing the Font Software.\n\nTERMINATIONThis license becomes null and void if any of the above conditions arenot met.\n\nDISCLAIMERTHE FONT SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND,EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO ANY WARRANTIES OFMERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENTOF COPYRIGHT, PATENT, TRADEMARK, OR OTHER RIGHT. IN NO EVENT SHALL THECOPYRIGHT HOLDER BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,INCLUDING ANY GENERAL, SPECIAL, INDIRECT, INCIDENTAL, OR CONSEQUENTIALDAMAGES, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISINGFROM, OUT OF THE USE OR INABILITY TO USE THE FONT SOFTWARE OR FROMOTHER DEALINGS IN THE FONT SOFTWARE.";
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