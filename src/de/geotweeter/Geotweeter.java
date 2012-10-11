package de.geotweeter;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(formKey="dHZaTjh2ZHcyd3dScFRTUFUzVmxlaUE6MQ")
public class Geotweeter extends Application {
	@Override
	public void onCreate() {
		ACRA.init(this);
		super.onCreate();
	}
}
