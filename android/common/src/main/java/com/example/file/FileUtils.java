package com.example.file;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.util.Log;

import com.example.common.R;
import com.example.preference.PreferenceUtil;
import com.example.strings.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;


/**
 * Created by xujian on 2018/2/5.
 */

public class FileUtils {

    private static final String TAG = "FileUtils";

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

    public static boolean saveFileByByte(String path, byte[] data) {
        // Bitmaps do not include metadata, so if you use Bitmap you will lose it.
        // You can save to file the byte[] array directly
        try {
            OutputStream os = new FileOutputStream(path);
            InputStream is = new ByteArrayInputStream(data);

            byte[] buff = new byte[1024];
            int len = 0;

            while ((len = is.read(buff)) != -1) {
                os.write(buff, 0, len);
            }
            is.close();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
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

    /**
     * 删除一个文件夹下的所有文件信息
     * @param context
     * @param folder
     * @return
     */
    public static boolean deleteFilesInFolder(Context context, @NonNull final File folder) {
        boolean totalSuccess = true;
        String[] children = folder.list();
        if (children != null) {
            for (String child: children) {
                File file = new File(folder, child);
                if (!file.isDirectory()) {
                    boolean success = deleteFile(context, file);
                    if (!success) {
                        Log.w(TAG, "Failed to delete file" + child);
                        totalSuccess = false;
                    }
                }
            }
        }
        return totalSuccess;
    }

    /**
     * 删除指定的文件
     * @param context
     * @param file
     * @return
     */
    public static boolean deleteFile(Context context, @NonNull final File file) {
        // First try the normal deletion.

        boolean success = file.delete();

        // Try with Storage Access Framework.
        if (!success && Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            DocumentFile document = getDocumentFile(context, file, false, false);
            success = document != null && document.delete();
        }

        // Try the Kitkat workaround.
        if (!success && Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            ContentResolver resolver = context.getContentResolver();

            try {
                Uri uri = null;//MediaStoreUtil.getUriFromFile(file.getAbsolutePath());
                if (uri != null) {
                    resolver.delete(uri, null, null);
                }
                success = !file.exists();
            }
            catch (Exception e) {
                Log.e(TAG, "Error when deleting file " + file.getAbsolutePath(), e);
                return false;
            }
        }

        if (success) {
//            scanFile(context, new String[]{ file.getPath() });
            updateMediaStoreAfterDelete(context, file);
        }
        return success;
    }

    /**
     * MediaScannerConnection 作用是为应用提供一个媒体扫描服务，
     * 当有新创建或者下载的文件时，会从该文件读取元数据并将该文件添加到媒体中去。
     * 当我们添加一个文件的时候，我们需要刷新媒体库才能立即找得到添加文件，使用MediaScannerConnection 去刷新媒体库
     * @param context
     * @param paths
     */
    private static void scanFile(Context context, String[] paths) {
        MediaScannerConnection.scanFile(context, paths, null, null);
    }

    /**
     * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5). If the file is not
     * existing, it is created.
     *
     * @param file              The file.
     * @param isDirectory       flag indicating if the file should be a directory.
     * @param createDirectories flag indicating if intermediate path directories should be created if not existing.
     * @return The DocumentFile
     */
    private static DocumentFile getDocumentFile(Context context, @NonNull final File file, final boolean isDirectory, final boolean createDirectories) {

        Uri treeUri = getTreeUri(context);

        if (treeUri == null) return null;

        DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);
        String sdcardPath = getSavedSdcardPath(context);
        String suffixPathPart = null;

        if (sdcardPath != null) {
            if((file.getPath().indexOf(sdcardPath)) != -1)
                suffixPathPart = file.getAbsolutePath().substring(sdcardPath.length());
        } else {
            HashSet<File> storageRoots = getStorageRoots(context);
            for(File root : storageRoots) {
                if (root != null) {
                    if ((file.getPath().indexOf(root.getPath())) != -1)
                        suffixPathPart = file.getAbsolutePath().substring(file.getPath().length());
                }
            }
        }

        if (suffixPathPart == null) {
            Log.d(TAG, "unable to find the document file, filePath:"+ file.getPath()+ " root: " + ""+sdcardPath);
            return null;
        }

        if (suffixPathPart.startsWith(File.separator)) suffixPathPart = suffixPathPart.substring(1);

        String[] parts = suffixPathPart.split("/");

        for (int i = 0; i < parts.length; i++) { // 3 is the

            DocumentFile tmp = document.findFile(parts[i]);
            if (tmp != null)
                document = document.findFile(parts[i]);
            else {
                if (i < parts.length - 1) {
                    if (createDirectories) document = document.createDirectory(parts[i]);
                    else return null;
                }
                else if (isDirectory) document = document.createDirectory(parts[i]);
                else return document.createFile("image", parts[i]);
            }
        }

        return document;
    }

    /**
     * Get the stored tree URIs.
     *
     * @return The tree URIs.
     * @param context context
     */
    private static Uri getTreeUri(Context context) {
        String uriString = PreferenceUtil.getInstance(context).getString(context.getString(R.string.preference_internal_uri_extsdcard_photos), null);

        if (uriString == null) return null;
        return Uri.parse(uriString);
    }


    private static String getSavedSdcardPath(Context context) {
        return PreferenceUtil.getInstance(context).getString("sd_card_path", null);
    }

    /**
     * 返回应用缓存目录的根目录信息
     * /storage/emulated/0/Android/data/应用包名/files
     * @param context
     * @return
     */
    public static HashSet<File> getStorageRoots(Context context) {
        HashSet<File> paths = new HashSet<File>();
        for (File file : ContextCompat.getExternalFilesDirs(context, "external")) {
            if (file != null) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) Log.w("asd", "Unexpected external file dir: " + file.getAbsolutePath());
                else
                    paths.add(new File(file.getAbsolutePath().substring(0, index)));
            }
        }
        return paths;
    }


    /**
     * 将source文件移动至 targetDir目录下
     * @param context
     * @param source
     * @param targetDir
     * @return
     */
    public static boolean moveFile(Context context, @NonNull final File source, @NonNull final File targetDir) {
        // 首先尝试普通的重命名操作
        File target = new File(targetDir, source.getName());
        boolean success = source.renameTo(target); // 重命名操作
        if (!success) {
            success = copyFile(context, source, targetDir);
            if (success) {
//                success = deleteFile(context, source);
                success = updateMediaStoreAfterDelete(context, source) && updateMediaStoreAfterCreate(context, target);
            }
        } else {
            success = updateMediaStoreAfterDelete(context, source) && updateMediaStoreAfterCreate(context, target);
        }

//        boolean success = copyFile(context, source, targetDir);
//        if (success) {
//            success = updateMediaStoreAfterDelete(context, source) && updateMediaStoreAfterCreate(context, target);
//        }

        return success;
    }

    /**
     * 创建新文件的时候，增加新文件值媒体库（新文件的创建操作并不在这个函数内）
     * @param context
     * @param createFile
     * @return
     */
    public static boolean updateMediaStoreAfterCreate(Context context, File createFile) {
        return updateMediaStore(context, createFile, null);
    }

    /**
     * 在进行删除文件的操作时，同时更新MediaStore（删除文件的操作在这个函数内）
     * @param context
     * @param deleteFile
     * @return
     */
    public static boolean updateMediaStoreAfterDelete(Context context, File deleteFile) {
//        if (!deleteFile.exists()) {
//            return false;
//        }

        try {
//            context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    MediaStore.Images.Media.DATA + " = ? ",
//                    new String[] { deleteFile.getAbsolutePath()});
            return updateMediaStore(context, deleteFile, null);
        } catch (Exception e) {
            Log.d("MediaStoreAfterDelete", e.getMessage());
            return false;
        }
    }

    /**
     * 更新MediaStore数据库
     * @param context
     * @param file
     * @return
     */
    public static boolean updateMediaStore(final Context context, final File file, MediaScannerConnection.OnScanCompletedListener onScanCompletedListener) {
        try {
            //版本号的判断  4.4为分水岭，发送广播更新媒体库
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                MediaScannerConnection.scanFile(context,
                        new String[]{file.getAbsolutePath()},
                        null,  // 根绝文件后缀名称决定mimetype
                        onScanCompletedListener);
//                        new MediaScannerConnection.OnScanCompletedListener() {
//                            @Override
//                            public void onScanCompleted(String s, Uri uri) {
////                                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
////                                intent.setData(uri);
////                                context.sendBroadcast(intent);
////                                Log.d("updateMediaStore1", s);
////                                Log.d("updateMediaStore2", Uri.fromFile(file).toString());
////                                Log.d("updateMediaStore3", uri.toString());
////                                Log.d("updateMediaStore4", Environment.getExternalStorageDirectory().getAbsolutePath());
//                            }
//                        });
            } else {
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(file)));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("updateMediaStore", e.getMessage());
            return false;
        }
    }


    public static boolean copyFile(Context context, @NonNull final File source, @NonNull final File targetDir) {
        InputStream inStream = null;
        OutputStream outStream = null;

        boolean success = false;
        File target = getTargetFile(source, targetDir);

        try {
            inStream = new FileInputStream(source);

            // First try the normal way
            if (isWritable(target)) {
                // standard way
                FileChannel inChannel = new FileInputStream(source).getChannel();
                FileChannel outChannel = new FileOutputStream(target).getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
                success = true;
                try { inChannel.close(); } catch (Exception ignored) { }
                try { outChannel.close(); } catch (Exception ignored) { }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //inStream = context.getContentResolver().openInputStream(Uri.fromFile(source));
                    //outStream = context.getContentResolver().openOutputStream(Uri.fromFile(target));
                    if (isFileOnSdCard(context, source)) {
                        DocumentFile sourceDocument = getDocumentFile(context, source, false, false);
                        if (sourceDocument != null) {
                            inStream = context.getContentResolver().openInputStream(sourceDocument.getUri());
                        }
                    }
                    // Storage Access Framework
                    DocumentFile targetDocument = getDocumentFile(context, target, false, false);
                    if (targetDocument != null) {
                        outStream = context.getContentResolver().openOutputStream(targetDocument.getUri());
                    }
                }
                else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                    // TODO: 13/08/16 test this
                    // Workaround for Kitkat ext SD card
                    Uri uri = getUriFromFile(context,target.getAbsolutePath());
                    if (uri != null) {
                        outStream = context.getContentResolver().openOutputStream(uri);
                    }
                }

                if (outStream != null) {
                    // Both for SAF and for Kitkat, write to output stream.
                    byte[] buffer = new byte[4096]; // MAGIC_NUMBER
                    int bytesRead;
                    while ((bytesRead = inStream.read(buffer)) != -1) outStream.write(buffer, 0, bytesRead);
                    success = true;
                }

            }
        } catch (Exception e) {
            Log.e(TAG, "Error when copying file from " + source.getAbsolutePath() + " to " + target.getAbsolutePath(), e);
            return false;
        }
        finally {
            try { inStream.close(); } catch (Exception ignored) { }
            try { outStream.close(); } catch (Exception ignored) { }
        }

        if (success) scanFile(context, new String[] { target.getPath() });
        return success;
    }

    /**
     * 生成 targetDir + source.getName()_date.xxx 的文件
     * @param source
     * @param targetDir
     * @return
     */
    private static File getTargetFile(File source, File targetDir) {
        File file = new File(targetDir, source.getName());
        if (!source.getParentFile().equals(targetDir) && !file.exists())
            return file;
        return new File(targetDir, StringUtils.incrementFileNameSuffix(source.getName()));
    }

    /**
     * Check is a file is writable. Detects write issues on external SD card.
     *
     * @param file The file
     * @return true if the file is writable.
     */
    private static boolean isWritable(@NonNull final File file) {
        boolean isExisting = file.exists();

        try {
            FileOutputStream output = new FileOutputStream(file, true);
            try {
                output.close();
            }
            catch (IOException e) {
                // do nothing.
            }
        }
        catch (java.io.FileNotFoundException e) {
            return false;
        }
        boolean result = file.canWrite();

        // Ensure that file is not created during this process.
        if (!isExisting) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }

        return result;
    }


    /**
     * 判断file文件时候在SD卡上
     * @param context
     * @param file
     * @return
     */
    public static boolean isFileOnSdCard(Context context, File file) {
        String sdcardPath = getSdcardPath(context);
        if (sdcardPath != null)
            return file.getPath().startsWith(sdcardPath);

        return false;
    }

    /**
     * 判断当前手机有无sd卡信息，有的话，返回SD卡的根路径信息
     * http://blog.desmondyao.com/android-storage/
     * @param context
     * @return
     */
    public static String getSdcardPath(Context context) {
        for(File file : context.getExternalFilesDirs("external")) {
            if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) Log.w("asd", "Unexpected external file dir: " + file.getAbsolutePath());
                else
                    return new File(file.getAbsolutePath().substring(0, index)).getPath();
            }
        }
        return null;
    }


    /**
     * Get an Uri from an file path.
     *
     * @param path The file path.
     * @return The Uri.
     */
    private static Uri getUriFromFile(Context context, final String path) {
        ContentResolver resolver = context.getContentResolver();

        Cursor filecursor = resolver.query(MediaStore.Files.getContentUri("external"),
                new String[] {BaseColumns._ID}, MediaStore.MediaColumns.DATA + " = ?",
                new String[] {path}, MediaStore.MediaColumns.DATE_ADDED + " desc");
        if (filecursor == null) {
            return null;
        }
        filecursor.moveToFirst();

        // cursor.isAfterLast() method returns true if you've read all position in your cursor,
        if (filecursor.isAfterLast()) {
            filecursor.close();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, path);
            return resolver.insert(MediaStore.Files.getContentUri("external"), values);
        }
        else {
            int imageId = filecursor.getInt(filecursor.getColumnIndex(BaseColumns._ID));
            Uri uri = MediaStore.Files.getContentUri("external").buildUpon().appendPath(
                    Integer.toString(imageId)).build();
            filecursor.close();
            return uri;
        }
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static String getMediaPath(final Context context, final Uri uri)
    {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        else if ("downloads".equals(uri.getAuthority())) { //download for chrome-dev workaround
            String[] seg = uri.toString().split("/");
            final String id = seg[seg.length - 1];
            final Uri contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

            return getDataColumn(context, contentUri, null, null);
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            try {
                return getDataColumn(context, uri, null, null);
            } catch (Exception ignored){ }

        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

}
