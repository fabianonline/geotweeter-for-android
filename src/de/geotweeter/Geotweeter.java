package de.geotweeter;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.Context;

@ReportsCrashes(formKey                = "",
                formUri                = "" /* will be overwritten in constructor */,
                mode                   = ReportingInteractionMode.DIALOG,
                resToastText           = R.string.crash_toast_text,
                resDialogText          = R.string.crash_dialog_text,
                resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
                resDialogOkToast       = R.string.crash_dialog_ok_toast,
                logcatArguments        = {"-t", "200", "-v", "threadtime", "dalvikvm:s"},
                sharedPreferencesName  = Constants.PREFS_APP,
                additionalSharedPreferences = {Constants.PREFS_APP, Constants.PREFS_ACCOUNTS},
                excludeMatchingSharedPreferencesKeys = {"^access_"})
public class Geotweeter extends Application {
	private static Context myContext;
	
	@Override
	public void onCreate() {
		myContext = this;
		ACRAConfiguration config = ACRA.getNewDefaultConfig(this);
		config.setFormUri(Utils.getProperty("crashreport.server.url") + "/send");
		ACRA.setConfig(config);
		ACRA.init(this);
		super.onCreate();
	}
	
	public static Context getContext() {
		return myContext;
	}
}
