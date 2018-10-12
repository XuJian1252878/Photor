package com.photor.album.entity;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.strings.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class Media implements Parcelable, Serializable {

    private String path = null;
    private long dateModified = -1;
    private String mimeType = null;
    private int orientation = 0;

    private String uri = null;

    private long size = 0;
    private boolean selected = false;

    public Media() { }

    public Media(String path, long dateModified) {
        this.path = path;
        this.dateModified = dateModified;
        this.mimeType = StringUtils.getMimeType(path);
    }

    public Media(File file) {
        this(file.getPath(), file.lastModified());
        this.size = file.length();
        this.mimeType = StringUtils.getMimeType(path);
    }

    public Media(String path) {
        this(path, -1);
    }

    public Media(Context context, Uri mediaUri) {
        this.uri = mediaUri.toString();
        // https://stackoverflow.com/questions/8885204/how-to-get-the-file-path-from-uri
        this.path = new File(mediaUri.getPath()).getAbsolutePath();
        this.mimeType = context.getContentResolver().getType(getUri());
    }

    public Media(Cursor cur) {
        this.path = cur.getString(0);
        this.dateModified = cur.getLong(1);
        this.mimeType = cur.getString(2);
        this.size = cur.getLong(3);
        this.orientation = cur.getInt(4);
    }

    private static int findOrientation(int exifOrientation){
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90: return 90;
            case ExifInterface.ORIENTATION_ROTATE_180: return 180;
            case ExifInterface.ORIENTATION_ROTATE_270: return 270;
        }
        return 0;
    }

    public void setUri(String uriString) {
        this.uri = uriString;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isGif() { return mimeType.endsWith("gif"); }

    public boolean isImage() { return mimeType.startsWith("image"); }

    public boolean isVideo() { return mimeType.startsWith("video"); }

    public Uri getUri() {
        return uri != null ? Uri.parse(uri) : Uri.fromFile(new File(path));
    }

    public String getName() {
        return StringUtils.getPhotoNameByPath(path);
    }

    public long getSize() {
        return size;
    }

    public String getPath() {
        return path;
    }

    public Long getDateModified() {
        return dateModified;
    }


    public Bitmap getBitmap(){
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(path,bmOptions);
        bitmap = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth(),bitmap.getHeight(),true);
        return bitmap;
    }
    
    public boolean setOrientation(final int orientation) {
        this.orientation = orientation;
        // TODO: 28/08/16  find a better way
        new Thread(new Runnable() {
            public void run() {
                int exifOrientation = -1;
                try {
                    ExifInterface  exif = new ExifInterface(path);
                    switch (orientation) {
                        case 90: exifOrientation = ExifInterface.ORIENTATION_ROTATE_90; break;
                        case 180: exifOrientation = ExifInterface.ORIENTATION_ROTATE_180; break;
                        case 270: exifOrientation = ExifInterface.ORIENTATION_ROTATE_270; break;
                        case 0: exifOrientation = ExifInterface.ORIENTATION_NORMAL; break;
                    }
                    if (exifOrientation != -1) {
                        exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(exifOrientation));
                        exif.saveAttributes();
                    }
                }
                catch (IOException ignored) {  }
            }
        }).start();
        return true;
    }

    public File getFile() {
        if (path != null) return new File(path);
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.path);
        dest.writeLong(this.dateModified);
        dest.writeString(this.mimeType);
        dest.writeLong(this.size);
        dest.writeByte(this.selected ? (byte) 1 : (byte) 0);
    }

    protected Media(Parcel in) {
        this.path = in.readString();
        this.dateModified = in.readLong();
        this.mimeType = in.readString();
        this.size = in.readLong();
        this.selected = in.readByte() != 0;
    }

    public static final Creator<Media> CREATOR = new Creator<Media>() {
        @Override
        public Media createFromParcel(Parcel source) {
            return new Media(source);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }
    };

    public int getOrientation() {
        return orientation;
    }
}
