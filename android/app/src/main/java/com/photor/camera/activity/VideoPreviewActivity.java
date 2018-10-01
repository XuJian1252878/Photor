package com.photor.camera.activity;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.photor.R;
import com.photor.camera.view.JzvdStdAutoCompleteAfterFullscreen;

import cn.jzvd.JZDataSource;
import cn.jzvd.JZMediaManager;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

import static cn.jzvd.Jzvd.SCREEN_WINDOW_FULLSCREEN;


public class VideoPreviewActivity extends Activity {

    private JzvdStdAutoCompleteAfterFullscreen videoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);
        Uri videoUri = getIntent().getParcelableExtra("video");

        videoView = findViewById(R.id.video_record_preview);
        // 设置视频播放路径
        videoView.setUp(videoUri.toString(), getResources().getString(R.string.camera_video_record_preview), Jzvd.SCREEN_WINDOW_NORMAL);
        // 设置缩略图
        Glide.with(this).load(R.drawable.ic_video_preview_on_black).into(videoView.thumbImageView);


//        videoView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                playVideo();
//            }
//        });

//        MediaController controller = new MediaController(this);
//        controller.setAnchorView(videoView);
//        controller.setMediaPlayer(videoView);
//        videoView.setMediaController(controller);
//        videoView.setVideoURI(videoUri);
//
//        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mp) {
////                actualResolution.setTitle("Actual resolution");
////                actualResolution.setMessage(mp.getVideoWidth() + " x " + mp.getVideoHeight());
//                ViewGroup.LayoutParams lp = videoView.getLayoutParams();
//                float videoWidth = mp.getVideoWidth();
//                float videoHeight = mp.getVideoHeight();
//                float viewWidth = videoView.getWidth();
//                lp.height = (int) (viewWidth * (videoHeight / videoWidth));
//                videoView.setLayoutParams(lp);
//                playVideo();
//            }
//        });
    }

//    void playVideo() {
//        if (videoView.isPlaying()) return;
//        videoView.start();
//    }

    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.releaseAllVideos();
    }
}
