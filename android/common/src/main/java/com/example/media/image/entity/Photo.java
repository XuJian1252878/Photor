package com.example.media.image.entity;

/**
 * Created by xujian on 2018/2/5.
 */

public class Photo {

    private int id;
    private String path;


    public Photo(int id, String path) {
        this.id = id;
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Photo)) {
            return false;
        }

        Photo photo  = (Photo) obj;
        return id == photo.id;
    }
}
