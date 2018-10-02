package com.example.file;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Created by xujian on 2018/2/5.
 */

public class FileUtils {

    public static final String FOLDER_NAME = "photor";
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


    public static String generateImgAbsPath() {
        if (TextUtils.equals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED)) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
            String imgFileName = "JPEG_" + timeStamp + ".jpg";
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            if (!storageDir.exists()) {
                if (!storageDir.mkdirs()) {
                    Log.e("TAG", "Throwing Errors....");
                }
            }

            File image = new File(storageDir, imgFileName);
            String imgPath = image.getAbsolutePath();
            return imgPath;
        }
        return null;
    }


    /**
     * 创建录制视频的存储路径
     * @return
     */
    public static File generateVideoFile() {
        if (TextUtils.equals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED)) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
            String videoFileName = "VIDEO_" + timeStamp + ".mp4";

            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

            if (!storageDir.exists()) {
                if (!storageDir.mkdirs()) {
                    Log.e("TAG", "Throwing Errors....");
                }
            }

            File video = new File(storageDir, videoFileName);
            return video;
        }
        return null;
    }


    public static String generateTempImgAbsPath(Context context) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imgFileName = "JPEG_" + timeStamp + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                Log.e("TAG", "Throwing Errors....");
            }
        }

        File image = new File(context.getExternalCacheDir(), imgFileName);
        String tempImgPath = image.getAbsolutePath();
        return tempImgPath;
    }


    public static boolean saveImgBitmap(String path, Bitmap bitmap) {
        File file = new File(path);
        try {
            if (!file.createNewFile()) {
                return false;
            }
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean deleteFileByPath(String filePath) {

        if (filePath == null) {
            return false;
        }

        File deleteFile = new File(filePath);


        if (deleteFile.exists()) {
            return deleteFile.delete();
        }

        return true;
    }

    /**
     * 获取存贮文件的文件夹路径
     *
     * @return
     */
    public static File createFolders() {
        File baseDir;
        if (android.os.Build.VERSION.SDK_INT < 8) {
            baseDir = Environment.getExternalStorageDirectory();
        } else {
            baseDir = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        }
        if (baseDir == null)
            return Environment.getExternalStorageDirectory();
        File aviaryFolder = new File(baseDir, FOLDER_NAME);
        if (aviaryFolder.exists())
            return aviaryFolder;
        if (aviaryFolder.isFile())
            aviaryFolder.delete();
        if (aviaryFolder.mkdirs())
            return aviaryFolder;
        return Environment.getExternalStorageDirectory();
    }

    public static File genEditFile(){
        return FileUtils.getEmptyFile("tietu"
                + System.currentTimeMillis() + ".png");
    }

    public static File getEmptyFile(String name) {
        File folder = FileUtils.createFolders();
        if (folder != null) {
            if (folder.exists()) {
                File file = new File(folder, name);
                return file;
            }
        }
        return null;
    }

    /**
     * 删除指定文件
     *
     * @param path
     * @return
     */
    public static boolean deleteFileNoThrow(String path) {
        File file;
        try {
            file = new File(path);
        } catch (NullPointerException e) {
            return false;
        }

        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 保存图片
     *
     * @param bitName
     * @param mBitmap
     */
    public static String saveBitmap(String bitName, Bitmap mBitmap) {
        File baseFolder = createFolders();
        File f = new File(baseFolder.getAbsolutePath(), bitName);
        FileOutputStream fOut = null;
        try {
            f.createNewFile();
            fOut = new FileOutputStream(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f.getAbsolutePath();
    }

    // 获取文件夹大小
    public static long getFolderSize(File file) throws Exception {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) { // 如果下面还有文件
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /** * 格式化单位 * * @param size * @return */
    public static String getFormatSize(double size) {
        double kiloByte = size / 1024d;
        int megaByte = (int) (kiloByte / 1024d);
        return megaByte + "MB";
    }

    /**
     *
     * @Description:
     * @Author 11120500
     * @Date 2013-4-25
     */
    public static boolean isConnect(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {

        }
        return false;
    }

}
