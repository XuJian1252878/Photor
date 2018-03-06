package com.photor.staralign;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.photopicker.PhotoPicker;
import com.example.photopicker.PhotoPreview;
import com.photor.R;
import com.photor.staralign.adapter.StarPhotoAdapter;
import com.photor.staralign.event.StarAlignEnum;
import com.photor.staralign.event.StarAlignProgressListener;
import com.photor.staralign.event.StarPhotoItemClickListener;
import com.photor.staralign.task.StarPhotoAlignThread;
import com.photor.util.FileUtils;
import com.photor.util.ImageUtils;
import com.photor.widget.graffiti.GraffitiView;

import org.opencv.core.Mat;

import java.io.IOException;
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
//                    new StarPhotoAlignTask(StarAlignBaseActivity.this, selectedPhotos, 0, alignResMat.getNativeObjAddr()).execute();
                    StarPhotoAlignThread thread = null;
                    try {
                        // 开始图片对齐操作
                        final String imgAbsPath =  FileUtils.generateImgAbsPath();
                        thread = new StarPhotoAlignThread(StarAlignBaseActivity.this,
                                selectedPhotos, 0, alignResMat.getNativeObjAddr(), imgAbsPath,
                                new StarAlignProgressListener() {
                                    @Override
                                    public void onStarAlignThreadFinish(int alignResultFlag) {
                                        if (alignResultFlag == StarAlignEnum.STAR_ALIGN_RESLUT_SUCCESS.getResCode()) {
                                            // 说明对齐操作成功
                                            StarAlignSetting.builder()
                                                    .setAlignResultPath(imgAbsPath)
                                                    .start(StarAlignBaseActivity.this);
                                        } else {
                                            Toast.makeText(StarAlignBaseActivity.this, "图片对齐失败", Toast.LENGTH_SHORT);
                                        }
                                    }
                                });
                        thread.startAlign();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // 3. 测试 对图片进行分割的按钮
        findViewById(R.id.graffiti_test_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String baseImgPath = selectedPhotos.get(0);
                Intent intent = new Intent(StarAlignBaseActivity.this, StarAlignSplitActivity.class);
                intent.putExtra("baseImgPath", baseImgPath);
                startActivity(intent);
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
