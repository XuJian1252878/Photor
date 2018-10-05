package com.photor.album.provider;

import android.content.Context;

import com.photor.album.entity.Album;
import com.photor.album.entity.Media;
import com.photor.album.utils.StringUtils;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;

public class MediaStoreProvider {

    public static ArrayList<Album> getAlbums(Context context, boolean hidden) {
        return getAlbums(context);
    }

    // 获得相册列表信息（每一个相册当前只包含一个作为封皮的相册文件）
    public static ArrayList<Album> getAlbums(Context context) {

        ArrayList<Album> list = new ArrayList<>();

        String[] projection = new String[] {
                MediaStore.Files.FileColumns.PARENT,  // 获得图片文件的父路径id
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME // 图片文件所在文件夹的名称
        };

        // 首先获得 拥有照片 的文件夹信息
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=? ) GROUP BY ( " + MediaStore.Files.FileColumns.PARENT + " ";
        String[] selectionArgs = new String[] {
                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
        };

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.PARENT);
                int nameColumn = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);
                // 循环获取 文件夹下的照片信息
                do {
                    // 获取该文件夹下最新的一张照片信息
                    Media media = getLastMedia(context, cursor.getLong(idColumn));
                    if (media != null && media.getPath() != null) {
                        String path = StringUtils.getBucketPathByImagePath(media.getPath());

                        Album album = new Album(
                                context,
                                path,
                                cursor.getLong(idColumn),
                                cursor.getString(nameColumn),
                                getMediaCountByParent(context, idColumn));

                        if (album.addMedia(media)) {
                            list.add(album);
                        }
                    }
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    private static int getMediaCountByParent(Context context, long albumId) {
        int c = 0;

        String[] projection = new String[] {
                MediaStore.Files.FileColumns.PARENT
        };

        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " = ? and "
                + MediaStore.Files.FileColumns.PARENT + " = ? ";
        String[] selectionArgs = new String[] {
                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                String.valueOf(albumId)
        };

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                null
        );

        if (cursor != null) {
            c = cursor.getCount();
            cursor.close();
        }
        return c;
    }

    private static Media getLastMedia(Context context, long albumId) {
        //  获取该文件夹下最新拍的一张照片
        ArrayList<Media> list = getMediasByParent(context, albumId, 1);
        return list.size() > 0 ? list.get(0) : null;
    }

    public static ArrayList<Media> getMedias(Context context, long albumId) {
        // 根据文件夹来获取图片信息（按照时间逆序排序）
        return getMediasByParent(context, albumId, -1);
    }

    private static ArrayList<Media> getMediasByParent(Context context, long albumId, int n) {
        String limit = n == -1 ? "" : " LIMIT " + n;
        ArrayList<Media> list = new ArrayList<>();

        String[] projection = new String[] {
                MediaStore.Images.Media.DATA,  // 图片路径信息
                MediaStore.Images.Media.DATE_TAKEN,  // 拍摄日期
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.ORIENTATION
        };

        Uri images = MediaStore.Files.getContentUri("external");

        // 获取某个特定文件夹下的图片信息
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " = ? and " + MediaStore.Files.FileColumns.PARENT + " = ? ";
        String[] selectionArgs = new String[] {
                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                String.valueOf(albumId)
        };

        // 将选出的结果按照时间的降序排序
        Cursor cursor = context.getContentResolver().query(
                images,
                projection,
                selection,
                selectionArgs,
                " " + MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC " + limit
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(new Media(cursor));
                } while (cursor.moveToNext());
                cursor.close();
            }
        }

        return list;
    }

}
