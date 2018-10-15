package com.photor.base;

import android.Manifest;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.theme.ThemeHelper;
import com.photor.MainApplication;
import com.photor.R;
import com.photor.album.entity.Album;
import com.photor.album.entity.HandlingAlbums;
import com.photor.base.activity.BaseActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.bgabanner.BGABanner;
import cn.bingoogolapple.bgabanner.BGALocalImageSize;
import io.reactivex.disposables.Disposable;

/**
 * @author htwxujian@gmail.com
 * @date 2018/10/15 15:26
 */
public class SplashActivity extends BaseActivity {

    @BindView(R.id.splash_bg)
    protected RelativeLayout splashBg;
//    @BindView(R.id.banner_guide_background)
//    protected BGABanner mBackgroundBanner;
//    @BindView(R.id.banner_guide_foreground)
//    protected BGABanner mForegroundBanner;

    private Intent nextIntent = null;  // 指定跳转到MainActivity的Intent

    // 当前被选中的album信息
    private Album album;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE  // 稳定布局，主要是在全屏和非全屏切换时，布局不要有大的变化。一般和View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN、View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION搭配使用。
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION  // 将布局内容拓展到导航栏的后面
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);  // 将布局内容拓展到状态的后面


        // 设置欢迎页面的背景颜色
        splashBg.setBackgroundColor(ThemeHelper.getPrimaryColor(this));

        // 进行权限申请已经数据初始化操作
        requestPermission(new OnAppPermissionGranted() {
            @Override
            public void onPermissionGranted() {
                // 当App的权限都被赋予的时候，加载手机内部的相册数据
                new SplashActivity.PrefetchAlbumsData().execute();
            }
        });

//        setUpGuideBanner();
    }

//    private void setUpGuideBanner() {
//        // 设置启动动画信息
//        mForegroundBanner.setEnterSkipViewIdAndDelegate(R.id.btn_guide_enter, R.id.tv_guide_skip, new BGABanner.GuideDelegate() {
//            @Override
//            public void onClickEnterOrSkip() {
//                startActivity(new Intent(SplashActivity.this, MainActivity.class));
//                finish();
//            }
//        });
//
//        // Bitmap 的宽高在 maxWidth maxHeight 和 minWidth minHeight 之间
//        BGALocalImageSize localImageSize = new BGALocalImageSize(720, 1280, 320, 640);
//        // 设置数据源
//        mBackgroundBanner.setData(localImageSize, ImageView.ScaleType.CENTER_CROP,
//                R.drawable.uoko_guide_background_1,
//                R.drawable.uoko_guide_background_2,
//                R.drawable.uoko_guide_background_3);
//
//        mForegroundBanner.setData(localImageSize, ImageView.ScaleType.CENTER_CROP,
//                R.drawable.uoko_guide_foreground_1,
//                R.drawable.uoko_guide_foreground_2,
//                R.drawable.uoko_guide_foreground_3);
//    }


    @Override
    protected void onResume() {
        super.onResume();

        // 如果开发者的引导页主题是透明的，需要在界面可见时给背景 Banner 设置一个白色背景，避免滑动过程中两个 Banner 都设置透明度后能看到 Launcher
//        mBackgroundBanner.setBackgroundResource(android.R.color.white);
    }

    /**
     * app 权限都申请到的时候，将要进行的操作
     */
    private interface OnAppPermissionGranted {
        public abstract void onPermissionGranted();
    }


    /**
     * 检查应用的权限开启信息
     * @return
     */
    private boolean requestPermission(OnAppPermissionGranted onAppPermissionGranted) {
        Disposable disposable = new RxPermissions(this).request(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(granted -> { // will emit 2 Permission objects
                    if (granted) {
                        // All requested permissions are granted
                        // 初始化加载相册信息
                        onAppPermissionGranted.onPermissionGranted();
                    } else {
                        // At least one permission is denied
                    }
                });
        return disposable.isDisposed();
    }


    // 加载相册信息
    private class PrefetchAlbumsData extends AsyncTask<Boolean, Boolean, Boolean> {
        HandlingAlbums albums = ((MainApplication) SplashActivity.this.getApplication()).getAlbums();
        @Override
        protected Boolean doInBackground(Boolean... booleans) {
            albums.restoreBackup(SplashActivity.this);
            if (albums.dispAlbums.size() == 0) {
                // 加载全部的相册信息
                albums.loadAlbums(SplashActivity.this.getApplicationContext(), false);
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            nextIntent = new Intent(SplashActivity.this, MainActivity.class);
            Bundle args = new Bundle();
            nextIntent.putExtras(args);
            startActivity(nextIntent);  // 相册数据加载完成之后进入主界面
            finish();

            if (result) {
                albums.saveBackup(getApplicationContext());
            }
        }
    }

    // 加载照片信息
    private class PrefetchPhotosData extends AsyncTask<Void, Void, Void> {
        HandlingAlbums albums = ((MainApplication) SplashActivity.this.getApplication()).getAlbums();
        @Override
        protected Void doInBackground(Void... voids) {
            album.updatePhotos(SplashActivity.this);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            albums.addAlbum(0, album);

            nextIntent = new Intent(SplashActivity.this, MainActivity.class);
            Bundle args = new Bundle();
            nextIntent.putExtras(args);
            startActivity(nextIntent);
            finish();
        }
    }
}
