package me.shaohui.advancedluban;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class Transformer {

    static <T> T transform(InputStreamProvider<T> provider, File file) {
        T param = provider.param();
        if (param instanceof String) {
            return (T) file.getAbsolutePath();
        }
        if (param instanceof File) {
            return (T) file;
        }
        if (param instanceof Uri) {
            //todo 将文件转为Uri
            return (T) Uri.fromFile(file);
        }
        return null;
    }


    static <T> InputStreamProvider<T> transform(Context context, T param) {
        if (param instanceof File) {
            return (InputStreamProvider<T>) new FileStreamProvider((File) param);
        }
        if (param instanceof String) {
            return (InputStreamProvider<T>) new PathStreamProvider((String) param);
        }
        if (param instanceof Uri) {
            return (InputStreamProvider<T>) new UriStreamProvider(context, (Uri) param);
        }
        return null;
    }

    static <T> List<InputStreamProvider<T>> transform(Context context, List<T> params) {
        List<InputStreamProvider<T>> result = new ArrayList<>();
        for (T param : params) {
            result.add(transform(context, param));
        }
        return result;
    }
}
