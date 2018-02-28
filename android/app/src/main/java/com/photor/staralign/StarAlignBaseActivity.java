package com.photor.staralign;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.example.photopicker.PhotoPicker;
import com.example.photopicker.PhotoPickerActivity;
import com.example.photopicker.PhotoPreview;
import com.photor.R;
import com.photor.staralign.adapter.StarPhotoAdapter;
import com.photor.staralign.event.StarPhotoItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class StarAlignBaseActivity extends AppCompatActivity {

    private StarPhotoAdapter starPhotoAdapter;
    private ArrayList<String> selectedPhotos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star_align_base);

        RecyclerView recyclerView = findViewById(R.id.star_align_rv);
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

        findViewById(R.id.star_align_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPicker.builder()
                        .setGridColumnCount(4)
                        .setPhotoCount(StarPhotoAdapter.MAX_PHOTO_COUNT)
                        .start(StarAlignBaseActivity.this);
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
            }
            starPhotoAdapter.notifyDataSetChanged();
        }
    }
}
