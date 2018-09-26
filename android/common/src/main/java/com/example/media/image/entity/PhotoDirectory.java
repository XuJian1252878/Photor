package com.example.media.image.entity;

import android.text.TextUtils;

import com.example.file.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xujian on 2018/2/5.
 */

public class PhotoDirectory {

    private String id;
    private String coverPath;
    private String name;
    private long dateAdded;
    private List<Photo> photos = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        if (photos == null) {
            return;
        }

        for (int i = 0, j = 0, num = photos.size(); i < num; i++) {
            Photo p = photos.get(j);
            if (p == null || !FileUtils.fileIsExists(p.getPath())) {
                photos.remove(j);
            } else {
                j ++;  // 使用j计数，防止因为remove操作而产生的下标差值。
            }
        }

        this.photos = photos;
    }

    @Override
    public int hashCode() {
        // 自定义hashCode生成策略
        if (TextUtils.isEmpty(id)) {
            if (TextUtils.isEmpty(name)) {
                return 0;
            }

            return name.hashCode();
        }

        int result = id.hashCode();
        if (TextUtils.isEmpty(name)) {
            return result;
        }

        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PhotoDirectory)) {
            return false;
        }

        PhotoDirectory directory = (PhotoDirectory) obj;

        boolean hasId = !TextUtils.isEmpty(id);
        boolean otherHasId = !TextUtils.isEmpty(directory.id);

        if (hasId && otherHasId) {  // 如果两个都为真的条件的判断方法
            if (!TextUtils.equals(id, directory.id)) {
                return false;
            }

            return TextUtils.equals(name, directory.name);
        }

        return false;
    }

    public List<String> getPhotoPaths() {
        List<String> paths = new ArrayList<>(photos.size());
        for (Photo photo: photos) {
            paths.add(photo.getPath());
        }
        return paths;
    }

    public void addPhoto(int id, String path) {
        if (FileUtils.fileIsExists(path)) {
            photos.add(new Photo(id, path));
        }
    }
}
