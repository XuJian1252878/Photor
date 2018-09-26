package com.example.photopicker.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.lifecycle.AndroidLifecycleUtils;
import com.example.media.image.MediaStoreHelper;
import com.example.media.image.imagecapture.ImageCaptureManager;
import com.example.permissions.PermissionsConstant;
import com.example.permissions.PermissionsUtils;
import com.example.photopicker.PhotoPickerActivity;
import com.example.photopicker.R;
import com.example.photopicker.adapter.PhotoGridAdapter;
import com.example.photopicker.adapter.PopupDirectoryListAdapter;
import com.example.media.image.entity.Photo;
import com.example.media.image.entity.PhotoDirectory;
import com.example.photopicker.event.OnPhotoClickListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.example.media.image.MediaStoreHelper.INDEX_ALL_PHOTOS;
import static com.example.media.image.PhotoPicker.DEFAULT_COLUMN_NUMBER;
import static com.example.media.image.PhotoPicker.EXTRA_GRID_COLUMN;
import static com.example.media.image.PhotoPicker.EXTRA_MAX_COUNT;
import static com.example.media.image.PhotoPicker.EXTRA_ORIGINAL_PHOTOS;
import static com.example.media.image.PhotoPicker.EXTRA_PREVIEW_ENABLED;
import static com.example.media.image.PhotoPicker.EXTRA_SHOW_CAMERA;
import static com.example.media.image.PhotoPicker.EXTRA_SHOW_GIF;

/**
 * Created by xujian on 2018/1/9.
 */

public class PhotoPickerFragment extends Fragment {

    //目录弹出框的一次最多显示的目录数目
    public static int COUNT_MAX = 4;

    private int column; // 默认显示的是多少列

    private ImageCaptureManager captureManager;

    // 所有photos的路径
    private List<PhotoDirectory> directories;
    // 已选的照片
    private ArrayList<String> originalPhotos;

    private RequestManager mGlideRequestManager;
    private ListPopupWindow listPopupWindow;

    private PhotoGridAdapter photoGridAdapter;
    private PopupDirectoryListAdapter listAdapter;

    private int SCROLL_THRESHOLD = 30;

    public static PhotoPickerFragment newInstance(boolean showCamera, boolean showGif,
                                                  boolean previewEnable, int column, int maxCount,
                                                  ArrayList<String> originalPhotos) {
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_SHOW_CAMERA, showCamera);
        args.putBoolean(EXTRA_SHOW_GIF, showGif);
        args.putBoolean(EXTRA_PREVIEW_ENABLED, previewEnable);
        args.putInt(EXTRA_GRID_COLUMN, column);
        args.putInt(EXTRA_MAX_COUNT, maxCount);
        args.putStringArrayList(EXTRA_ORIGINAL_PHOTOS, originalPhotos);

        PhotoPickerFragment fragment = new PhotoPickerFragment();
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Fragment具有属性retainInstance，默认值为false。当设备旋转时，fragment会随托管activity一起销毁并重建。
         *
         * 但是这个要和 pickerFragment = (PhotoPickerFragment) getSupportFragmentManager().findFragmentByTag(PHOTO_PICKER_FRAGMENT_TAG);
         * 一起用 tag 保证恢复fragment 控件状态的时候能够根据tag恢复；setRetainInstance 保证跟UI相关的状态的恢复
         *
         * 普通情况下：
         * Once Fragment is returned from backstack, its View would be destroyed and recreated.
         * In this case, Fragment is not destroyed. Only View inside Fragment does.
         */
        setRetainInstance(true);

        mGlideRequestManager = Glide.with(this);

        directories = new ArrayList<>();
        originalPhotos = getArguments().getStringArrayList(EXTRA_ORIGINAL_PHOTOS);

