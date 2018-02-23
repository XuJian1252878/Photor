package com.example.photopicker.utils;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;

/**
 * Created by xujian on 2018/2/6.
 */

/**
 * 设置图片的查询方式，可以通过contentprovider 从磁盘、数据库、ContentProvider、网络或者另一个进程
 * 加载数据，完全自定义
 */
public class PhotoDirectoryLoader extends CursorLoader {

    final String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA, // 图片的存储路径
            MediaStore.Images.Media.BUCKET_ID, // 存储图片文件夹的id
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME, // 存储图片文件夹的名字
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE
    };

    public PhotoDirectoryLoader(Context context, boolean showGif) {
        super(context);

        setProjection(IMAGE_PROJECTION); // 设置要选出哪些数据条目
        setUri(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        setSortOrder(MediaStore.Images.Media.DATE_ADDED + " DESC");

        setSelection(MediaStore.Images.Media.MIME_TYPE + " = ? or " +
                MediaStore.Images.Media.MIME_TYPE  + " = ? or " +
                MediaStore.Images.Media.MIME_TYPE  + " = ? " +
                (showGif ? (" or " + MediaStore.Images.Media.MIME_TYPE + " = ? ") : ""));

        String[] selectionArgs;
        if (showGif) {
            selectionArgs = new String[] {"image/jpeg", "image/png", "image/jpg","image/gif" };
        } else {
            selectionArgs = new String[] { "image/jpeg", "image/png", "image/jpg" };
        }
        setSelectionArgs(selectionArgs);
    }

    public PhotoDirectoryLoader(Context context, Uri uri, String[] projection, String selection,
                                String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
    }
}
