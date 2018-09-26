package com.example.photopicker.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.lifecycle.AndroidLifecycleUtils;
import com.example.photopicker.R;
import com.example.photopicker.entity.Photo;
import com.example.photopicker.entity.PhotoDirectory;
import com.example.photopicker.event.OnItemCheckListener;
import com.example.photopicker.event.OnPhotoClickListener;
import com.example.photopicker.utils.MediaStoreHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.example.photopicker.PhotoPicker.DEFAULT_COLUMN_NUMBER;

/**
 * Created by xujian on 2018/2/5.
 */

public class PhotoGridAdapter extends SelectableAdapter<PhotoGridAdapter.PhotoViewHolder>  {

    private RequestManager glide;

    private OnItemCheckListener onItemCheckListener = null;
    private OnPhotoClickListener onPhotoClickListener = null;
    private View.OnClickListener onCameraClickListener = null;

    private final static int ITEM_TYPE_CAMERA = 100;
    private final static int ITEM_TYPE_PHOTO = 101;

    private boolean hasCamera = true;
    private boolean previewEnable = true;

    private int imageSize;
    private int columnNumber = DEFAULT_COLUMN_NUMBER;

    // 获取每一列小图片应该有的宽度（在默认的显示图片的列数下）
    private void setColumnNumber(Context context, int columnNumber) {
        this.columnNumber = columnNumber;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int widthPixels = metrics.widthPixels;
        imageSize = widthPixels / columnNumber;
    }

    public PhotoGridAdapter(Context context, RequestManager requestManager, List<PhotoDirectory> photoDirectories) {
        this.photoDirectories = photoDirectories;
        this.glide = requestManager;
        setColumnNumber(context, columnNumber);
    }

    public PhotoGridAdapter(Context context, RequestManager requestManager, List<PhotoDirectory> photoDirectories, ArrayList<String> originalPhotos, int colNum) {
        this(context, requestManager, photoDirectories);
        setColumnNumber(context, columnNumber);
        this.selectedPhotos = new ArrayList<>();
        if (originalPhotos != null) {
            this.selectedPhotos.addAll(originalPhotos);
        }
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.__picker_item_photo, parent, false);
        PhotoViewHolder holder = new PhotoViewHolder(itemView);

        if (viewType == ITEM_TYPE_CAMERA) {
            holder.vSelected.setVisibility(View.GONE);
            holder.ivPhoto.setScaleType(ImageView.ScaleType.CENTER);
            holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { // 如果在 onBindViewHolder 中绑定，那么还需要调用 getViewType，这里调用就直接不需要绑定
                    if (onCameraClickListener != null) {
                        // 将定义照相机的操作交给外部调用者
                        onCameraClickListener.onClick(v);
                    }
                }
            });
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(final PhotoViewHolder holder, final int position) {
        if (getItemViewType(position) == ITEM_TYPE_PHOTO) {
            List<Photo> photos = getCurrentPhotos();

            // 获取对应的Photo对象
            final Photo photo;
            if (showCamera()) {
                photo = photos.get(position - 1);
            } else {
                photo = photos.get(position);
            }

            // 检查activity是否处于销毁状态
            boolean canLoadImage = AndroidLifecycleUtils.canLoadImage(holder.ivPhoto.getContext()); //

            if (canLoadImage) {
                RequestOptions options = new RequestOptions();
                options.centerCrop()
                        .dontAnimate()
                        .override(imageSize, imageSize)
                        .placeholder(R.drawable.__picker_ic_photo_black_48dp)
                        .error(R.drawable.__picker_ic_broken_image_black_48dp);

                glide.setDefaultRequestOptions(options)
                        .load(new File(photo.getPath()))
                        .thumbnail(0.5f)
                        .into(holder.ivPhoto);
            }

            boolean isChecked = isSelected(photo);

            // 加载对应状态的资源文件
            holder.ivPhoto.setSelected(isChecked);
            holder.vSelected.setSelected(isChecked);

            holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onPhotoClickListener != null) {
                        int pos = holder.getAdapterPosition();
                        if (previewEnable) {
                            onPhotoClickListener.onClick(v, pos, showCamera());
                        } else {
                            // 相当于执行了 vSelected 的click操作
                            // View类的performClick和callOnclick函数都可以实现，不用用户手动点击，直接触发View的点击事件。
                            holder.vSelected.performClick();
                        }
                    }
                }
            });

            holder.vSelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getAdapterPosition();
                    boolean isEnable = true;

                    if (onItemCheckListener != null) {
                        isEnable = onItemCheckListener.onItemCheck(pos, photo,
                                getSelectedPhotos().size() + (isSelected(photo) ? -1 : 1));
                    }

                    if (isEnable) {
                        // 设置是选择还是不选择被点击的图片
                        toggleSelection(photo);
                        notifyItemChanged(pos);
                    }
                }
            });

        } else {
            holder.ivPhoto.setImageResource(R.drawable.__picker_camera);
        }
    }

    @Override
    public int getItemCount() {
        int photosCount = photoDirectories.size() == 0 ? 0 : getCurrentPhotos().size();
        if (showCamera()) {
            return photosCount + 1;
        }
        return photosCount;
    }

    @Override
    public int getItemViewType(int position) {
        // 当前是照相的图标被点击还是普通的图片图标被点击
        return (showCamera() && position == 0) ? ITEM_TYPE_CAMERA : ITEM_TYPE_PHOTO;
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private ImageView vSelected;

        public PhotoViewHolder(View itemView) {
            super(itemView); // 这个是必须的，必须实现RecyclerView.ViewHolder的构造函数
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            vSelected = itemView.findViewById(R.id.v_selected);
        }
    }


    public void setOnItemCheckListener(OnItemCheckListener onItemCheckListener) {
        this.onItemCheckListener = onItemCheckListener;
    }

    public void setOnPhotoClickListener(OnPhotoClickListener onPhotoClickListener) {
        this.onPhotoClickListener = onPhotoClickListener;
    }

    public void setOnCameraClickListener(View.OnClickListener onCameraClickListener) {
        this.onCameraClickListener = onCameraClickListener;
    }

    public void setShowCamera(boolean hasCamera) {
        this.hasCamera = hasCamera;
    }

    public void setPreviewEnable(boolean previewEnable) {
        this.previewEnable = previewEnable;
    }

    // 是否显示照相机图标（只有是显示全部图像的时候才能显示照相机图标）
    public boolean showCamera() {
        return (hasCamera && currentDirectoryIndex == MediaStoreHelper.INDEX_ALL_PHOTOS);
    }

    public ArrayList<String> getSelectedPhotoPaths() {
        // 因为不知道selectedPhotos有没有被初始化，所以只能重新创建一个对象
        ArrayList<String> selectedPhotoPaths = new ArrayList<>(getSelectedItemCount());

        for (String photo: selectedPhotos) {
            selectedPhotoPaths.add(photo);
        }
        return selectedPhotoPaths;
    }

    @Override
    public void onViewRecycled(PhotoViewHolder holder) {
        // 在View或者Target上调用clear()方法都表示，Glide要取消加载，可以安全地把Target占用的所有资源（Bitmap，bytes数组等）放入资源池中（pool）。
        glide.clear(holder.ivPhoto);  // 如果图片已经不在显示范围之内了，那么取消这个ImageView的加载请求
        super.onViewRecycled(holder);
    }
}