        column = getArguments().getInt(EXTRA_GRID_COLUMN, DEFAULT_COLUMN_NUMBER);
        boolean showCamera = getArguments().getBoolean(EXTRA_SHOW_CAMERA, true);
        boolean previewEnable = getArguments().getBoolean(EXTRA_PREVIEW_ENABLED, true);

        photoGridAdapter = new PhotoGridAdapter(getActivity(), mGlideRequestManager, directories, originalPhotos, column);
        photoGridAdapter.setShowCamera(showCamera);
        photoGridAdapter.setPreviewEnable(previewEnable);

        listAdapter = new PopupDirectoryListAdapter(directories, mGlideRequestManager);

        Bundle mediaStoreArgs = new Bundle();

        boolean showGif = getArguments().getBoolean(EXTRA_SHOW_GIF);
        mediaStoreArgs.putBoolean(EXTRA_SHOW_GIF, showGif);
        // 这里进行loaderManger的load操作
        MediaStoreHelper.getPhotoDirs(getActivity(), mediaStoreArgs, new MediaStoreHelper.PhotosResultCallback() {
            @Override
            public void onResultCallback(List<PhotoDirectory> dirs) {
                // dirs 是已经从MediaStore中取出的图片信息
                directories.clear();
                directories.addAll(dirs); // 填充入全部的数据
                // 通知RecyclerView和ListView更新数据
                photoGridAdapter.notifyDataSetChanged();
                listAdapter.notifyDataSetChanged();
                adjustHeight(); // 调整listPopupWindow的高度
            }
        });

