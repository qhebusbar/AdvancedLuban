/*Copyright 2016 Zheng Zibin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package me.shaohui.advancedluban;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.util.Log;

import java.io.File;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class Luban<T> {

    public static final int FIRST_GEAR = 1;

    public static final int THIRD_GEAR = 3;

    public static final int CUSTOM_GEAR = 4;

    private static final String TAG = "Luban";

    private static final String DEFAULT_DISK_CACHE_DIR = "luban_disk_cache";
    private final LubanBuilder mBuilder;
    private InputStreamProvider<T> mProvider;
    private List<InputStreamProvider<T>> mProviders;

    private Luban(File cacheDir) {
        mBuilder = new LubanBuilder(cacheDir);
    }

    public static Luban<Uri> compressUri(Context context, Uri uri) {
        Luban<Uri> luban = new Luban<>(Luban.getPhotoCacheDir(context));
        InputStreamProvider<Uri> provider = Transformer.transform(context, uri);
        luban.mProvider = provider;
        luban.mProviders = Collections.singletonList(provider);
        return luban;
    }

    public static Luban<File> compressFile(Context context, File file) {
        Luban<File> luban = new Luban<>(Luban.getPhotoCacheDir(context));
        InputStreamProvider<File> provider = Transformer.transform(context, file);
        luban.mProvider = provider;
        luban.mProviders = Collections.singletonList(provider);
        return luban;
    }

    public static Luban<String> compressPath(Context context, String path) {
        Luban<String> luban = new Luban<>(Luban.getPhotoCacheDir(context));
        InputStreamProvider<String> provider = Transformer.transform(context, path);
        luban.mProvider = provider;
        luban.mProviders = Collections.singletonList(provider);
        return luban;
    }

    public static Luban<Uri> compressUris(Context context, List<Uri> uris) {
        Luban<Uri> luban = new Luban<>(Luban.getPhotoCacheDir(context));
        List<InputStreamProvider<Uri>> providers = Transformer.transform(context, uris);
        luban.mProviders = providers;
        luban.mProvider = providers.get(0);
        return luban;
    }

    public static Luban<File> compressFiles(Context context, List<File> files) {
        Luban<File> luban = new Luban<>(Luban.getPhotoCacheDir(context));
        List<InputStreamProvider<File>> providers = Transformer.transform(context, files);
        luban.mProviders = providers;
        luban.mProvider = providers.get(0);
        return luban;
    }

    public static Luban<String> compressPaths(Context context, List<String> paths) {
        Luban<String> luban = new Luban<>(Luban.getPhotoCacheDir(context));
        List<InputStreamProvider<String>> providers = Transformer.transform(context, paths);
        luban.mProviders = providers;
        luban.mProvider = providers.get(0);
        return luban;
    }

    private static boolean isCacheDirValid(File cacheDir) {
        return cacheDir.isDirectory() && (cacheDir.exists() || cacheDir.mkdirs());
    }

    /**
     * Returns a directory with a default name in the private cache directory of the application to
     * use to store
     * retrieved media and thumbnails.
     *
     * @param context A context.
     * @see #getPhotoCacheDir(Context, String)
     */
    private static File getPhotoCacheDir(Context context) {
        return getPhotoCacheDir(context, Luban.DEFAULT_DISK_CACHE_DIR);
    }

    /**
     * Returns a directory with the given name in the private cache directory of the application to
     * use to store
     * retrieved media and thumbnails.
     *
     * @param context   A context.
     * @param cacheName The name of the subdirectory in which to store the cache.
     * @see #getPhotoCacheDir(Context)
     */
    private static File getPhotoCacheDir(Context context, String cacheName) {
        File cacheDir = context.getCacheDir();
        if (cacheDir != null) {
            File result = new File(cacheDir, cacheName);
            if (!result.mkdirs() && (!result.exists() || !result.isDirectory())) {
                // File wasn't able to create a directory, or the result exists but not a directory
                return null;
            }
            return result;
        }
        if (Log.isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG, "default disk cache dir is null");
        }
        return null;
    }

    public Luban<T> setCacheDir(File cacheDir) {
        mBuilder.cacheDir = cacheDir;
        return this;
    }

    /**
     * 自定义压缩模式 FIRST_GEAR、THIRD_GEAR、CUSTOM_GEAR
     */
    public Luban<T> putGear(@GEAR int gear) {
        mBuilder.gear = gear;
        return this;
    }

    /**
     * 自定义图片压缩格式
     */
    public Luban<T> setCompressFormat(Bitmap.CompressFormat compressFormat) {
        mBuilder.compressFormat = compressFormat;
        return this;
    }

    /**
     * CUSTOM_GEAR 指定目标图片的最大体积
     */
    public Luban<T> setMaxSize(int size) {
        mBuilder.maxSize = size;
        return this;
    }

    /**
     * CUSTOM_GEAR 指定目标图片的最大宽度
     *
     * @param width 最大宽度
     */
    public Luban<T> setMaxWidth(int width) {
        mBuilder.maxWidth = width;
        return this;
    }

    /**
     * CUSTOM_GEAR 指定目标图片的最大高度
     *
     * @param height 最大高度
     */
    public Luban<T> setMaxHeight(int height) {
        mBuilder.maxHeight = height;
        return this;
    }

    /**
     * listener调用方式，在主线程订阅并将返回结果通过 listener 通知调用方
     *
     * @param listener 接收回调结果
     */
    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void launch(final OnCompressListener<T> listener) {
        asObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) {
                        listener.onStart();
                    }
                }).subscribe(new Consumer<T>() {
                    @Override
                    public void accept(T param) {
                        listener.onSuccess(param);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        listener.onError(throwable);
                    }
                });
    }

    /**
     * listener调用方式，在主线程订阅并将返回结果通过 listener 通知调用方
     *
     * @param listener 接收回调结果
     */
    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void launch(final OnMultiCompressListener<T> listener) {
        asListObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) {
                        listener.onStart();
                    }
                }).subscribe(new Consumer<List<T>>() {
                    @Override
                    public void accept(List<T> params) {
                        listener.onSuccess(params);
                    }

                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        listener.onError(throwable);
                    }
                });
    }

    // Utils

    /**
     * 返回File Observable
     */
    public Observable<T> asObservable() {
        LubanCompressor compressor = new LubanCompressor(mBuilder);
        return compressor.singleAction(mProvider);
    }

    /**
     * 返回fileList Observable
     */
    public Observable<List<T>> asListObservable() {
        LubanCompressor compressor = new LubanCompressor(mBuilder);
        return compressor.multiAction(mProviders);
    }

    /**
     * 清空Luban所产生的缓存
     * Clears the cache generated by Luban
     */
    public Luban<T> clearCache() {
        if (mBuilder.cacheDir.exists()) {
            deleteFile(mBuilder.cacheDir);
        }
        return this;
    }

    /**
     * 清空目标文件或文件夹
     * Empty the target file or folder
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteFile(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File file : fileOrDirectory.listFiles()) {
                deleteFile(file);
            }
        }
        fileOrDirectory.delete();
    }

    @IntDef({FIRST_GEAR, THIRD_GEAR, CUSTOM_GEAR})
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.SOURCE)
    @Documented
    @Inherited
    @interface GEAR {

    }
}