package com.photor.album.entity;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.photor.album.entity.comparator.MediaComparators;
import com.photor.album.provider.MediaStoreProvider;
import com.photor.album.utils.PreferenceUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static com.photor.album.entity.FilterMode.ALL;

public class Album {

    private String name = null;  // 相册的名称
    private String path = null;  // 相册的路径名称
    private long id = -1;
    private int count = -1;  // 相册内文件的个数
    private int currentMediaIndex = 0;

    private boolean selected = false;
    public AlbumSettings settings = null;

    private ArrayList<Media> media;

    public ArrayList<Media> selectedMedias;
    private boolean isPreviewSelected;
    private boolean issecured=false;
    private String previewPath;
    private int selectedCount;

    private Album() {
        media = new ArrayList<Media>();
        selectedMedias = new ArrayList<Media>();
    }

    public Album(Context context, String path, long id, String name, int count) {
        this();
        this.path = path;
        this.name = name;
        this.count = count;
        this.id = id;
        settings = AlbumSettings.getSettings(context, this);
        setPreviewPath(getCoverPath());
    }

    public Album(Context context, File mediaPath) {
        super();
        File folder = mediaPath.getParentFile();
        this.path = folder.getPath();
        this.name = folder.getName();
        settings = AlbumSettings.getSettings(context, this);
        updatePhotos(context);
        setCurrentPhoto(mediaPath.getAbsolutePath());
    }

    /**
     * used for open an image from an unknown content storage
     *
     * @param context context
     * @param mediaUri uri of the media to display
     */
    public Album(Context context, Uri mediaUri) {
        super();
        media.add(0, new Media(context, mediaUri));
        setCurrentPhotoIndex(0);
    }

    public void setPreviewPath(String previewPath) {
        this.previewPath = previewPath;
    }

    public String getPreviewPath() {
        return previewPath;
    }

    public boolean isPreviewSelected() {
        return isPreviewSelected;
    }

    public void setsecured(boolean set){
        this.issecured=set;
    }

    public boolean getsecured(){
        return issecured;
    }

    public void setPreviewSelected(boolean previewSelected) {
        isPreviewSelected = previewSelected;
    }

    public ArrayList<Media> getSelectedMedia() {
        return selectedMedias;
    }

    public Media getSelectedMedia(int index) {
        return selectedMedias.get(index);
    }

