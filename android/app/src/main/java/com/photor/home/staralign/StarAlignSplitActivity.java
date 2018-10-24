package com.photor.home.staralign;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.file.FileUtils;
import com.example.theme.ThemeHelper;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.photor.R;
import com.photor.util.ImageUtils;
import com.photor.widget.graffiti.ColorPickerDialog;
import com.photor.widget.graffiti.GraffitiView;
import com.xw.repo.BubbleSeekBar;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import static com.photor.home.staralign.StarAlignOperator.EXTRA_BASE_SELECT_PHOTO_PATH;
import static com.photor.home.staralign.StarAlignOperator.EXTRA_MASK_IMG_PATH;

public class StarAlignSplitActivity extends AppCompatActivity {

    private FrameLayout starAlignSplitContainer;
    private Mat resImgMat = new Mat();
    private Mat oriImgMat = new Mat();
    private Mat maskImgMat = new Mat();
    private boolean isReadySplit = false;  // 记录有没准备好划分星空和地面的分界线

    private String maskImgPath;
    private int splitDrawLineColor = Color.GREEN;

    // 表示当前划分分界线的状态
    private enum BoundaryEnum {
        NOT_INIT_POSITION(-1);

        private int state;

        BoundaryEnum(int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }
    }

    private int OPERATE_FLAG = 0;  // 1 星空（前景）；2 地面（背景）；3 分界线
    private int GRABCUT_TOUCH_EVENT = -1; // DOWN = 0, UP = 1, MOVE = 2
    private int LAST_BOUNDARY_X = BoundaryEnum.NOT_INIT_POSITION.getState(),
            LAST_BOUNDARY_Y = BoundaryEnum.NOT_INIT_POSITION.getState();

    private GraffitiView graffitiView;

