package com.photor.staralign;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.Button;

import com.example.photopicker.PhotoPicker;
import com.example.photopicker.PhotoPickerActivity;
import com.example.photopicker.PhotoPreview;
import com.photor.R;
import com.photor.staralign.adapter.StarPhotoAdapter;
import com.photor.staralign.event.StarPhotoItemClickListener;

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
        // 初始化显示选择图片的RecyclerView
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


        // 设置 选择图片/进行图片对齐 操作的按钮
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
                    alignStarPhotos(selectedPhotos, 0, alignResMat.getNativeObjAddr());
                }
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
            starAlignBtn.setText(R.string.star_align_btn_enter_label);
        }
    }

    // 进行图像对齐操作的 jni native function
    private native int alignStarPhotos(ArrayList<String> starPhotos, int alignBasePhotoIndex, long alignResMatAddr);
}