    private boolean isFromMediaStore() {
        return id != -1;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getId() {
        return  this.id;
    }

    public ArrayList<String> getParentsFolders() {
        ArrayList<String> result = new ArrayList<String>();

        File f = new File(getPath());
        while(f != null && f.canRead()) {
            result.add(f.getPath());
            f = f.getParentFile();
        }
        return result;
    }

    public boolean isPinned(){ return settings.isPinned(); }

    public boolean hasCustomCover() {
        return settings.getCoverPath() != null;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public Media getMedia(int index) { return media.get(index); }

    public void setCurrentPhotoIndex(int index){ currentMediaIndex = index; }

    public void setCurrentPhotoIndex(Media m){ setCurrentPhotoIndex(media.indexOf(m)); }

    public Media getCurrentMedia() { return getMedia(currentMediaIndex); }

    public int getCurrentMediaIndex() { return currentMediaIndex; }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public String getCoverPath() {
        return settings.getCoverPath();
    }

    public boolean isHidden() {
        return new File(getPath(), ".nomedia").exists();
    }

    public boolean isWritable(){
        File file = new File(getPath());
        return file.canWrite();
    }

    public boolean isReadable(){
        File file = new File(getPath());
        return file.canRead();
    }

    public String lastmodified(){
        File file = new File(getPath());
        Date date = new Date(file.lastModified());
        return String.valueOf(date);
    }

    public String getParentPath(){
        File file = new File(getPath());
        return file.getParent();
    }

    public String size(){
        File file = new File(getPath());
        long size = 0;
        size = getFileFolderSize(file);
        double sizeMB = (double) size / 1024 / 1024;
        String s = " MB";
        if (sizeMB < 1) {
            sizeMB = (double) size / 1024;
            s = " KB";
        }
        sizeMB = (double) Math.round(sizeMB * 100) / 100;
        return String.valueOf(sizeMB) + s;
    }

    public static long getFileFolderSize(File dir) {
        long size = 0;
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    size += file.length();
                } else
                    size += getFileFolderSize(file);
            }
        } else if (dir.isFile()) {
            size += dir.length();
        }
        return size;
    }

    public Media getCoverAlbum() {
        if (hasCustomCover())
            return new Media(settings.getCoverPath());
        if (media.size() > 0)
            return media.get(0);
        return new Media();
    }

    /**
     * 获得按date 降序排列的 空 album信息，并且相册Album的Setting排序信息时默认的
     * @return
     */
    public static Album getEmptyAlbum() {
        Album album = new Album();
        album.settings = AlbumSettings.getDefaults();
        return album;
    }

    public ArrayList<Media> getMedia() {
        ArrayList<Media> mediaArrayList = new ArrayList<Media>();
        switch (getFilterMode()) {
            case ALL:
                mediaArrayList = media;
                break;
            case GIF:
                for (Media media1 : media)
                    if (media1.isGif()) mediaArrayList.add(media1);
                break;
            case IMAGES:
                for (Media media1 : media)
                    if (media1.isImage()) mediaArrayList.add(media1);
                break;
            default:
                break;
        }
        return mediaArrayList;
    }


    public boolean addMedia(@Nullable Media media) {
        if(media == null) return false;
        this.media.add(media);
        return true;
    }

    public void removeCoverAlbum(Context context) {
        settings.changeCoverPath(context, null);
        setPreviewSelected(false);
        setPreviewPath(null);
    }

    public void setSelectedPhotoAsPreview(Context context) {
        if (selectedMedias.size() > 0)
            settings.changeCoverPath(context, selectedMedias.get(0).getPath());
        setPreviewPath(getCoverPath());
    }

    private void setCurrentPhoto(String path) {
        for (int i = 0; i < media.size(); i++)
            if (media.get(i).getPath().equals(path)) currentMediaIndex = i;
    }

    public int getSelectedCount() {
        if(selectedMedias!=null){
            selectedCount = selectedMedias.size();
        }
        return selectedCount;
    }

    public boolean areMediaSelected() { return getSelectedCount() != 0;}

    public void selectAllPhotos() {
        for (int i = 0; i < media.size(); i++) {
            if (!media.get(i).isSelected()) {
                media.get(i).setSelected(true);
                selectedMedias.add(media.get(i));
            }
        }
    }

    private int toggleSelectPhoto(int index) {
        if (media.get(index) != null) {
            media.get(index).setSelected(!media.get(index).isSelected());
            if (media.get(index).isSelected()) {
                selectedMedias.add(media.get(index));
                if(getPreviewPath() != null && getPreviewPath().equals(media.get(index).getPath()))
                    setPreviewSelected(true);
            }
            else {
                selectedMedias.remove(media.get(index));
                if(getPreviewPath() != null && getPreviewPath().equals(media.get(index).getPath()))
                    setPreviewSelected(false);
            }
        }
        return index;
    }

    public int toggleSelectPhoto(Media m) {
        return toggleSelectPhoto(media.indexOf(m));
    }

    public void setDefaultSortingMode(Context context, SortingMode column) {
        settings.changeSortingMode(context, column);
    }


    public void updatePhotos(Context context) {
        media = getMedia(context);
        sortPhotos();
        setCount(media.size());
    }

    private ArrayList<Media> getMedia(Context context) {
        PreferenceUtil SP = PreferenceUtil.getInstance(context);
        ArrayList<Media> mediaArrayList = new ArrayList<Media>();

        if (isFromMediaStore()) {
            // 流程：咋子相册界面先加载每个图片文件夹下最新的图片信息
            // 之后查看每一个文件夹内的具体文件信息的时候，再将文件夹下的文件信息加载完全
            mediaArrayList.addAll(
                    MediaStoreProvider.getMedias(
                            context, id));
        } else {
//            mediaArrayList.addAll(StorageProvider.getMedia(
//                    getPath(), SP.getBoolean("set_include_video", false)));
        }
        return mediaArrayList;
    }

    public void sortPhotos() {
        Collections.sort(media, MediaComparators.getComparator(settings.getSortingMode(), settings.getSortingOrder()));
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Album) {
            return getPath().equals(((Album) obj).getPath());
        }
        return super.equals(obj);
    }

    private boolean found_id_album = false;


    public void scanFile(Context context, String[] path) { MediaScannerConnection.scanFile(context, path, null, null); }

    public void scanFile(Context context, String[] path, MediaScannerConnection.OnScanCompletedListener onScanCompletedListener) {
        MediaScannerConnection.scanFile(context, path, null, onScanCompletedListener);
    }

    public FilterMode getFilterMode() {
        return settings != null ? settings.getFilterMode() : ALL;
    }

}
