package de.geotweeter;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(formKey                = "",
                formUri                = Constants.URI_REPORT_CRASHES,
                mode                   = ReportingInteractionMode.DIALOG,
                resToastText           = R.string.crash_toast_text,
                resDialogText          = R.string.crash_dialog_text,
                resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
                resDialogOkToast       = R.string.crash_dialog_ok_toast,
                logcatArguments        = {"-t", "200", "-v", "threadtime", "dalvikvm:s"})
public class Geotweeter extends Application {
	@Override
	public void onCreate() {
		ACRA.init(this);
		super.onCreate();
	}
}
