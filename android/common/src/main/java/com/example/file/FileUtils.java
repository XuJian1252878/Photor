package com.example.file;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;


/**
 * Created by xujian on 2018/2/5.
 */

public class FileUtils {

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";

    public static File createTmpFile(Context context) throws IOException {
        File dir;
        // Storage state if the media is present and mounted at its mount point with read/write access.
        if (TextUtils.equals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED)) {
            /**
             * Get a top-level shared/external storage directory for placing files of a particular
             * type. This is where the user will typically place and manage their own files, so you
             * should be careful about what you put here to ensure you don't erase their files or
             * get in the way of their own organization.
             * On devices with multiple users (as described by UserManager), each user has their own
             * isolated shared storage. Applications only have access to the shared storage for the
             * user they're running as.
             */
            // 尝试将文件放在公共的外部存储的DCIM目录中
            dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            if (!dir.exists()) {
                dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera");
                if (!dir.exists()) {
                    // 如果没有在外部存储的公共目录不存在，那么存储在外部存储中的应用程序 私有目录中
                    dir = getCacheDirectory(context, true);
                }
            }
        } else {
            // 外部存储没有准备就绪的情况下，那么将文件存储在内部存储的应用目录中
            dir = getCacheDirectory(context, true);
        }
        return File.createTempFile(JPEG_FILE_PREFIX, JPEG_FILE_SUFFIX, dir);
    }

    /**
     * Returns application cache directory. Cache directory will be created on SD card
     * <i>("/Android/data/[app_package_name]/cache")</i> if card is mounted and app has appropriate permission. Else -
     * Android defines cache directory on device's file system.
     *
     * @param context Application context
     * @return Cache {@link File directory}.<br />
     * <b>NOTE:</b> Can be null in some unpredictable cases (if SD card is unmounted and
     * {@link Context#getCacheDir() Context.getCacheDir()} returns null).
     */
    public static File getCacheDirectory(Context context) {
        return getCacheDirectory(context, true);
    }


    /**
     * Returns application cache directory. Cache directory will be created on SD card
     * <i>("/Android/data/[app_package_name]/cache")</i> (if card is mounted and app has appropriate permission) or
     * on device's file system depending incoming parameters.
     *
     * @param context        Application context
     * @param preferExternal Whether prefer external location for cache
     * @return Cache {@link File directory}.<br />
     * <b>NOTE:</b> Can be null in some unpredictable cases (if SD card is unmounted and
     * {@link Context#getCacheDir() Context.getCacheDir()} returns null).
     */
    public static File getCacheDirectory(Context context, boolean preferExternal) {
        File appCacheDir = null;
        String externalStorageState;

        try {
            externalStorageState = Environment.getExternalStorageState();
        } catch (NullPointerException e) {
            externalStorageState = "";
        } catch (IncompatibleClassChangeError e) {
            externalStorageState = "";
        }

        // 在外部存储上文件应用的私有cache文件夹
        if (preferExternal && Environment.MEDIA_MOUNTED.equals(externalStorageState) && hasExternalStoragePermission(context)) {
            appCacheDir = getExternalCacheDir(context);
        }

        // 如果在外部存储上创建应用的私有cache文件夹失败，那么创建内部的cache文件夹
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }

        if (appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/cache/";
            appCacheDir = new File(cacheDirPath);
        }

        // 确保这个appCacheDir存在
        if(!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                return appCacheDir; // 无法创建Cache文件夹的话，那么交给上一级处理。
            }
        }

        return appCacheDir;
    }

    /**
     * Returns individual application cache directory (for only image caching from ImageLoader). Cache directory will be
     * created on SD card <i>("/Android/data/[app_package_name]/cache/uil-images")</i> if card is mounted and app has
     * appropriate permission. Else - Android defines cache directory on device's file system.
     *
     * @param context Application context
     * @param cacheDir Cache directory path (e.g.: "AppCacheDir", "AppDir/cache/images")
     * @return Cache {@link File directory}
     */
    public static File getIndividualCacheDirectory(Context context, String cacheDir) {
        File appCacheDir = getCacheDirectory(context);
        File individualCacheDir = new File(appCacheDir, cacheDir);
        if (!individualCacheDir.exists()) {
            if (!individualCacheDir.mkdirs()) {
                individualCacheDir = appCacheDir;
            }
        }
        return individualCacheDir;
    }


    /**
     * 创建关于应用的外部存储中的 cache 文件夹
     * Environment.getExternalStorageDirectory(): /storage/sdcard0
     * ﻿所有应用程序的外部存储的私有文件都放在根目录的/Android/data/下，目录形式为/Android/data/<package_name>/
     * @param context
     * @return 成功：/storage/sdcard0  /Android/data/<package_name>/  cache
     */
    public static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");

        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return appCacheDir;
    }

    /**
     * 检查程序是否有外部存储权限
     * @param context
     * @return boolean
     */
    private static boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
        return PackageManager.PERMISSION_GRANTED == perm;
    }

    public static boolean fileIsExists(String path) {
        if (path == null || path.trim().length() <= 0) {
            return false;
        }

        try {
            File file = new File(path);
            if (!file.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
