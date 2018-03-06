package com.photor.staralign;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.photor.R;
import com.photor.util.ImageUtils;
import com.photor.widget.graffiti.ColorPickerDialog;
import com.photor.widget.graffiti.GraffitiView;

public class StarAlignSplitActivity extends AppCompatActivity {

    private FrameLayout starAlignSplitContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star_align_split);

        initUI();
    }


    private void initUI() {

        // 1. 显示待切割的图片信息
        Intent intent = getIntent();
        String baseImgPath = intent.getStringExtra("baseImgPath");

        Bitmap originBitmap;
        originBitmap = ImageUtils.createBitmapFromPath(baseImgPath, StarAlignSplitActivity.this);

        starAlignSplitContainer = findViewById(R.id.star_align_split_container);

        final GraffitiView graffitiView = new GraffitiView(StarAlignSplitActivity.this, originBitmap,
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

        // 2. 绑定绘图需要的事件
        // 2.1 设置手绘线条模式
        findViewById(R.id.btn_hand_write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                graffitiView.setShape(GraffitiView.Shape.HAND_WRITE);
            }
        });

        // 2.2 设置橡皮擦模式
        findViewById(R.id.btn_pen_eraser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                graffitiView.setPen(GraffitiView.Pen.ERASER);
            }
        });

        // 2.3 设置清屏模式
        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                graffitiView.clear();
            }
        });

        // 2.4 设置撤销模式
        findViewById(R.id.btn_undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                graffitiView.undo();
            }
        });

        // 2.5 设置颜色选择框模式
        final ImageView btnColor = findViewById(R.id.btn_set_color);
        // 初始化颜色信息
        if (graffitiView.getGraffitiColor().getType() == GraffitiView.GraffitiColor.Type.COLOR) {
            btnColor.setBackgroundColor(graffitiView.getGraffitiColor().getColor());
        } else if (graffitiView.getGraffitiColor().getType() == GraffitiView.GraffitiColor.Type.BITMAP) {
            btnColor.setBackgroundDrawable(new BitmapDrawable(graffitiView.getGraffitiColor().getBitmap()));
        }
        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ColorPickerDialog(StarAlignSplitActivity.this, graffitiView.getGraffitiColor().getColor(), "画笔颜色",
                        new ColorPickerDialog.OnColorChangedListener() {
                            public void colorChanged(int color) {
                                btnColor.setBackgroundColor(color);
                                graffitiView.setColor(color);
                            }

                            @Override
                            public void colorChanged(Drawable color) {
                                btnColor.setBackgroundDrawable(color);
                                graffitiView.setColor(ImageUtils.getBitmapFromDrawable(color));
                            }
                        }).show();
            }
        });

        // 2.6 设置进度条模式
        SeekBar paintSizeBar = findViewById(R.id.paint_size);
        final TextView paintSizeView = findViewById(R.id.paint_size_text);
        paintSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                paintSizeView.setText("" + progress);
                graffitiView.setPaintSize(progress);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        paintSizeBar.setProgress((int) graffitiView.getPaintSize());
    }
}