    public native void initGrabCut(long oriImgMatAddr, long resImgMatAddr, long maskMatAddr);
    public native void moveGrabCut(int event, int x, int y, int flags, int lastX, int latsY);
    public native void reset();
    public native boolean saveMaskMat(String maskImgPath);
    public native void grabCutOver();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star_align_split);

        initUI();
    }

    private void initUI() {

        // 0. 显示action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // 1. 显示待切割的图片信息
        Intent intent = getIntent();
        final String baseImgPath = intent.getStringExtra(EXTRA_BASE_SELECT_PHOTO_PATH);

        // 2. 异步加载划线界面
        new LoadGraffitiAsyncTask(baseImgPath).execute();

    }

    private void showImage() {
        starAlignSplitContainer.removeAllViews();
        ImageView imgView = new ImageView(this);
        Bitmap bm = Bitmap.createBitmap(graffitiView.getOriginBitmap().getWidth(),
                graffitiView.getOriginBitmap().getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(resImgMat, bm);
//        Bitmap bm = Bitmap.createBitmap(graffitiView.getOriginBitmap().getWidth(),
//                graffitiView.getOriginBitmap().getHeight(), Bitmap.Config.RGB_565);
//        Utils.matToBitmap(maskImgMat, bm);
        imgView.setImageBitmap(bm);
        starAlignSplitContainer.addView(imgView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void initGraffitiView(Bitmap originBitmap, String baseImgPath) {

        // 先初始化抠图的必备信息 在Async中完成了 oriImgMat 的加载
        initGrabCut(oriImgMat.getNativeObjAddr(), resImgMat.getNativeObjAddr(), maskImgMat.getNativeObjAddr());

        starAlignSplitContainer = findViewById(R.id.star_align_split_container);
        graffitiView = new GraffitiView(StarAlignSplitActivity.this, originBitmap,
                baseImgPath, true, new GraffitiView.GraffitiListener() {
            @Override
            public void onSaved(Bitmap bitmap, Bitmap bitmapEraser) {}

            @Override
            public void onError(int i, String msg) {}

            @Override
            public void onReady() {}

            @Override
            public boolean onTouchEvent(View v, MotionEvent event, int imgX, int imgY) {
                if (isReadySplit) {
                    // 当前已经合适进行图片分割操作
                    Log.d("TagImg", imgX + "\t" + imgY);
                    switch(event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            GRABCUT_TOUCH_EVENT = 0;
                            moveGrabCut(GRABCUT_TOUCH_EVENT, imgX, imgY, OPERATE_FLAG, LAST_BOUNDARY_X, LAST_BOUNDARY_Y);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            GRABCUT_TOUCH_EVENT = 2;
                            moveGrabCut(GRABCUT_TOUCH_EVENT, imgX, imgY, OPERATE_FLAG, LAST_BOUNDARY_X, LAST_BOUNDARY_Y);
                            break;
                        case MotionEvent.ACTION_UP:
                            GRABCUT_TOUCH_EVENT = 1;
                            moveGrabCut(GRABCUT_TOUCH_EVENT, imgX, imgY, OPERATE_FLAG, LAST_BOUNDARY_X, LAST_BOUNDARY_Y);
                            break;
                        default:
                            break;
                    }
                    if (OPERATE_FLAG == 3) {
                        LAST_BOUNDARY_X = imgX;
                        LAST_BOUNDARY_Y = imgY;
                    }
                }
                return isReadySplit;
            }
        });

        graffitiView.setPen(GraffitiView.Pen.HAND);
        graffitiView.setColor(splitDrawLineColor);  // 设置画图页面分割线初始的颜色信息
        starAlignSplitContainer.addView(graffitiView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        // 2. 绑定绘图需要的事件
        // 2.3 设置划分界限的的线条
        findViewById(R.id.btn_star_ground_boundary).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isReadySplit = true;
                graffitiView.setShape(GraffitiView.Shape.HAND_WRITE);
                OPERATE_FLAG = 3;  // 分界线
            }
        });

        // 2.4 设置清屏模式
        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                graffitiView.setColor(Color.TRANSPARENT);  // 没有设置的时候不进行操作
                graffitiView.clear();
                reset();  // 清除mask中已经划定的前景 背景信息
            }
        });

        // 2.5 设置开始显示前景图像
        findViewById(R.id.btn_star_grab_cut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isReadySplit) {
                    // 没有设置分割线的情况下
                    Toast.makeText(StarAlignSplitActivity.this, "请首先设置分割线", Toast.LENGTH_SHORT).show();
                    return;
                }

                SweetAlertDialog tipDialog = new SweetAlertDialog(StarAlignSplitActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                tipDialog.getProgressHelper().setBarColor(ThemeHelper.getPrimaryColor(StarAlignSplitActivity.this));
                tipDialog.setTitleText("正在分割图片");
                tipDialog.setCancelable(false);
                tipDialog.show();

                // 获得maskImgPath的路径
                maskImgPath = FileUtils.generateTempImgAbsPath(StarAlignSplitActivity.this);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (saveMaskMat(maskImgPath)) {
                            // 存储获取的mask图像
//                            Bitmap bm = Bitmap.createBitmap(maskImgMat.cols(), maskImgMat.rows(), Bitmap.Config.RGB_565);
//                            Utils.matToBitmap(maskImgMat, bm);
//                            maskImgPath = FileUtils.generateTempImgAbsPath(StarAlignSplitActivity.this);
//                            FileUtils.saveImgBitmap(maskImgPath, bm);

                            Log.i("maskImgPath", "run: " + maskImgPath);

                            // 更新界面信息
                            StarAlignSplitActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    grabCutOver();
                                    tipDialog.dismiss();
                                    // 返回上一个Activity，并且传回mask路径
                                    Intent intent = new Intent();
                                    intent.putExtra(EXTRA_MASK_IMG_PATH, maskImgPath);
                                    setResult(Activity.RESULT_OK, intent);
                                    finish();
                                }
                            });
                        }
                    }
                }).start();
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
        BubbleSeekBar paintSizeBar = findViewById(R.id.paint_size);
        paintSizeBar.getConfigBuilder()
                .min(0)
                .max(100)
                .showSectionText()
                .sectionTextColor(getResources().getColor(R.color.icongrey))
                .build();
        paintSizeBar.setProgress((int) graffitiView.getPaintSize());
        paintSizeBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                graffitiView.setPaintSize(progress);
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class LoadGraffitiAsyncTask extends AsyncTask<Void, Integer, Void> {

        private Bitmap originBitmap;
        private String baseImgPath;
        private SweetAlertDialog tipDialog;

        public LoadGraffitiAsyncTask(String baseImgPath) {
            this.baseImgPath = baseImgPath;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // 弹出加载图片的对话框提示
            tipDialog = new SweetAlertDialog(StarAlignSplitActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            tipDialog.getProgressHelper().setBarColor(ThemeHelper.getPrimaryColor(StarAlignSplitActivity.this));
            tipDialog.setTitleText(StarAlignSplitActivity.this.getResources().getString(R.string.loading));
            tipDialog.setCancelable(false);
            tipDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
//            originBitmap = ImageUtils.createBitmapFromPath(baseImgPath, StarAlignSplitActivity.this);
            // 将获取的bit 存入 oriImgMat 中
            originBitmap = BitmapFactory.decodeFile(baseImgPath);
            Utils.bitmapToMat(originBitmap, oriImgMat);
            // 这个函数的意义 CV_8UC4 -> CV_8UC3
            Imgproc.cvtColor(oriImgMat, oriImgMat, Imgproc.COLOR_RGBA2RGB);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // 界面操作
            initGraffitiView(originBitmap, baseImgPath);
            // 关闭提示的对话框
            tipDialog.dismiss();
        }
    }
}
