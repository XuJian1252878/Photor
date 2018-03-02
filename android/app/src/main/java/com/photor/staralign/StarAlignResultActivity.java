package com.photor.staralign;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.photor.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class StarAlignResultActivity extends AppCompatActivity {

    private String alignResImgPath = null;
    private ImageView alignResImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star_align_result);

        alignResImgPath = getIntent().getStringExtra(StarAlignSetting.EXTRA_ALIGN_RESULT_PATH);
        alignResImageView = findViewById(R.id.star_align_result_iv);

        // todo: 要根据用户的选择决定是否保存图片，若是保存，要将图片索引，不保存则要删除图片
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(alignResImgPath);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            alignResImageView.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


}
