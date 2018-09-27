package com.photor.staralign;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.photopicker.PhotoPicker;
import com.example.photopicker.PhotoPreview;
import com.photor.R;
import com.photor.base.adapters.PhotoAdapter;
import com.photor.staralign.event.StarAlignEnum;
import com.photor.staralign.event.StarAlignProgressListener;
import com.photor.base.adapters.event.PhotoItemClickListener;
import com.photor.staralign.task.StarPhotoAlignThread;
import com.photor.util.FileUtils;
import com.shuhart.stepview.StepView;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;


public class StarAlignBaseActivity extends AppCompatActivity {



//    public static final int REQUEST_SPLIT_CODE = 100;

    private PhotoAdapter photoAdapter;
    private ArrayList<String> selectedPhotos = new ArrayList<>();

    private Button starAlignBtn;
    private RecyclerView recyclerView;
    private StepView starAlignStepView;

    private Mat alignResMat = new Mat(); // 进行图片对齐的Mat结果
    private String maskImgPath; // 星空模板的路径（地面是白色区域）

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star_align_base);

        // 初始化UI操作
        initUI();
    }

    private void initUI() {
        // -1. 初始化action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // 0. 设置图片选择的step view
        starAlignStepView = findViewById(R.id.star_align_step_view);

        // 1. 初始化显示选择图片的RecyclerView
        recyclerView = findViewById(R.id.star_align_rv);
        photoAdapter = new PhotoAdapter(selectedPhotos, this);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL));
        recyclerView.setAdapter(photoAdapter);

        recyclerView.addOnItemTouchListener(new PhotoItemClickListener(this,
                new PhotoItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (photoAdapter.getItemViewType(position) == PhotoAdapter.TYPE_ADD) {
                            PhotoPicker.builder()
                                    .setSelected(selectedPhotos)
                                    .setPhotoCount(PhotoAdapter.MAX_PHOTO_COUNT)
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
        updateStepInfo(StarAlignEnum.STAR_ALIGN_SELECT_PHOTOS.getCode());  // 初始化操作步骤信息（包括stepview和btn文字两个部分）
        starAlignBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentStep = starAlignStepView.getCurrentStep();
                if (currentStep == StarAlignEnum.STAR_ALIGN_SELECT_PHOTOS.getCode()) {  // 图片选择步骤
                    if (selectedPhotos.size() < 2) {
                        PhotoPicker.builder()
                                .setGridColumnCount(4)
                                .setPhotoCount(PhotoAdapter.MAX_PHOTO_COUNT)
                                .start(StarAlignBaseActivity.this);
                    }
                } else if (currentStep == StarAlignEnum.STAR_ALIGN_BOUNDARY.getCode()) {  // 图片边界划分步骤
                    String baseImgPath = selectedPhotos.get(0);
                    StarAlignOperator.splitBuilder()
                            .setBasePhotoPath(baseImgPath)
                            .start(StarAlignBaseActivity.this);
                } else if (currentStep == StarAlignEnum.STAR_ALIGN_RESULT.getCode()) {  // 图片对齐步骤
                    // 开始图片对齐操作
                    final String imgAbsPath =  FileUtils.generateImgAbsPath();
                    StarPhotoAlignThread thread = new StarPhotoAlignThread(StarAlignBaseActivity.this,
                            selectedPhotos, 0, alignResMat.getNativeObjAddr(),
                            imgAbsPath, maskImgPath,
                            new StarAlignProgressListener() {
                                @Override
                                public void onStarAlignThreadFinish(int alignResultFlag) {
                                    if (alignResultFlag == StarAlignEnum.STAR_ALIGN_RESLUT_SUCCESS.getCode()) {
                                        // 说明对齐操作成功
                                        StarAlignOperator.resultBuilder()
                                                .setAlignResultPath(imgAbsPath)
                                                .start(StarAlignBaseActivity.this);
                                    } else {
                                        Toast.makeText(StarAlignBaseActivity.this, "图片对齐失败", Toast.LENGTH_SHORT);
                                    }
                                }
                            });
                    thread.startAlign();
                }
            }
        });

//        // 3. 测试 对图片进行分割的按钮
//        findViewById(R.id.graffiti_test_btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String baseImgPath = selectedPhotos.get(0);
//                Intent intent = new Intent(StarAlignBaseActivity.this, StarAlignSplitActivity.class);
//                intent.putExtra(EXTRA_BASE_SELECT_PHOTO_PATH, baseImgPath);
//                startActivityForResult(intent, REQUEST_SPLIT_CODE);
//            }
//        });
//
//        // 4. 抠图功能测试
//        findViewById(R.id.grab_cut_test_btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String baseImgPath = selectedPhotos.get(0);
//                Intent intent = new Intent(StarAlignBaseActivity.this, GrabCutActivity.class);
//                intent.putExtra("baseImgPath", baseImgPath);
//                startActivity(intent);
//            }
//        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 图片选择选项
        if (resultCode == Activity.RESULT_OK &&
                (requestCode == PhotoPicker.REQUEST_CODE || requestCode == PhotoPreview.REQUEST_CODE)) {
            List<String> photos = null;

            if (data != null) {
                photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
            }
            selectedPhotos.clear();
            if (photos != null) {
                selectedPhotos.addAll(photos);
            }
            photoAdapter.notifyDataSetChanged();

            // 设定操作步骤信息
            if (selectedPhotos.size() <= 0) {
                updateStepInfo(StarAlignEnum.STAR_ALIGN_SELECT_PHOTOS.getCode());
            } else {
                updateStepInfo(StarAlignEnum.STAR_ALIGN_BOUNDARY.getCode());
            }
        }

        // 分割图片选项
        if (resultCode == Activity.RESULT_OK && requestCode == StarAlignOperator.REQUEST_BOUNDARY_CODE && data != null) {
            maskImgPath = data.getStringExtra(StarAlignOperator.EXTRA_MASK_IMG_PATH);
            // 设定操作步骤信息
            updateStepInfo(StarAlignEnum.STAR_ALIGN_RESULT.getCode());
        }

        // 在图片对齐完成之后，初始化图片对齐操作界面
        if (requestCode == StarAlignOperator.REQUEST_RESULT_CODE) {
            // 清空已选择的图片信息
            selectedPhotos.clear();
            photoAdapter.notifyDataSetChanged();
            updateStepInfo(StarAlignEnum.STAR_ALIGN_SELECT_PHOTOS.getCode());
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // 根据情况显示step view
    private void updateStepInfo(int currentStep) {
        // 首先更新step view的信息
        if (currentStep < starAlignStepView.getStepCount()) {
            starAlignStepView.go(currentStep, true);
        }

        // 然后更新图片对齐按钮的信息
        if (currentStep == StarAlignEnum.STAR_ALIGN_SELECT_PHOTOS.getCode()) {
            starAlignBtn.setText(R.string.star_align_btn_select_label);
        } else if (currentStep == StarAlignEnum.STAR_ALIGN_BOUNDARY.getCode()) {
            starAlignBtn.setText(R.string.star_align_btn_boundary_label);
        } else if (currentStep == StarAlignEnum.STAR_ALIGN_RESULT.getCode()) {
            starAlignBtn.setText(R.string.star_align_enter_btn_label);
        }
    }
}
