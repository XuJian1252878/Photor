package com.photor.album.entity;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.file.FileUtils;
import com.example.strings.StringUtils;
import com.photor.R;
import com.photor.album.adapter.MediaAdapter;
import com.photor.album.entity.comparator.MediaComparators;
import com.photor.album.provider.MediaStoreProvider;
import com.example.preference.PreferenceUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.TreeMap;

import static com.photor.album.entity.FilterMode.ALL;

public class Album {

    private String name = null;  // 相册的名称
    private String path = null;  // 相册的路径名称
    private long id = -1;
    private int count = -1;  // 相册内文件的个数
    private int currentMediaIndex = 0;

    private boolean selected = false;
    public AlbumSettings settings = null;

    private ArrayList<Media> medias;

    public ArrayList<Media> selectedMedias;
    private boolean isPreviewSelected;
    private boolean issecured=false;
    private String previewPath;
    private int selectedCount;

    private Album() {
        medias = new ArrayList<Media>();
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

    public Album(Context context, @NonNull File mediaPath) {
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
     * @param mediaUri uri of the medias to display
     */
    public Album(Context context, Uri mediaUri) {
        super();
        if (medias == null) {
            synchronized (Album.class) {
                if (medias == null) {
                    medias = new ArrayList<>();
                    medias.add(0, new Media(context, mediaUri));
                    String imgPath = mediaUri.getPath();
                    path = StringUtils.getBucketPathByImagePath(imgPath);
                    setCurrentPhotoIndex(0);
                }
            }
        }
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

    public Media getAllMediaUnderAlbum(int index) { return medias.get(index); }

    public void setCurrentPhotoIndex(int index){ currentMediaIndex = index; }

    public void setCurrentPhotoIndex(Media m){ setCurrentPhotoIndex(medias.indexOf(m)); }

    public Media getCurrentMedia() { return getAllMediaUnderAlbum(currentMediaIndex); }

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

    /**
     * 如果Album中已经设置了封面信息，那么直接去封面路径
     * 没有的话，取Album中的第一个Media作为封面路径（最近拍摄的一张照片）
     * @return
     */
    public Media getCoverAlbum() {
        if (hasCustomCover())
            return new Media(settings.getCoverPath());
        if (medias.size() > 0)
            return medias.get(0);
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

    public ArrayList<Media> getMedias() {
        ArrayList<Media> mediaArrayList = new ArrayList<Media>();
        switch (getFilterMode()) {
            case ALL:
                mediaArrayList = medias;
                break;
            case GIF:
                for (Media media1 : medias)
                    if (media1.isGif()) mediaArrayList.add(media1);
                break;
            case IMAGES:
                for (Media media1 : medias)
                    if (media1.isImage()) mediaArrayList.add(media1);
                break;
            default:
                break;
        }
        return mediaArrayList;
    }


    public boolean addMedia(@Nullable Media media) {
        if(media == null) return false;
        this.medias.add(media);
        return true;
    }

    public void removeCoverAlbum(Context context) {
        settings.changeCoverPath(context, null);
        setPreviewSelected(false);
        setPreviewPath(null);
    }

    /**
     * 将更新的相册封面信息写入sqllite数据库中
     * @param context
     */
    public void setSelectedPhotoAsPreview(Context context) {
        if (selectedMedias.size() > 0)
            settings.changeCoverPath(context, selectedMedias.get(0).getPath());
        setPreviewPath(getCoverPath());
    }

    private void setCurrentPhoto(String path) {
        for (int i = 0; i < medias.size(); i++)
            if (medias.get(i).getPath().equals(path)) currentMediaIndex = i;
    }

    public int getSelectedCount() {
        if(selectedMedias!=null){
            selectedCount = selectedMedias.size();
        }
        return selectedCount;
    }

    public boolean areMediaSelected() { return getSelectedCount() != 0;}

    public void selectAllPhotos() {
        for (int i = 0; i < medias.size(); i++) {
            if (!medias.get(i).isSelected()) {
                medias.get(i).setSelected(true);
                selectedMedias.add(medias.get(i));
            }
        }
    }

    private int toggleSelectPhoto(int index) {
        if (index >= 0 && medias.get(index) != null) {
            medias.get(index).setSelected(!medias.get(index).isSelected());
            if (medias.get(index).isSelected()) {
                selectedMedias.add(medias.get(index));
                if(getPreviewPath() != null && getPreviewPath().equals(medias.get(index).getPath()))
                    setPreviewSelected(true);
            }
            else {
                selectedMedias.remove(medias.get(index));
                if(getPreviewPath() != null && getPreviewPath().equals(medias.get(index).getPath()))
                    setPreviewSelected(false);
            }
        }
        return index;
    }

    public int toggleSelectPhoto(Media m) {
        return toggleSelectPhoto(medias.indexOf(m));
    }

    public void setDefaultSortingMode(Context context, SortingMode column) {
        settings.changeSortingMode(context, column);
    }

    public void setDefaultSortingAscending(Context context, SortingOrder sortingOrder) {
        settings.changeSortingOrder(context, sortingOrder);
    }


    public void updatePhotos(Context context) {
        medias = getAllMediaUnderAlbum(context);
        sortPhotos();
        setCount(medias.size());
    }

    public ArrayList<Media> getAllMediaUnderAlbum(Context context) {
        PreferenceUtil SP = PreferenceUtil.getInstance(context);
        ArrayList<Media> mediaArrayList = new ArrayList<Media>();

        if (isFromMediaStore()) {
            // 流程：咋子相册界面先加载每个图片文件夹下最新的图片信息
            // 之后查看每一个文件夹内的具体文件信息的时候，再将文件夹下的文件信息加载完全
            mediaArrayList.addAll(
                    MediaStoreProvider.getMedias(
                            context, id));
        } else {
//            mediaArrayList.addAll(StorageProvider.getMedias(
//                    getPath(), SP.getBoolean("set_include_video", false)));
        }
        return mediaArrayList;
    }

    public void sortPhotos() {
        Collections.sort(medias, MediaComparators.getComparator(settings.getSortingMode(), settings.getSortingOrder()));
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Album) {
            return getPath().equals(((Album) obj).getPath());
        }
        return super.equals(obj);
    }

    private boolean found_id_album = false;

    public FilterMode getFilterMode() {
        return settings != null ? settings.getFilterMode() : ALL;
    }

    /**
     * 获取文件夹的详情信息
     * @param context
     * @return
     */
    public TreeMap<String, String> getAlbumDetails(Context context) {
        TreeMap<String, String> details = new TreeMap<>();
        details.put(context.getString(R.string.folder_path), getPath());
        details.put(context.getString(R.string.folder_name), getName());
        details.put(context.getString(R.string.total_photos),Integer.toString(getCount()));
        details.put(context.getString(R.string.parent_path), getParentPath());
        details.put(context.getString(R.string.modified), lastmodified());
        details.put(context.getString(R.string.size_folder), size());

        if(isReadable()){
            details.put(context.getString(R.string.readable),  context.getString(R.string.answer_yes));
        }
        else{
            details.put(context.getString(R.string.readable), context.getString(R.string.answer_no));
        }
        if(isWritable()){
            details.put(context.getString(R.string.writable),  context.getString(R.string.answer_yes));
        }
        else{
            details.put(context.getString(R.string.writable), context.getString(R.string.answer_no));
        }
        return details;
    }

    /**
     * 连选当前点击图片 至前段位置图片间的所有图片信息
     * @param targetIndex
     * @param adapter
     */
    public void selectAllPhotosUpTo(int targetIndex, MediaAdapter adapter) {
        int indexRightBeforeOrAfter = -1;
        int indexNow;
        for (Media m: selectedMedias) {
            indexNow = medias.indexOf(m);

            if (indexRightBeforeOrAfter == -1) {
                indexRightBeforeOrAfter = indexNow;
            }

            if (indexNow > targetIndex) {
                break;
            }

            indexRightBeforeOrAfter = indexNow;
        }

        if (indexRightBeforeOrAfter != -1) {
            for (int index = Math.min(targetIndex, indexRightBeforeOrAfter);
                 index <= Math.max(targetIndex, indexRightBeforeOrAfter);
                 index ++) {
                if (medias.get(index) != null && !medias.get(index).isSelected()) {
                    medias.get(index).setSelected(true);
                    selectedMedias.add(medias.get(index));

                    if (getPreviewPath() != null &&
                            medias.get(index).getPath().equals(getPreviewPath())) {
                        setPreviewSelected(true);
                    }

                    adapter.notifyItemChanged(index);
                }
            }
        }
    }

    /**
     * 获得当前照片在相册下的下标信息
     * @param m
     * @return
     */
    public int getIndex(Media m) {
        return medias.indexOf(m);
    }

    /**
     * 清空当前Album内已经被选择的照片信息
     */
    public void clearSelectedPhotos() {
        for (Media m : medias) {
            m.setSelected(false);
        }
        if (selectedMedias != null) {
            selectedMedias.clear();
        }
        setPreviewSelected(false);  // 设置封面页没有被选择，待用
    }


    /**
     * 转移被选择的照片信息到指定的文件夹
     * @param context
     * @param targetDir
     * @return
     */
    public int moveSelectedMedia(Context context, String targetDir) {
        return moveAllMedia(context, targetDir, selectedMedias);
    }

    /**
     * 将 source文件路径的文件，移动至 targetDir这个文件夹中
     * @param context
     * @param source
     * @param targetDir
     * @return
     */
    private boolean moveMedia(Context context, String source, String targetDir) {
        File from = new File(source);
        File to = new File(targetDir);
        return FileUtils.moveFile(context, from, to);
    }


    /**
     * 转移albummedia信息到指定的文件夹
     * @param context
     * @param targetDir
     * @return
     */
    public int moveAllMedia(Context context, String targetDir, ArrayList<Media> albummedia){
        int n = 0;
        boolean isInCurrentAlbum = false;  // 看是不是从外部移动图片到本相册
        try
        {
            int index=-1;
            for (int i =0;i<albummedia.size(); i++) {

                String s = albummedia.get(i).getPath();
                int indexOfLastSlash = s.lastIndexOf("/");
                String fileName = s.substring(indexOfLastSlash + 1);

                if (!albummedia.get(i).getPath().equals(targetDir+"/"+fileName)) {
                    // 当前被选择的照片没有完成移动操作
                    index=-1;
                } else {
                    index=i;
                    break;
                }
            }

            if(index!=-1) {
                n = -1;  // 一种情况是：当前要移动文件所处的文件夹与当前的目标文件夹相同
            } else {
                // 当前被选择的照片都没有发生移动操作
                for (int i = 0; i < albummedia.size(); i++) {

                    if (moveMedia(context, albummedia.get(i).getPath(), targetDir)) {

                        if (targetDir.equals(getPath())) {
                            isInCurrentAlbum = true;
                            // 说明是将照片信息移入本相册
                            String path = albummedia.get(i).getPath();
                            int indexOfLastSlash = path.lastIndexOf("/");
                            String fileName = path.substring(indexOfLastSlash + 1);
                            medias.add(new Media(new File(targetDir, fileName)));
                        } else {
                            // 说明是将照片信息移出本相册
                            medias.remove(albummedia.get(i));
                        }
                        n++;
                    }
                }
                setCount(medias.size());
            }

            if (isInCurrentAlbum) {
                sortPhotos();  // 从外部加入照片时需要重新给照片排序
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return n;

    }

    /**
     * 删除Album对应的图片信息
     * @param context
     * @param media
     * @return
     */
    private boolean deleteMedia(Context context, Media media) {
        boolean success;
        File file = new File(media.getPath());
        if (success = FileUtils.deleteFile(context, file)) {
            // 如果被删除的文件是封面文件
            if (getPreviewPath() != null && media.getPath().equals(getPreviewPath())) {
                removeCoverAlbum(context);
            }
        }
        return success;
    }

    /**
     * 删除当前相册所有被选中的图片文件信息
     * @param context
     * @return
     */
    public boolean deleteSelectedMedia(Context context) {
        boolean success = true;
        for (Media selectedMedia: selectedMedias) {
            if (deleteMedia(context, selectedMedia)) {
                medias.remove(selectedMedia);
            } else {
                success = false;
            }

            if (getPreviewPath() != null && selectedMedia.getPath().equals(getPreviewPath())) {
                removeCoverAlbum(context);
            }
        }

        if (success) {
            clearSelectedPhotos();
            setCount(medias.size());
        }
        return success;
    }

    public boolean copySelectedPhotos(Context context, String folderPath) {
        boolean success = true;
        for (Media media: selectedMedias) {
            if (!copyPhoto(context, media.getPath(), folderPath)) {
                success = false;
            }
        }
        return success;
    }


    public boolean copyPhoto(Context context, String olderPath, String folderPath) {
        boolean success = false;
        try {
            File from = new File(olderPath);
            File to = new File(folderPath);
            if (success = FileUtils.copyFile(context, from, to)) {
                FileUtils.updateMediaStoreAfterCreate(context, new File(StringUtils.getPhotoPathMoved(olderPath, folderPath)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public boolean renameAlbum(final Context context, String newName) {
        found_id_album = false; //
        boolean success;
        File newDir = new File(StringUtils.getAlbumPathRenamed(getPath(), newName)); // 生成新的相册路径
        File oldDir = new File(getPath());
        success = oldDir.renameTo(newDir);

        if (success) {
            // 更新图片文件信息至MediaStore
            for (Media m: medias) {
                File from = new File(m.getPath());  // 旧相册中图片路径
                File to = new File(StringUtils.getPhotoPathRenamedAlbumChange(m.getPath(), newName)); // 新相册中图片路径
                FileUtils.updateMediaStore(context, from, true, null);
                FileUtils.updateMediaStore(context, to, false, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String s, Uri uri) {
                        if (!found_id_album) {
                            id = MediaStoreProvider.getAlbumId(context, s);
                            found_id_album = true;
                        }
                        Log.d(s, "onScanCompleted: "+s);
                        m.setPath(s);  // 更新相册中文件的路径信息
                        m.setUri(uri.toString());
                    }
                });
            }
            path = newDir.getAbsolutePath();
            name = newName;
        }
        return success;
    }

    /**
     * 移动当前相片文件的位置
     * @param context
     * @param targetDir
     * @return
     */
    public boolean moveCurrentMedia(Context context, String targetDir) {
        boolean success = false;
        try {
            String from = getCurrentMedia().getPath();
            if (success = moveMedia(context, from, targetDir)) {
                medias.remove(getCurrentMediaIndex());
                if (getPreviewPath() != null && from.equals(getPreviewPath())) {
                    removeCoverAlbum(context);
                }
                setCount(medias.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    /**
     * 移动指定图片文件至targetDir
     * @param context
     * @param targetDir
     * @param pathofmed
     * @return
     */
    public boolean moveAnyMedia(Context context, String targetDir, String pathofmed) {
        boolean success = false;
        try {
            String from = pathofmed;
            if (success = moveMedia(context, from, targetDir)) {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public boolean deleteCurrentMedia(Context context) {
        boolean success = deleteMedia(context, getCurrentMedia());
        if (success) {
            medias.remove(getCurrentMediaIndex());
            if(getPreviewPath() != null && getCurrentMedia().getPath().equals(getPreviewPath())) {
                removeCoverAlbum(context);
            }
            setCount(medias.size());
        }
        return success;
    }

}
