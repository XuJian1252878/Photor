package com.example.photopicker;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.photopicker.entity.Photo;
import com.example.photopicker.event.OnItemCheckListener;
import com.example.photopicker.fragment.ImagePagerFragment;
import com.example.photopicker.fragment.PhotoPickerFragment;

import java.util.ArrayList;
import java.util.List;

import static com.example.photopicker.PhotoPicker.DEFAULT_COLUMN_NUMBER;
import static com.example.photopicker.PhotoPicker.DEFAULT_MAX_COUNT;
import static com.example.photopicker.PhotoPicker.EXTRA_GRID_COLUMN;
import static com.example.photopicker.PhotoPicker.EXTRA_MAX_COUNT;
import static com.example.photopicker.PhotoPicker.EXTRA_ORIGINAL_PHOTOS;
import static com.example.photopicker.PhotoPicker.EXTRA_PREVIEW_ENABLED;
import static com.example.photopicker.PhotoPicker.EXTRA_SHOW_CAMERA;
import static com.example.photopicker.PhotoPicker.EXTRA_SHOW_GIF;
import static com.example.photopicker.PhotoPicker.KEY_SELECTED_PHOTOS;

public class PhotoPickerActivity extends AppCompatActivity {

    private final static String PHOTO_PICKER_FRAGMENT_TAG = "tag";
    private PhotoPickerFragment pickerFragment;
    private ImagePagerFragment imagePagerFragment;

    private MenuItem menuDoneItem;

    /** to prevent multiple calls to inflate menu */
    private boolean menuIsInflated = false;

    private boolean showGif = false;
    private ArrayList<String> originalPhotos = null;

    private int maxCount = DEFAULT_MAX_COUNT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.__picker_activity_photo_picker);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /**
         * Set the Z-axis elevation of the action bar in pixels.
         * The action bar's elevation is the distance it is placed from its parent surface.
         * Higher values are closer to the user.
         */
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 设置action bar的仰角
            actionBar.setElevation(25);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        // activity数据获取
        boolean showCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, true);  // 取不到值的话指定一个默认值
        boolean showGif = getIntent().getBooleanExtra(EXTRA_SHOW_GIF, true);
        boolean previewEnabled = getIntent().getBooleanExtra(EXTRA_PREVIEW_ENABLED, true);
        maxCount = getIntent().getIntExtra(EXTRA_MAX_COUNT, DEFAULT_MAX_COUNT);
        int columnNumber = getIntent().getIntExtra(EXTRA_GRID_COLUMN, DEFAULT_COLUMN_NUMBER);
        ArrayList<String> originalPhotos = getIntent().getStringArrayListExtra(EXTRA_ORIGINAL_PHOTOS);


        // 启动PhotoPickerFragment
        pickerFragment = (PhotoPickerFragment) getSupportFragmentManager().findFragmentByTag(PHOTO_PICKER_FRAGMENT_TAG);
        if (pickerFragment == null) {
            pickerFragment = PhotoPickerFragment.newInstance(showCamera, showGif, previewEnabled, columnNumber, maxCount, originalPhotos);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, pickerFragment, PHOTO_PICKER_FRAGMENT_TAG)
                    .commit();
            // 您需要把多次提交操作的同一个时间点一起执行，则使用 executePendingTransactions()
            // 马上执行fragment的commit操作，以便在接下来的代码中马上使用 pickerFragment
            getSupportFragmentManager().executePendingTransactions();
        }

        pickerFragment.getPhotoGridAdapter().setOnItemCheckListener(new OnItemCheckListener() {
            @Override
            public boolean onItemCheck(int position, Photo photo, int selectedItemCount) {
                // 这里进行点击复选框之后的刷新操作
                menuDoneItem.setEnabled(selectedItemCount > 0);

                if (maxCount <= 1) {
                    List<String> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
                    if (!photos.contains(photo.getPath())) {
                        photos.clear();
                        pickerFragment.getPhotoGridAdapter().notifyDataSetChanged();
                    }
                    return true;
                }

                if (selectedItemCount > maxCount) {
                    Toast.makeText(getActivity(), getString(R.string.__picker_over_max_count_tips), Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (maxCount > 1) {
                    menuDoneItem.setTitle(getString(R.string.__picker_done_with_count, selectedItemCount, maxCount));
                } else {
                    menuDoneItem.setTitle(getString(R.string.__picker_done));
                }
                return true;
            }
        });
    }

    // 刷新右上角的显示文字
    public void updateTitleDoneItem() {
        if (menuIsInflated) {
            // pickerFragment 处于活动的状态
            if (pickerFragment != null && pickerFragment.isResumed()) {
                List<String> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
                int size = photos == null ? 0 : photos.size();
                menuDoneItem.setEnabled(size > 0);
                if (maxCount > 1) {
                    menuDoneItem.setTitle(getString(R.string.__picker_done_with_count, size, maxCount));
                } else {
                    menuDoneItem.setTitle(getString(R.string.__picker_done));
                }
            } else if (imagePagerFragment != null && imagePagerFragment.isResumed()) {
                // 预览界面的图片总是可点的，没选就默认选当前的图片
                menuDoneItem.setEnabled(true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (imagePagerFragment != null && imagePagerFragment.isVisible()) {
            // ﻿这样子点击一下back键，那么就会回到上一个Fragment，如果没有这个方法的话，就不会回到上一个Fragment（如果是主Activity的话，那么程序会直接退出。）
            // getFragmentManager 会不起作用
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }

        if (item.getItemId() == R.id.done) {
            Intent intent = new Intent();
            ArrayList<String> selectedPhotos = null;
            if (pickerFragment != null) {
                selectedPhotos = pickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
            }
            // 如果在列表中没有选择图片，但是又在详情页面， 那么默认选择当前图片
            if (selectedPhotos.size() <= 0) {
                if(imagePagerFragment != null && imagePagerFragment.isResumed()){
                    // 预览界面
                    selectedPhotos = imagePagerFragment.getCurrentPath();
                }
            }

            if (selectedPhotos != null && selectedPhotos.size() > 0) {
                intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS, selectedPhotos);
                setResult(RESULT_OK, intent);
                finish();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!menuIsInflated) {
            getMenuInflater().inflate(R.menu.__picker_menu_picker, menu);
            menuDoneItem = menu.findItem(R.id.done);
            if (originalPhotos != null && originalPhotos.size() > 0) {
                menuDoneItem.setEnabled(true);
                menuDoneItem.setTitle(getString(R.string.__picker_done_with_count, originalPhotos.size(), maxCount));
            } else {
                menuDoneItem.setEnabled(false);
            }
            menuIsInflated = true;  // 防止菜单被重复加载
            return true;
        }
//        return super.onCreateOptionsMenu(menu);
        return false;
    }

    public void addImagePagerFragment(ImagePagerFragment imagePagerFragment) {
        this.imagePagerFragment = imagePagerFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, this.imagePagerFragment)
                .addToBackStack(null)  // 　保证photopicker这个fragment在preciew之后会显示出来
                .commit();
    }

    // 在Activity的内部类中 能够取到Activity的对象
    public PhotoPickerActivity getActivity() {
        return this;
    }

    public void setShowGif(boolean showGif) {
        this.showGif = showGif;
    }
}
