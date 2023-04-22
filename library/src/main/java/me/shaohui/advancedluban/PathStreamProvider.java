package me.shaohui.advancedluban;

import android.graphics.BitmapFactory;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PathStreamProvider extends InputStreamAdapter<String> {

    private final String path;

    PathStreamProvider(String path) {
        this.path = path;
    }

    @SuppressWarnings("IOStreamConstructor")
    @Override
    public InputStream openInternal() throws IOException {
        return new FileInputStream(path);
    }

    @Override
    public String param() {
        return path;
    }

    @Override
    public long length() {
        return new File(path).length();
    }

    @Override
    public int[] size() {
        int[] res = new int[2];
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        BitmapFactory.decodeFile(path, options);
        res[0] = options.outWidth;
        res[1] = options.outHeight;
        return res;
    }

    @Override
    public int spinAngle() {
        int degree = 0;
        ExifInterface exifInterface;
        try {
            exifInterface = new ExifInterface(path);
        } catch (IOException e) {
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
