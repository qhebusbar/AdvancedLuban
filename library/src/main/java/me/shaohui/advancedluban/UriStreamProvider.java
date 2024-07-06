package me.shaohui.advancedluban;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;


import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class UriStreamProvider extends InputStreamAdapter<Uri> {

    private final Uri uri;
    private final Context context;

    UriStreamProvider(@NonNull Context context, @NonNull Uri uri) {
        this.context = context;
        this.uri = uri;
    }

    @Override
    public InputStream openInternal() throws IOException {
        return context.getContentResolver().openInputStream(uri);
    }

    @Override
    public Uri param() {
        return uri;
    }

    @Override
    public long length() {
        try {
            switch (Objects.requireNonNull(uri.getScheme())) {
                case ContentResolver.SCHEME_FILE:
                    return new File(Objects.requireNonNull(uri.getPath())).length();
                case ContentResolver.SCHEME_CONTENT:
                    Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                    long tempLength = 0;
                    if (cursor != null && cursor.moveToFirst()) {
                        tempLength = new File(cursor.getString(cursor.getColumnIndex("_data"))).length();
                        cursor.close();
                    }
                    return tempLength;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int[] size() {
        int[] res = new int[2];
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = 1;
            BitmapFactory.decodeStream(open(), null, options);
            res[0] = options.outWidth;
            res[1] = options.outHeight;
            return res;
        } catch (Exception e) {
            return res;
        }
    }

    @Override
    public int spinAngle() {
        int degree = 0;
        ExifInterface exifInterface;
        try {
            ParcelFileDescriptor r = context.getContentResolver().openFileDescriptor(uri, "r");
            exifInterface = new ExifInterface(Objects.requireNonNull(r).getFileDescriptor());
            r.close();
        } catch (Exception e) {
            // 图片不支持获取角度
            return 0;
        }
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
        }
        return degree;
    }
}
