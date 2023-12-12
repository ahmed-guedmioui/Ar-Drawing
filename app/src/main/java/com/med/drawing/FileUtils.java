package com.med.drawing;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import com.anggrayudi.storage.file.DocumentFileCompat;
import com.anggrayudi.storage.file.StorageId;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


public class FileUtils {
    private static Uri contentUri;
    private static Context context;

    public FileUtils(Context context2) {
        context = context2;
    }

    public static String getPath(Uri uri) {
        Uri uri2 = null;
        Cursor cursor = null;
        if (Build.VERSION.SDK_INT >= 19) {
            if (isExternalStorageDocument(uri)) {
                String[] split = DocumentsContract.getDocumentId(uri).split(":");
                String str = split[0];
                String pathFromExtSD = getPathFromExtSD(split);
                if (pathFromExtSD != "") {
                    return pathFromExtSD;
                }
                return null;
            }
            if (isDownloadsDocument(uri)) {
                if (Build.VERSION.SDK_INT >= 23) {

                        Cursor query = context.getContentResolver().query(uri, new String[]{"_display_name"}, null, null, null);
                        if (query != null) {
                            try {
                                if (query.moveToFirst()) {
                                    String str2 = Environment.getExternalStorageDirectory().toString() + "/Download/" + query.getString(0);
                                    if (!TextUtils.isEmpty(str2)) {
                                        if (query != null) {
                                            query.close();
                                        }
                                        return str2;
                                    }
                                }
                            } catch (Exception e) {
                              e.printStackTrace();
                            }
                        }
                        if (query != null) {
                            query.close();
                        }
                        String documentId = DocumentsContract.getDocumentId(uri);
                        if (!TextUtils.isEmpty(documentId)) {
                            if (documentId.startsWith("raw:")) {
                                return documentId.replaceFirst("raw:", "");
                            }
                            try {
                                return getDataColumn(context, ContentUris.withAppendedId(Uri.parse(new String[]{"content://downloads/public_downloads", "content://downloads/my_downloads"}[0]), Long.valueOf(documentId).longValue()), null, null);
                            } catch (NumberFormatException unused) {
                                return uri.getPath().replaceFirst("^/document/raw:", "").replaceFirst("^raw:", "");
                            }
                        }

                } else {
                    String documentId2 = DocumentsContract.getDocumentId(uri);
                    if (documentId2.startsWith("raw:")) {
                        return documentId2.replaceFirst("raw:", "");
                    }
                    try {
                        contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId2).longValue());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    Uri uri3 = contentUri;
                    if (uri3 != null) {
                        return getDataColumn(context, uri3, null, null);
                    }
                }
            }
            if (isMediaDocument(uri)) {
                String[] split2 = DocumentsContract.getDocumentId(uri).split(":");
                String str3 = split2[0];
                if ("image".equals(str3)) {
                    uri2 = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(str3)) {
                    uri2 = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(str3)) {
                    uri2 = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                return getDataColumn(context, uri2, "_id=?", new String[]{split2[1]});
            } else if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(uri);
            } else {
                if (isWhatsAppFile(uri)) {
                    return getFilePathForWhatsApp(uri);
                }
                if ("content".equalsIgnoreCase(uri.getScheme())) {
                    if (isGooglePhotosUri(uri)) {
                        return uri.getLastPathSegment();
                    }
                    if (isGoogleDriveUri(uri)) {
                        return getDriveFilePath(uri);
                    }
                    if (Build.VERSION.SDK_INT >= 29) {
                        return copyFileToInternalStorage(uri, "userfiles");
                    }
                    return getDataColumn(context, uri, null, null);
                } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                    return uri.getPath();
                }
            }
        } else if (isWhatsAppFile(uri)) {
            return getFilePathForWhatsApp(uri);
        } else {
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                try {
                    Cursor query2 = context.getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
                    int columnIndexOrThrow = query2.getColumnIndexOrThrow("_data");
                    if (query2.moveToFirst()) {
                        return query2.getString(columnIndexOrThrow);
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        return null;
    }

    private static boolean fileExists(String str) {
        return new File(str).exists();
    }

    private static String getPathFromExtSD(String[] strArr) {
        String str = strArr[0];
        String str2 = "/" + strArr[1];
        if (StorageId.PRIMARY.equalsIgnoreCase(str)) {
            String str3 = Environment.getExternalStorageDirectory() + str2;
            if (fileExists(str3)) {
                return str3;
            }
        }
        String str4 = System.getenv("SECONDARY_STORAGE") + str2;
        if (fileExists(str4)) {
            return str4;
        }
        String str5 = System.getenv("EXTERNAL_STORAGE") + str2;
        if (fileExists(str5)) {
        }
        return str5;
    }

    private static String getDriveFilePath(Uri uri) {
        Cursor query = context.getContentResolver().query(uri, null, null, null, null);
        int columnIndex = query.getColumnIndex("_display_name");
        int columnIndex2 = query.getColumnIndex("_size");
        query.moveToFirst();
        String string = query.getString(columnIndex);
        Long.toString(query.getLong(columnIndex2));
        File file = new File(context.getCacheDir(), string);
        try {
            InputStream openInputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] bArr = new byte[Math.min(openInputStream.available(), 1048576)];
            while (true) {
                int read = openInputStream.read(bArr);
                if (read == -1) {
                    break;
                }
                fileOutputStream.write(bArr, 0, read);
            }
            Log.e("File Size", "Size " + file.length());
            openInputStream.close();
            fileOutputStream.close();
            Log.e("File Path", "Path " + file.getPath());
            Log.e("File Size", "Size " + file.length());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return file.getPath();
    }

    private static String copyFileToInternalStorage(Uri uri, String str) {
        File file;
        Cursor query = context.getContentResolver().query(uri, new String[]{"_display_name", "_size"}, null, null, null);
        int columnIndex = query.getColumnIndex("_display_name");
        int columnIndex2 = query.getColumnIndex("_size");
        query.moveToFirst();
        String string = query.getString(columnIndex);
        Long.toString(query.getLong(columnIndex2));
        if (!str.equals("")) {
            File file2 = new File(context.getFilesDir() + "/" + str);
            if (!file2.exists()) {
                file2.mkdir();
            }
            file = new File(context.getFilesDir() + "/" + str + "/" + string);
        } else {
            file = new File(context.getFilesDir() + "/" + string);
        }
        try {
            InputStream openInputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] bArr = new byte[1024];
            while (true) {
                int read = openInputStream.read(bArr);
                if (read == -1) {
                    break;
                }
                fileOutputStream.write(bArr, 0, read);
            }
            openInputStream.close();
            fileOutputStream.close();
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return file.getPath();
    }

    private static String getFilePathForWhatsApp(Uri uri) {
        return copyFileToInternalStorage(uri, "whatsapp");
    }

    private static String getDataColumn(Context context2, Uri uri, String str, String[] strArr) {
        Cursor cursor = null;

            Cursor query = context2.getContentResolver().query(uri, new String[]{"_data"}, str, strArr, null);
            if (query != null) {
                try {
                    if (query.moveToFirst()) {
                        String string = query.getString(query.getColumnIndexOrThrow("_data"));
                        if (query != null) {
                            query.close();
                        }
                        return string;
                    }
                } catch (Exception th) {
                    th.printStackTrace();
                }
            }
            if (query != null) {
                query.close();
            }
            return null;

    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return DocumentFileCompat.EXTERNAL_STORAGE_AUTHORITY.equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return DocumentFileCompat.DOWNLOADS_FOLDER_AUTHORITY.equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return DocumentFileCompat.MEDIA_FOLDER_AUTHORITY.equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static boolean isWhatsAppFile(Uri uri) {
        return "com.whatsapp.provider.media".equals(uri.getAuthority());
    }

    private static boolean isGoogleDriveUri(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority()) || "com.google.android.apps.docs.storage.legacy".equals(uri.getAuthority());
    }
}