        captureManager = new ImageCaptureManager(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.__picker_fragment_photo_picker, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.rv_photos);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(column, OrientationHelper.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(photoGridAdapter);

        final Button btSwitchDirectory = rootView.findViewById(R.id.category_btn);

        listPopupWindow = new ListPopupWindow(getActivity());
        listPopupWindow.setWidth(ListPopupWindow.MATCH_PARENT);
        listPopupWindow.setAnchorView(rootView.findViewById(R.id.category_footer));
        listPopupWindow.setAdapter(listAdapter);
        listPopupWindow.setModal(true);
        listPopupWindow.setDropDownGravity(Gravity.BOTTOM);

        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listPopupWindow.dismiss();

                PhotoDirectory directory = directories.get(position);
                btSwitchDirectory.setText(directory.getName());

                photoGridAdapter.setCurrentDirectoryIndex(position);
                photoGridAdapter.notifyDataSetChanged();
            }
        });

        // 设置照片被点击时的事件信息
        photoGridAdapter.setOnPhotoClickListener(new OnPhotoClickListener() {
            @Override
            public void onClick(View v, int position, boolean showCamera) {
                int index = showCamera ? position - 1 : position;

                List<String> photos = photoGridAdapter.getCurrentPhotoPaths();

                ImagePagerFragment imagePagerFragment = ImagePagerFragment.newInstance(photos, index);
                // 两个fragment通过寄宿的activity通信
                ((PhotoPickerActivity)getActivity()).addImagePagerFragment(imagePagerFragment);
            }
        });

        // 设置拍照图片被点击时的事件
        photoGridAdapter.setOnCameraClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 如果没有照相机相关的权限，那么向用户
                if (!PermissionsUtils.checkCameraPermission(PhotoPickerFragment.this)) return;
                if (!PermissionsUtils.checkWriteStoragePermission(PhotoPickerFragment.this)) return;
                openCamera();
            }
        });

        btSwitchDirectory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listPopupWindow != null && listPopupWindow.isShowing()) {
                    listPopupWindow.dismiss();
                } else if (!getActivity().isFinishing()) {
                    adjustHeight();  // 根据文件夹的个数调整listPopupWindow的高度
                    listPopupWindow.show();
                }
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                /**
                 * The RecyclerView is not currently scrolling.（静止没有滚动）
                 */
                //  public static final int SCROLL_STATE_IDLE = 0;

                /**
                 * The RecyclerView is currently being dragged by outside input such as user touch input.
                 *（正在被外部拖拽,一般为用户正在用手指滚动）
                 */
                //  public static final int SCROLL_STATE_DRAGGING = 1;

                /**
                 * The RecyclerView is currently animating to a final position while not under outside control.
                 *（自动滚动）
                 */
                //  public static final int SCROLL_STATE_SETTLING = 2;

                // 当停止滚动的时候，恢复加载请求
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    resumeRequestsIfNotDestroyed();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // dx dy 为每次调用onScrolled的时候的 滚动过的距离，手指向上向左的时候为正，向下向右的时候为负
                if (Math.abs(dy) > SCROLL_THRESHOLD) {
                    // 如果滚动的速度草果一定的幅度，那么停止图片加载的请求
                    mGlideRequestManager.pauseRequests();
                } else {
                    // 滚动的速度下降了，说明滚动即将停止的时候恢复滚动请求
                    resumeRequestsIfNotDestroyed();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof PhotoPickerActivity) {
            PhotoPickerActivity photoPickerActivity = (PhotoPickerActivity)getActivity();
            photoPickerActivity.updateTitleDoneItem();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 对返回的拍照结果进行处理
        if (requestCode == ImageCaptureManager.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            if (captureManager == null) {
                FragmentActivity activity = getActivity();
                captureManager = new ImageCaptureManager(activity);
            }

            // 向MediaStore中加入 刚拍照的图片路径信息
            captureManager.galleryAddPic();
            if (directories.size() > 0) {
                String path = captureManager.getCurrentPhotoPath();
                // 将新拍的图片加入全部文件夹下
                PhotoDirectory directory = directories.get(INDEX_ALL_PHOTOS);
                directory.getPhotos().add(INDEX_ALL_PHOTOS, new Photo(path.hashCode(), path));
                directory.setCoverPath(path);
                photoGridAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case PermissionsConstant.REQUEST_CAMERA:
                case PermissionsConstant.REQUEST_EXTERNAL_WRITE:
                    // 再检查一遍是否拥有了照相机权限
                    if (PermissionsUtils.checkWriteStoragePermission(this) &&
                            PermissionsUtils.checkCameraPermission(this)) {
                        openCamera();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public PhotoGridAdapter getPhotoGridAdapter() {
        return photoGridAdapter;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // 将当前的 拍照图片的路径存储起来
        captureManager.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    // tells the fragment that all of the saved state of its view hierarchy has been restored.
    // Called when all saved state has been restored into the view hierarchy of the fragment.
    // This is called after onActivityCreated(Bundle) and before onStart().
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        // 将之前的 拍照图片的路径 恢复出来
        captureManager.onRestoreInstanceState(savedInstanceState);
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (directories == null) {
            return;
        }

        // 释放directory数据
        for (PhotoDirectory directory : directories) {
            directory.getPhotoPaths().clear();
            directory.getPhotos().clear();
            directory.setPhotos(null);
        }

        directories.clear();
        directories = null;
    }

    public void adjustHeight() {
        if (listAdapter == null) {
            return;
        }
        int count = listAdapter.getCount();
        count = count < COUNT_MAX ? count : COUNT_MAX;
        if (listPopupWindow != null) {
            listPopupWindow.setHeight(count * getResources().getDimensionPixelOffset(R.dimen.__picker_item_directory_height));
        }
    }

    private void openCamera() {
        try {
            Intent intent = captureManager.dispatchTakePictureIntent();
            startActivityForResult(intent, ImageCaptureManager.REQUEST_TAKE_PHOTO);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ActivityNotFoundException e) {
            Log.e("PhotoPickerFragment", "No Activity Found to handle Intent", e);
        }
    }

    private void resumeRequestsIfNotDestroyed() {
        if (!AndroidLifecycleUtils.canLoadImage(this)) {
            return;
        }

        // 恢复加载图片的请求
        mGlideRequestManager.resumeRequests();
    }
}
