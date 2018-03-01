package com.photor.staralign;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.Button;

import com.example.photopicker.PhotoPicker;
import com.example.photopicker.PhotoPreview;
import com.photor.R;
import com.photor.staralign.adapter.StarPhotoAdapter;
import com.photor.staralign.event.StarPhotoItemClickListener;
import com.photor.staralign.task.StarPhotoAlignTask;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;


public class StarAlignBaseActivity extends AppCompatActivity {

    private StarPhotoAdapter starPhotoAdapter;
    private ArrayList<String> selectedPhotos = new ArrayList<>();

    private Button starAlignBtn;
    private RecyclerView recyclerView;

    private Mat alignResMat = new Mat(); // 进行图片对齐的Mat结果

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star_align_base);

        initUI();
    }

    private void initUI() {
        // 1. 初始化显示选择图片的RecyclerView
        recyclerView = findViewById(R.id.star_align_rv);
        starPhotoAdapter = new StarPhotoAdapter(selectedPhotos, this);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(4, OrientationHelper.VERTICAL));
        recyclerView.setAdapter(starPhotoAdapter);

        recyclerView.addOnItemTouchListener(new StarPhotoItemClickListener(this,
                new StarPhotoItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (starPhotoAdapter.getItemViewType(position) == StarPhotoAdapter.TYPE_ADD) {
                            PhotoPicker.builder()
                                    .setSelected(selectedPhotos)
                                    .setPhotoCount(StarPhotoAdapter.MAX_PHOTO_COUNT)
                                    .start(StarAlignBaseActivity.this);
                        } else {
                            PhotoPreview.builder()
                                    .setPhotos(selectedPhotos)
                                    .setCurrentItem(position)
                                    .start(StarAlignBaseActivity.this);
                        }
                    }
                }));


        // 2. 设置 选择图片/进行图片对齐 操作的按钮
        starAlignBtn = findViewById(R.id.star_align_btn);
        updateStarAlignBtnText();

        starAlignBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPhotos.size() < 2) {
                    PhotoPicker.builder()
                            .setGridColumnCount(4)
                            .setPhotoCount(StarPhotoAdapter.MAX_PHOTO_COUNT)
                            .start(StarAlignBaseActivity.this);
                } else {
                    new StarPhotoAlignTask(StarAlignBaseActivity.this, selectedPhotos, 0, alignResMat.getNativeObjAddr()).execute();
                }
            }
        });

        // 3. 初始化 星空图片处理的 processbar
        findViewById(R.id.square_progress_bar_test_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示 star align处理的进度条
//                ProgressDialog dialog = new ProgressDialog(StarAlignBaseActivity.this);
//                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // 设置进度条的形式为圆形转动的进度条
//                dialog.setCancelable(false); // 设置是否可以通过点击Back键取消
//                dialog.setCanceledOnTouchOutside(false); // 设置在点击Dialog外是否取消Dialog进度条
//                dialog.setTitle(R.string.star_align_progress_dialog_title);
//                // 设置dismiss监听
//                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//                        // 取消
//                    }
//                });
//
//                // 设置取消按钮
//                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//
//                            }
//                        });
//                dialog.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK &&
                (requestCode == PhotoPicker.REQUEST_CODE || requestCode == PhotoPreview.REQUEST_CODE)) {
            List<String> photos = null;

            if (data != null) {
                photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
            }
            selectedPhotos.clear();
            if (photos != null) {
                selectedPhotos.addAll(photos);
                updateStarAlignBtnText();
            }
            starPhotoAdapter.notifyDataSetChanged();
        }
    }

    private void updateStarAlignBtnText() {
        if (selectedPhotos.size() < 2) {
            starAlignBtn.setText(R.string.star_align_btn_select_label);
        } else {
            starAlignBtn.setText(R.string.star_align_enter_btn_label);
        }
    }
}
