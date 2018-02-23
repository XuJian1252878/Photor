package com.example.photopicker;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.photopicker.fragment.ImagePagerFragment;

import java.util.List;

import static com.example.photopicker.PhotoPicker.KEY_SELECTED_PHOTOS;
import static com.example.photopicker.PhotoPreview.EXTRA_CURRENT_ITEM;
import static com.example.photopicker.PhotoPreview.EXTRA_PHOTOS;
import static com.example.photopicker.PhotoPreview.EXTRA_SHOW_DELETE;

public class PhotoPagerActivity extends AppCompatActivity {

    private ImagePagerFragment pagerFragment;

    private ActionBar actionBar;
    private boolean showDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.__picker_activity_photo_pager);

        int currentItem = getIntent().getIntExtra(EXTRA_CURRENT_ITEM, 0);
        List<String> paths = getIntent().getStringArrayListExtra(EXTRA_PHOTOS);
        showDelete = getIntent().getBooleanExtra(EXTRA_SHOW_DELETE, true);

        if (pagerFragment == null) {
            pagerFragment = (ImagePagerFragment) getSupportFragmentManager().findFragmentById(R.id.photoPagerFragment);
        }
        // 设置pagerView的初始信息
        pagerFragment.setPhotos(paths, currentItem);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            updateActionBarTitle();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                // 设置actionBar的仰角
                actionBar.setElevation(25);
            }
        }

        // 设置当 PagerView 被滑动的时候响应的事件
        pagerFragment.getViewPager().addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // 更新当前的进度数据
                updateActionBarTitle();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (showDelete) {
            getMenuInflater().inflate(R.menu.__picker_menu_preview, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (item.getItemId() == R.id.delete) {
            final int index  = pagerFragment.getCurrentItem();  // 获取当前正在被显示的图片下标
            final String deletedPath = pagerFragment.getPaths().get(index);

            // 保证Snackbar弹出的时候不会遮盖住 pagerFragment.getView()
            Snackbar snackbar = Snackbar.make(pagerFragment.getView(), R.string.__picker_deleted_a_photo, Snackbar.LENGTH_LONG);

            // 当照片只有一张的时候，提示用户是否真的需要删除照片
            if (pagerFragment.getPaths().size() <= 1) {
                // 显示警告的对话框
                new AlertDialog.Builder(this)
                        .setTitle(R.string.__picker_confirm_to_delete)
                        .setPositiveButton(R.string.__picker_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 用户选择了删除图片
                                dialog.dismiss();
                                pagerFragment.getPaths().remove(index);
                                pagerFragment.getViewPager().getAdapter().notifyDataSetChanged();;
                                onBackPressed();
                            }
                        })
                        .setNegativeButton(R.string.__picker_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            } else {
                snackbar.show();
                pagerFragment.getPaths().remove(index);
                pagerFragment.getViewPager().getAdapter().notifyDataSetChanged();
            }

            snackbar.setAction(R.string.__picker_undo, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (pagerFragment.getPaths().size() > 0) {
                        pagerFragment.getPaths().add(index, deletedPath);
                    } else {
                        pagerFragment.getPaths().add(deletedPath);
                    }

                    pagerFragment.getViewPager().getAdapter().notifyDataSetChanged();
                    // boolean: True to smoothly scroll to the new item, false to transition immediately
                    pagerFragment.getViewPager().setCurrentItem(index, true);
                }
            });

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // 需要将数据（可能是经过删除之后的返回给调用的activity）
        Intent intent = new Intent();
        intent.putExtra(KEY_SELECTED_PHOTOS, pagerFragment.getPaths());
        setResult(RESULT_OK, intent);  // 与startActivityForResult成对调用
        finish();

        super.onBackPressed();
    }

    public void updateActionBarTitle() {
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.__picker_image_index,
                    pagerFragment.getViewPager().getCurrentItem() + 1,
                    pagerFragment.getPaths().size()));
        }
    }
}
