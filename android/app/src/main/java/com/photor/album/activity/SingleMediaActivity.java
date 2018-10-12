package com.photor.album.activity;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeBounds;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.file.FileUtils;
import com.example.preference.PreferenceUtil;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.photor.BuildConfig;
import com.photor.R;
import com.photor.album.adapter.ImageAdapter;
import com.photor.album.entity.Album;
import com.photor.album.utils.Measure;
import com.photor.album.views.PagerRecyclerView;
import com.photor.base.activity.BaseActivity;
import com.photor.base.fragment.AlbumFragment;
import com.photor.util.ActivitySwitchHelper;
import com.photor.util.AlertDialogsHelper;
import com.photor.util.BasicCallBack;
import com.photor.util.ColorPalette;
import com.photor.util.ThemeHelper;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SingleMediaActivity extends BaseActivity implements ImageAdapter.OnSingleTap, ImageAdapter.EnterTransition {

    public static final String ACTION_OPEN_ALBUM = "android.intent.action.pagerAlbumMedia";
    private static final String ACTION_REVIEW = "com.android.camera.action.REVIEW";

    private PreferenceUtil SP;
    private static int SLIDE_SHOW_INTERVAL = 5000;  // 控制幻灯片播放的时长

    private CoordinatorLayout relativeLayout, ActivityBackground;

    public Boolean allPhotoMode;  // 是否由全部照片模式跳转而来的
    public int all_photo_pos;  // 全部照片模式下，照片的当前位置信息
    public int size_all;  // 全部照片模式下，照片的总数量

    public static String pathForDescription;  // 相册模式下传递过来的照片路径
    private boolean customUri = false;  // 图片是否是以URI形式传递来的
    private boolean fullScreenMode = false; // 当前是否以全屏模式来显示照片

    // 跟显示系统UI相关
    private Handler handler;
    private Runnable runnable;

    private ImageAdapter adapter;

    // 幻灯片播放
    private boolean slideshow = false;

    public int current_image_pos; // 记录当前图片的下标位置（在全局图片显示的模式下）

    @Nullable
    @BindView(R.id.view_switcher_single_media)
    ViewSwitcher viewSwitcher;

    @Nullable
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Nullable
    @BindView(R.id.photos_pager)
    PagerRecyclerView mViewPager;  // 图片的RecyclerView

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
            setUpUI();

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

        setUpViewPager();

        // https://cstsinghua.github.io/2018/03/26/Android%E5%B1%8F%E5%B9%95%E6%96%B9%E5%90%91/
        // 监听android 的横竖屏幕
        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        if (display.getRotation() == Surface.ROTATION_90) {
            // 当前已经处于横屏状态
            Configuration configuration = new Configuration();
            configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
            onConfigurationChanged(configuration);
        }
    }

    private void setUpUI() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(
                ColorPalette.getTransparentColor(ThemeHelper.getPrimaryColor(getApplicationContext()), 255)
        );
        toolbar.setPopupTheme(ThemeHelper.getPopupToolbarStyle(getApplicationContext()));

        ActivityBackground.setBackgroundColor(ThemeHelper.getBackgroundColor(getApplicationContext()));

    }

    /**
     * 分享图片至外部应用
     */
    private void shareToOthers() {
        Uri uri = null;
        String name = null;
        String mediaPath = null;

        if (!allPhotoMode) {
            mediaPath = getAlbum().getCurrentMedia().getPath();
            name = getAlbum().getCurrentMedia().getName();
        } else {
            mediaPath = AlbumFragment.listAll.get(current_image_pos).getPath();
            name = AlbumFragment.listAll.get(current_image_pos).getName();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(getApplicationContext(),
                    BuildConfig.APPLICATION_ID + ".provider", new File(mediaPath));
        } else {
            uri = Uri.fromFile(new File(mediaPath));
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, name);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/png");

        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_image)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            case R.id.action_share:
                handler.removeCallbacks(slideShowRunnable);
                shareToOthers();
                return true;
            case R.id.action_slideshow:
                handler.removeCallbacks(slideShowRunnable);
                setSlideShowDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void singleTap() {
        toggleSystemUI();
        // slideshow的情况
        if (slideshow) {
            handler.removeCallbacks(slideShowRunnable);
            slideshow = false;
            Toast.makeText(this, getString(R.string.slide_show_off), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void startPostponedTransition() {
        getWindow().setSharedElementEnterTransition(new ChangeBounds().setDuration(300));
        startPostponedEnterTransition();  // 启动共享元素动画
    }

    private void setUpViewPager() {
        // 控制显示UI界面的回调
        BasicCallBack basicCallBack = new BasicCallBack() {
            @Override
            public void callBack(int status, Object data) {
                toggleSystemUI();
            }
        };

        if (!allPhotoMode) {
            // 相册模式下点击某一个图片进入该界面
            adapter = new ImageAdapter(basicCallBack, getAlbum().getMedias(), this, this);
            getSupportActionBar().setTitle((getAlbum().getCurrentMediaIndex() + 1) + " / " + getAlbum().getMedias().size()); // 设置标题栏为当前图片下标
            mViewPager.setOnPageChangeListener(new PagerRecyclerView.OnPageChangeListener() {
                @Override
                public void onPageChanged(int oldPosition, int newPosition) {
                    getAlbum().setCurrentPhotoIndex(newPosition);
                    toolbar.setTitle((newPosition + 1) + " / " + getAlbum().getMedias().size());
                    invalidateOptionsMenu();
                    // 记录当前的图片文件路径
                    pathForDescription = getAlbum().getMedias().get(newPosition).getPath();
                }
            });
            // 设置RecyclerView滚动到当前的item下标
            mViewPager.scrollToPosition(getAlbum().getCurrentMediaIndex());
        } else {
            // 全部照片模式下，点击一个图片进入该界面
            adapter = new ImageAdapter(basicCallBack, AlbumFragment.listAll, this, this);
            getSupportActionBar().setTitle(all_photo_pos + " / " + size_all);
            current_image_pos = all_photo_pos;
            // OnPageChangeListener 是由OnScroller事件来触发的
            mViewPager.setOnPageChangeListener(new PagerRecyclerView.OnPageChangeListener() {
                @Override
                public void onPageChanged(int oldPosition, int newPosition) {
                    current_image_pos = newPosition;
//                    getAlbum().setCurrentPhotoIndex(getAlbum().getCurrentMediaIndex());
                    toolbar.setTitle((newPosition + 1) + " / " + size_all);
                    invalidateOptionsMenu();
                    pathForDescription = AlbumFragment.listAll.get(newPosition).getPath();
                }
            });
            mViewPager.scrollToPosition(all_photo_pos);
        }
        mViewPager.setAdapter(adapter);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.setMargins(0,0, 0,0);
        } else {
            params.setMargins(0,0,0,0);
        }

        toolbar.setLayoutParams(params);
        setUpViewPager();
    }

    private Runnable slideShowRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (!allPhotoMode) {
                    // 相册模式
                    mViewPager.scrollToPosition((getAlbum().getCurrentMediaIndex() + 1) % getAlbum().getMedias().size());
                } else {
                    // 全部照片模式
                    mViewPager.scrollToPosition((current_image_pos + 1) % AlbumFragment.listAll.size());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (getAlbum().getCurrentMediaIndex() + 1 == getAlbum().getMedias().size() - 1) {
                    handler.removeCallbacks(slideShowRunnable);
                    slideshow = false;
                    toggleSystemUI();
                } else {
                    handler.postDelayed(this, SLIDE_SHOW_INTERVAL);
                }
            }
        }
    };

    /**
     * 设置幻灯片播放
     */
    private void setSlideShowDialog() {
        final AlertDialog.Builder slideshowDialog = new AlertDialog.Builder(SingleMediaActivity.this, R.style.AlertDialog_Light);
        final View SlideshowDialogLayout = getLayoutInflater().inflate(R.layout.dialog_slideshow, null);
        final TextView slideshowDialogTitle = (TextView) SlideshowDialogLayout.findViewById(R.id.slideshow_dialog_title);
        final CardView slideshowDialogCard = (CardView) SlideshowDialogLayout.findViewById(R.id.slideshow_dialog_card);
        final EditText editTextTimeInterval = (EditText) SlideshowDialogLayout.findViewById(R.id.slideshow_edittext);

        slideshowDialogTitle.setBackgroundColor(ThemeHelper.getPrimaryColor(this));
        slideshowDialogCard.setBackgroundColor(ThemeHelper.getCardBackgroundColor(this));
        editTextTimeInterval.getBackground().mutate().setColorFilter(ThemeHelper.getTextColor(this), PorterDuff.Mode.SRC_ATOP);
        editTextTimeInterval.setTextColor(ThemeHelper.getTextColor(this));
        editTextTimeInterval.setHintTextColor(ThemeHelper.getSubTextColor(this));

        slideshowDialog.setView(SlideshowDialogLayout);
        AlertDialog dialog = slideshowDialog.create();

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok_action), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String value = editTextTimeInterval.getText().toString();
                if (!"".equals(value)) {
                    slideshow = true;
                    int intValue = Integer.valueOf(value);
                    SLIDE_SHOW_INTERVAL = intValue * 1000;
                    if (SLIDE_SHOW_INTERVAL > 1000) {
                        hideSystemUI();
                        Toast.makeText(SingleMediaActivity.this, getString(R.string.slide_show_on), Toast.LENGTH_SHORT).show();
                        handler.postDelayed(slideShowRunnable, SLIDE_SHOW_INTERVAL);
                    } else {
                        Toast.makeText(SingleMediaActivity.this, "Minimum duration is 2 sec", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
        AlertDialogsHelper.setButtonTextColor(new int[]{DialogInterface.BUTTON_POSITIVE}, ThemeHelper.getAccentColor(this), dialog);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(slideShowRunnable);
    }
}
