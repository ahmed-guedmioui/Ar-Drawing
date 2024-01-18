package com.ardrawing.sketchtrace.util.other;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class AppConstant {
    public static String TraceDirect = "Trace_direct";
    public static String selected_id = "Trace direct";

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

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri uri) {
        Cursor query = context.getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
        int columnIndexOrThrow = query.getColumnIndexOrThrow("_data");
        String string = (columnIndexOrThrow < 0 || !query.moveToFirst()) ? null : query.getString(columnIndexOrThrow);
        query.close();
        return string;
    }

}
