package com.photor.staralign;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.photor.R;
import com.photor.util.ImageUtils;
import com.photor.widget.graffiti.GraffitiView;

public class StarAlignSplitActivity extends AppCompatActivity {

    private FrameLayout starAlignSplitContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star_align_split);

        Intent intent = getIntent();
        String baseImgPath = intent.getStringExtra("baseImgPath");

        Bitmap originBitmap;
        originBitmap = ImageUtils.createBitmapFromPath(baseImgPath, StarAlignSplitActivity.this);

        starAlignSplitContainer = findViewById(R.id.star_align_split_container);

        GraffitiView graffitiView = new GraffitiView(StarAlignSplitActivity.this, originBitmap,
                baseImgPath, true, new GraffitiView.GraffitiListener() {
            @Override
            public void onSaved(Bitmap bitmap, Bitmap bitmapEraser) {

            }

            @Override
            public void onError(int i, String msg) {

            }

            @Override
            public void onReady() {

            }
        });

        graffitiView.setPen(GraffitiView.Pen.HAND);
        graffitiView.setShape(GraffitiView.Shape.HAND_WRITE);

        starAlignSplitContainer.addView(graffitiView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }
}
