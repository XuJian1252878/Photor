package com.photor.album.entity;

import android.content.Context;

import com.photor.R;
import com.photor.album.entity.comparator.AlbumsComparators;
import com.photor.album.provider.MediaStoreProvider;
import com.photor.album.utils.PreferenceUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class HandlingAlbums {

    public final static String TAG = "HandlingAlbums";
    private static String backupFile = "albums.dat";

    public ArrayList<Album> dispAlbums;
    private ArrayList<Album> selectedAlbums;

    private PreferenceUtil SP;

    private int current = 0;  // 当前被选中的相册信息
    private boolean hidden;

    public HandlingAlbums(Context context) {
        SP = PreferenceUtil.getInstance(context);
        dispAlbums = new ArrayList<Album>();
        selectedAlbums = new ArrayList<Album>();
    }

    public void loadAlbums(Context context, boolean hidden) {
        this.hidden = hidden;

        ArrayList<Album> list = new ArrayList<Album>();
        if (SP.getBoolean(context.getString(R.string.preference_use_alternative_provider), false)) {
//            StorageProvider p = new StorageProvider(context);
//            list = p.getAlbums(context, hidden);
        } else {
            // 获得初步的 图片文件夹信息（album），并没有加载全部的图片信息
            list.addAll(MediaStoreProvider.getAlbums(context, hidden));
        }
        dispAlbums = list;
        sortAlbums();
    }

    public void addAlbum(int position, Album album) {
        dispAlbums.add(position, album);
        setCurrentAlbum(album);

    }

    public void setCurrentAlbum(Album album) {
        current = dispAlbums.indexOf(album);
    }

    public void sortAlbums() {
        Collections.sort(dispAlbums, AlbumsComparators.getComparator(getSortingMode(), getSortingOrder()));
    }

    public SortingMode getSortingMode() {
        return SortingMode.fromValue(SP.getInt("albums_sorting_mode", SortingMode.DATE.getValue()));
    }

    public SortingOrder getSortingOrder() {
        return SortingOrder.fromValue(SP.getInt("albums_sorting_order", SortingOrder.DESCENDING.getValue()));
    }

    public void setDefaultSortingAscending(SortingOrder sortingOrder) {
        SP.putInt("albums_sorting_order", sortingOrder.getValue());
    }

    public void setDefaultSortingMode(SortingMode sortingMode) {
        SP.putInt("albums_sorting_mode", sortingMode.getValue());
    }

    public Album getCurrentAlbum() {
        return dispAlbums.get(current);
    }
    
    public void clearSelectedAlbums() {
        for (Album dispAlbum : dispAlbums)
            dispAlbum.setSelected(false);

        selectedAlbums.clear();
    }

    /**
     * 从缓存文件中读取手机相册的信息，相册界面刚打开的时候就从之前本地缓存的数据读取相册信息
     * @param context
     */
    public void restoreBackup(Context context) {
        FileInputStream inStream;
        try {
            File f = new File(context.getCacheDir(), backupFile);
            inStream = new FileInputStream(f);
            ObjectInputStream objectInputStream = new ObjectInputStream(inStream);
            // 从缓存文件中获得 dispAlbums
            dispAlbums = (ArrayList<Album>) objectInputStream.readObject();
            objectInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将相册信息写入本地文件
     * @param context
     */
    public void saveBackup(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream outStream;
                try {
                    File f = new File(context.getCacheDir(), backupFile);
                    outStream = new FileOutputStream(f);
                    ObjectOutputStream objectOutStream = new ObjectOutputStream(outStream);
                    objectOutStream.writeObject(dispAlbums);
                    objectOutStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
