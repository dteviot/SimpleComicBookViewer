package com.dteviot.simpleComicBookViewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.widget.Toast;

/*
 * Assorted utility functions
 */
public class Utility {
    public static final String ERROR_STRING_ID_EXTRA = "ERROR_STRING_ID_EXTRA";

    public static void showToast(Context context, int stringId) {
        Toast msg = Toast.makeText(context, stringId, Toast.LENGTH_SHORT);
        msg.setGravity(Gravity.CENTER, msg.getXOffset() / 2, msg.getXOffset() / 2);
        msg.show();
    }
    
    public static void finishWithError(Activity activity, int stringId) {
        Intent intent = new Intent();
        intent.putExtra(ERROR_STRING_ID_EXTRA, stringId);
        activity.setResult(Activity.RESULT_CANCELED, intent);
        activity.finish();
    }
    
    public static void showErrorToast(Context context, Intent intent) {
        if (intent != null) {
            int stringId = intent.getIntExtra(ERROR_STRING_ID_EXTRA, 0);
            if (stringId != 0) {
                showToast(context, stringId);
            }
        }
    }
}
