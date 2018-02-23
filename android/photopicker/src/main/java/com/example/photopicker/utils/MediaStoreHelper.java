package com.example.photopicker.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.example.photopicker.R;
import com.example.photopicker.entity.PhotoDirectory;

import java.util.ArrayList;
import java.util.List;

import static com.example.photopicker.PhotoPicker.EXTRA_SHOW_GIF;

/**
 * Created by xujian on 2018/2/5.
 */

public class MediaStoreHelper {

    public final static int INDEX_ALL_PHOTOS = 0;

    public static void getPhotoDirs(FragmentActivity activity, Bundle args, PhotosResultCallback resultCallback) {
        // 将PhotosResultCallback 一直暴露给最外层的函数调用方，让函数调用方来自己实现 PhotosResultCallback
        activity.getSupportLoaderManager().initLoader(0, args, new PhotoDirLoaderCallbacks(activity, resultCallback));
    }

    public interface PhotosResultCallback {
        void onResultCallback(List<PhotoDirectory> directories);
    }


    private static class PhotoDirLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        private Context context;
        private PhotosResultCallback resultCallback;

        public PhotoDirLoaderCallbacks(Context context, PhotosResultCallback resultCallback) {
            this.context = context;
            this.resultCallback = resultCallback;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // 这里的args是和loader对应的activity或者fragment绑定的，直接获取就可以，也有助于代码的分离
            return new PhotoDirectoryLoader(context, args.getBoolean(EXTRA_SHOW_GIF, false));
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data == null) return;

            List<PhotoDirectory> directories = new ArrayList<>();

            // 设置总图片的目录
            PhotoDirectory photoDirectoryAll = new PhotoDirectory();
            photoDirectoryAll.setName(context.getString(R.string.__picker_all_image));
            photoDirectoryAll.setId("ALL");


            while (data.moveToNext()) {
                int imageId = data.getInt(data.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                String bucketId = data.getString(data.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID));
                // 图片文件夹的名字
                String name = data.getString(data.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                // 图片文件的全路径信息
                String path = data.getString(data.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                long size = data.getLong(data.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));

                if (size < 1) {
                    continue;
                }

                // 构建图片文件夹信息
                PhotoDirectory photoDirectory = new PhotoDirectory();
                photoDirectory.setId(bucketId);
                photoDirectory.setName(name);

                if (!directories.contains(photoDirectory)) {
                    photoDirectory.setCoverPath(path); //  在这里设置时间最早的一张图片作为文件夹的封面
                    photoDirectory.addPhoto(imageId, path);
                    // 相当于用该文件夹下第一张图片的生成时间代表了这个文件夹的生成时间
                    photoDirectory.setDateAdded(data.getLong(data.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)));
                    directories.add(photoDirectory);
                } else {
                    // 重写 PhotoDirectory 的hash code用于 equals的判断，这里就体现了用场
                    directories.get(directories.indexOf(photoDirectory)).addPhoto(imageId, path);
                }

                // 全部图片文件夹下的显示内容
                photoDirectoryAll.addPhoto(imageId, path);
            }

            if (photoDirectoryAll.getPhotoPaths().size() > 0) {
                // 设置全部图片文件夹的封面图片信息
                photoDirectoryAll.setCoverPath(photoDirectoryAll.getPhotoPaths().get(0));
            }

            directories.add(INDEX_ALL_PHOTOS, photoDirectoryAll);

            // 执行自定义的接口信息
            if (resultCallback != null) {
                resultCallback.onResultCallback(directories);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }
}
