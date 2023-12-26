package com.med.drawing.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class PermissionUtils {
    public static void checkPermission(Activity activity, String str, int i) {
        if (ContextCompat.checkSelfPermission(activity, str) != 0) {
            ActivityCompat.requestPermissions(activity, new String[]{str}, i);
        }
    }

    public static boolean isCameraGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, "android.permission.CAMERA") == 0;
    }
}
