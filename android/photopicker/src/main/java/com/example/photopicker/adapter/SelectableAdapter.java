package com.example.photopicker.adapter;

import android.support.v7.widget.RecyclerView;

import com.example.photopicker.entity.Photo;
import com.example.photopicker.entity.PhotoDirectory;
import com.example.photopicker.event.Selectable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xujian on 2018/2/5.
 */

public abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements Selectable {

    private static final String TAG = SelectableAdapter.class.getSimpleName();

    protected List<PhotoDirectory> photoDirectories;
    protected List<String> selectedPhotos;

    // 当前被选择的图片文件夹
    protected int currentDirectoryIndex = 0;

    public SelectableAdapter() {
        photoDirectories = new ArrayList<>();
        selectedPhotos = new ArrayList<>();
    }

    @Override
    public boolean isSelected(Photo photo) {
        return getSelectedPhotos().contains(photo.getPath());
    }

    @Override
    public void toggleSelection(Photo photo) {
        // 根据点击设置当前图片文件的被选择状态
        if (selectedPhotos.contains(photo.getPath())) {
            selectedPhotos.remove(photo.getPath());
        } else {
            selectedPhotos.add(photo.getPath());
        }
    }

    @Override
    public void clearSelection() {
        selectedPhotos.clear();
    }

    @Override
    public int getSelectedItemCount() {
        return selectedPhotos.size();
    }

    public void setCurrentDirectoryIndex(int currentDirectoryIndex) {
        this.currentDirectoryIndex = currentDirectoryIndex;
    }

    public List<Photo> getCurrentPhotos() {
        // 获得当前文件夹下的照片信息
        return photoDirectories.get(currentDirectoryIndex).getPhotos();
    }

    // 获得当前文件夹下照片的路径信息
    public List<String> getCurrentPhotoPaths() {
        List<String> currentPhotoPaths = new ArrayList<>(getCurrentPhotos().size());
        for (Photo photo: getCurrentPhotos()) {
            currentPhotoPaths.add(photo.getPath());
        }
        return currentPhotoPaths;
    }

    public List<String> getSelectedPhotos() {
        return selectedPhotos;
    }
}
