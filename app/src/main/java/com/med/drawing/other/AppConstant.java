package com.med.drawing.other;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import androidx.loader.content.CursorLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class AppConstant {
    public static String DIALOG_SHOW_KEY = "Hint_dialog";
    public static String TraceDirect = "Trace_direct";
    public static String TracePaper = "Trace_paper";
    public static String selected_id = "Trace direct";
    public static final String privacy_policy_url = "http://adtubeservices.co.in/WorldGlobleApps/privacy_policy.html";

    public static boolean isSdkHigherThan29() {
        return Build.VERSION.SDK_INT > 29;
    }

    public static Bitmap getBitmapFromAsset(Context context, String str) {
        try {
            return BitmapFactory.decodeStream(context.getAssets().open(str));
        } catch (IOException unused) {
            return null;
        }
    }

    public static Bitmap getBitmap(String str) {
        return BitmapFactory.decodeFile(str, new BitmapFactory.Options());
    }

    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        if (uri.getHost().contains("com.android.providers.media")) {
            String[] strArr = {"_data"};
            Cursor query = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, strArr, "_id=?", new String[]{DocumentsContract.getDocumentId(uri).split(":")[1]}, null);
            String string = query.moveToFirst() ? query.getString(query.getColumnIndex(strArr[0])) : "";
            query.close();
            return string;
        }
        return getRealPathFromURI_BelowAPI11(context, uri);
    }

    public static String getRealPathFromURI_API11to18(Context context, Uri uri) {
        Cursor loadInBackground = new CursorLoader(context, uri, new String[]{"_data"}, null, null, null).loadInBackground();
        if (loadInBackground != null) {
            int columnIndexOrThrow = loadInBackground.getColumnIndexOrThrow("_data");
            loadInBackground.moveToFirst();
            return loadInBackground.getString(columnIndexOrThrow);
        }
        return null;
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri uri) {
        Cursor query = context.getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
        int columnIndexOrThrow = query.getColumnIndexOrThrow("_data");
        String string = (columnIndexOrThrow < 0 || !query.moveToFirst()) ? null : query.getString(columnIndexOrThrow);
        query.close();
        return string;
    }

    public static String getRealPathFromURI(Context context, Uri uri) {
        try {
            Cursor query = context.getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
            String string = query.moveToFirst() ? query.getString(query.getColumnIndexOrThrow("_data")) : null;
            query.close();
            return string;
        } catch (Exception e) {
            e.toString();
            return null;
        }
    }

    public static Bitmap decodeFile(File file) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            int i = 1;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, options);
            while ((options.outWidth / i) / 2 >= 340 && (options.outHeight / i) / 2 >= 340) {
                i *= 2;
            }
            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inSampleSize = i;
            return BitmapFactory.decodeStream(new FileInputStream(file), null, options2);
        } catch (FileNotFoundException e) {
            e.toString();
            return null;
        }
    }
}
