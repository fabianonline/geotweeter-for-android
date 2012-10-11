package de.geotweeter;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(formKey                = "dHZaTjh2ZHcyd3dScFRTUFUzVmxlaUE6MQ",
                mode                   = ReportingInteractionMode.DIALOG,
                resToastText           = R.string.crash_toast_text,
                resDialogText          = R.string.crash_dialog_text,
                resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
                resDialogOkToast       = R.string.crash_dialog_ok_toast)
public class Geotweeter extends Application {
	@Override
	public void onCreate() {
		ACRA.init(this);
		super.onCreate();
	}
}
