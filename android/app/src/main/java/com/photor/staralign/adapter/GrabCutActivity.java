package com.photor.staralign.adapter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.photor.R;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class GrabCutActivity extends AppCompatActivity {

    private long gcapp;
    private int flags = 0; // 0范围，1前景，2背景
    private Bitmap bitmap;
    private ImageView imageView;
    private Bitmap bm;
    private float s = 0;

    private Mat resImgMat = new Mat();
    private Mat oriImgMat = new Mat();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grab_cut);


        Intent intent = getIntent();
        final String baseImgPath = intent.getStringExtra("baseImgPath");

        imageView = findViewById(R.id.image_view);

        bitmap = BitmapFactory.decodeFile(baseImgPath);
        bm = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.bitmapToMat(bitmap, oriImgMat);
        Imgproc.cvtColor(oriImgMat, oriImgMat, Imgproc.COLOR_RGBA2RGB);
        gcapp = initGrabCut(oriImgMat.getNativeObjAddr(), resImgMat.getNativeObjAddr());


        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(s == 0){
                    s = imageView.getWidth()*1.0f / bitmap.getWidth();
                }
                int x = (int) (event.getX()/s);
                int y = (int) (event.getY()/s);
                int type = event.getAction();
                switch (type){
                    case MotionEvent.ACTION_DOWN:
                        moveGrabCut(0,x,y,flags,gcapp);
                        break;
                    case MotionEvent.ACTION_UP:
                        moveGrabCut(1,x,y,flags,gcapp);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        moveGrabCut(1,x,y,flags,gcapp);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        moveGrabCut(2,x,y,flags,gcapp);
                        break;
                }


                return true;
            }
        });

    }

    public void onFlags(View view){
        Button button = (Button) view;
        if("范围".equals(button.getText().toString())){
            flags = 0;
        }else if("前景".equals(button.getText().toString())){
            flags = 1;
        }else if("背景".equals(button.getText().toString())){
            flags = 2;
        }
    }
    public void onReset(View view){
        flags = 0;
        reset(gcapp);
    }
    public void onGrabCut(View view){
        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                Log.d("grabCut","开始处理");
                if(grabCut(gcapp)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            grabCutOver(gcapp);
                        }
                    });
                }
                Log.d("grabCut","结束处理");

            }
        };
        thread.start();
    }


//    public void showImage(long image){
    public void showImage(){
//        Mat oriImgMat = new Mat(image);
        Utils.matToBitmap(resImgMat, bm);
        imageView.setImageBitmap(bm);
    }


    public native long initGrabCut(long oriImgMatAddr, long resImgMatAddr);
    public native void moveGrabCut(int event, int x, int y, int flags,long gcapp);
    public native void reset(long gcapp);
    public native boolean grabCut(long gcapp);
    public native void grabCutOver(long gcapp);
}
