package com.photor.album.provider;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.photor.album.entity.Media;

import java.io.File;
import java.util.ArrayList;

public class StorageProvider {

    /**
     * 获得所有的照片文件信息
     * @param activity
     * @return
     */
    public static ArrayList<Media> getAllShownImages(Activity activity) {
        Uri uri;
        Cursor cursor;
        int column_index;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        ArrayList<Media> list = new ArrayList<Media>();
        String absolutePathOfImage;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA};

        cursor = activity.getContentResolver().query(uri, projection, null,
                null, null);

        // column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
        column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index);
            listOfAllImages.add(absolutePathOfImage);
        }
        for (String path : listOfAllImages) {
            list.add(new Media(new File(path)));
        }
        return list;
    }

}
