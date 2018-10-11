package com.photor.album.activity;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.ViewSwitcher;

import com.example.file.FileUtils;
import com.example.preference.PreferenceUtil;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.photor.R;
import com.photor.album.entity.Album;
import com.photor.album.utils.Measure;
import com.photor.album.views.PagerRecyclerView;
import com.photor.base.activity.BaseActivity;
import com.photor.util.ActivitySwitchHelper;
import com.photor.util.ThemeHelper;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SingleMediaActivity extends BaseActivity {

    public static final String ACTION_OPEN_ALBUM = "android.intent.action.pagerAlbumMedia";
    private static final String ACTION_REVIEW = "com.android.camera.action.REVIEW";

    private PreferenceUtil SP;

    private CoordinatorLayout relativeLayout, ActivityBackground;

    public Boolean allPhotoMode;  // 是否由全部照片模式跳转而来的
    public int all_photo_pos;  // 全部照片模式下，照片的当前位置信息
    public int size_all;  // 全部照片模式下，照片的总数量

    public static String pathForDescription;  // 相册模式下传递过来的照片路径
    private boolean customUri = false;  // 图片是否是以URI形式传递来的
    private boolean fullScreenMode = false; // 当前是否以全屏模式来显示照片

    private Handler handler;
    private Runnable runnable;

    @Nullable
    @BindView(R.id.view_switcher_single_media)
    ViewSwitcher viewSwitcher;

    @Nullable
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Nullable
    @BindView(R.id.photos_pager)
    PagerRecyclerView mViewPager;

    @Nullable
    @BindView(R.id.toolbar_bottom)
    ActionMenuView bottomBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);
        ButterKnife.bind(this);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                hideSystemUI();
            }
        };
        startHandler();  // 5秒时候会自动隐藏系统界面

        ActivityBackground = relativeLayout = findViewById(R.id.PhotoPager_Layout);
        SP = PreferenceUtil.getInstance(getApplicationContext());

        allPhotoMode = getIntent().getBooleanExtra(getString(R.string.all_photo_mode), false);
        all_photo_pos = getIntent().getIntExtra(getString(R.string.position), 0);
        size_all = getIntent().getIntExtra(getString(R.string.allMediaSize), getAlbum().getCount());

        pathForDescription = getIntent().getStringExtra("path");

        try {
            Album album;
            if (getIntent().getAction().equals(Intent.ACTION_VIEW) || getIntent().getAction().equals(ACTION_REVIEW) || getIntent().getData() != null) {
                // 从其他应用传递过来的图片
                String path = FileUtils.getMediaPath(getApplicationContext(), getIntent().getData());
                pathForDescription = path;
                File file = null;
                if (path != null) {
                    file = new File(path);
                }
                if (file != null && file.isFile()) {
                    // 图片在本地路径上
                    album = new Album(getApplicationContext(), file);
                } else {
                    // 图片是一个uri
                    album = new Album(getApplicationContext(), getIntent().getData());
                    customUri = true;
                }
                getAlbums().addAlbum(0, album);
            }

            setUpSwitcherAnimation();
            initUI();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * startHandler stopHandler 用来控制进入或者是退出全屏模式
     */
    private void startHandler() {
        handler.postDelayed(runnable, 5000);
    }

    private void stopHandler() {
        handler.removeCallbacks(runnable);
    }

    private void toggleSystemUI() {
        if (fullScreenMode) {
            showSystemUI();
        } else {
            hideSystemUI();
        }
    }

    private void setupSystemUI() {
        toolbar.animate().translationY(Measure.getStatusBarHeight(getResources())).setInterpolator(new DecelerateInterpolator())
                .setDuration(0).start();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void showSystemUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toolbar.animate().translationY(Measure.getStatusBarHeight(getResources())).setInterpolator(new DecelerateInterpolator())
                        .setDuration(240).start();
                bottomBar.animate().translationY(0).setInterpolator(new DecelerateInterpolator())
                        .setDuration(240).start();

                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                fullScreenMode = false;
                changeBackGroundColor();
            }
        });
    }

    /**
     * 照片全屏显示的时候，隐藏系统的UI信息
     */
    private void hideSystemUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // AccelerateInterpolator 在动画开始的地方速率改变比较慢，然后开始加速
                // -toolbar.getHeight() 起始点Y坐标最终停靠的位置
                toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator())
                        .setDuration(200).start();
                bottomBar.animate().translationY(+bottomBar.getHeight()).setInterpolator(new AccelerateInterpolator())
                        .setDuration(200).start();

                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);
                fullScreenMode = true;
                changeBackGroundColor();  // 切换背景色信息
                stopHandler();
            }
        });
    }

    /**
     * 设置照片的切换效果
     */
    private void setUpSwitcherAnimation() {
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);

        viewSwitcher.setInAnimation(in);
        viewSwitcher.setOutAnimation(out);
    }

    /**
     * 设置照片背景界面的渐变色（在切换全屏的时候）
     */
    private void changeBackGroundColor() {
        int colorTo;
        int colorFrom;
        if (fullScreenMode) {
            // 进入全屏模式
            colorFrom = ThemeHelper.getBackgroundColor(getApplicationContext());
            colorTo = (ContextCompat.getColor(SingleMediaActivity.this, R.color.md_black_1000));
        } else {
            // 退出全屏模式
            colorFrom = (ContextCompat.getColor(SingleMediaActivity.this, R.color.md_black_1000));
            colorTo = ThemeHelper.getBackgroundColor(getApplicationContext());
        }
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(240);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ActivityBackground.setBackgroundColor((Integer)valueAnimator.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }

    private void initUI() {

        final Menu bottomMenu = bottomBar.getMenu();
        getMenuInflater().inflate(R.menu.menu_bottom_view_pager, bottomMenu);

        for (int i = 0; i < bottomMenu.size(); i ++) {
            bottomMenu.getItem(i).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    stopHandler();
                    return onOptionsItemSelected(menuItem);
                }
            });
        }

        // 设置状态栏
        setSupportActionBar(toolbar);
        toolbar.bringToFront();
        toolbar.setNavigationIcon(ThemeHelper.getToolbarIcon(getApplicationContext(), CommunityMaterial.Icon.cmd_arrow_left));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // 显示界面的toolbar
        setupSystemUI();

        // 设置显示照片RecycleView的界面
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ActivitySwitchHelper.getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        mViewPager.setLayoutManager(linearLayoutManager);
        mViewPager.setHasFixedSize(true);
        mViewPager.setLongClickable(true);

        // 监听手机状态栏的变化来控制app导航栏是否显示
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            // 状态栏处于可见状态，显示导航栏
                            showSystemUI();
                        } else {
                            // 状态栏属于隐藏状态，隐藏导航栏
                            hideSystemUI();
                        }
                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
