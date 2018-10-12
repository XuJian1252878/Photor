package com.photor.album.activity;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
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
import com.photor.album.entity.Media;
import com.photor.album.utils.Measure;
import com.photor.album.views.PagerRecyclerView;
import com.photor.base.activity.BaseActivity;
import com.photor.base.activity.PhotoExifDetailActivity;
import com.photor.base.fragment.AlbumFragment;
import com.photor.data.TrashBinRealmModel;
import com.photor.util.ActivitySwitchHelper;
import com.photor.util.AlertDialogsHelper;
import com.photor.util.BasicCallBack;
import com.photor.util.ColorPalette;
import com.photor.util.SnackBarHandler;
import com.photor.util.ThemeHelper;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.realm.Realm;

import static com.photor.base.activity.PhotoOperateResultActivity.EXTRA_IS_SAVED_CROP_RES;
import static com.photor.base.activity.PhotoOperateResultActivity.EXTRA_ORI_IMG_PATH;
import static com.photor.util.ActivitySwitchHelper.getContext;

public class SingleMediaActivity extends BaseActivity implements ImageAdapter.OnSingleTap, ImageAdapter.EnterTransition {

    public static final String ACTION_OPEN_ALBUM = "android.intent.action.pagerAlbumMedia";
    private static final String ACTION_REVIEW = "com.android.camera.action.REVIEW";

    private PreferenceUtil SP;
    private static int SLIDE_SHOW_INTERVAL = 5000;  // 控制幻灯片播放的时长

    private RelativeLayout relativeLayout, ActivityBackground;

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

    // 数据库实例
    private Realm realm;

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

    @Nullable
    @BindView(R.id.PhotoPager_Layout)
    View parentView;

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
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
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
            case R.id.action_delete:
                handler.removeCallbacks(slideShowRunnable);
                deleteAction();
                return true;
            case R.id.action_details:
                handler.removeCallbacks(slideShowRunnable);
                Intent intent = new Intent(SingleMediaActivity.this, PhotoExifDetailActivity.class);
                intent.putExtra(EXTRA_ORI_IMG_PATH, pathForDescription);  // 设置原图的路径
                intent.putExtra(EXTRA_IS_SAVED_CROP_RES, false);  // 说明不是经过裁剪之后的图片
                startActivity(intent);
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

    private void deleteAction() {
        final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(SingleMediaActivity.this, R.style.AlertDialog_Light);
        AlertDialogsHelper.getTextCheckboxDialog(SingleMediaActivity.this,
                deleteDialog,
                R.string.delete,
                R.string.delete_photo_message,
                null,
                "移至回收站",
                ThemeHelper.getAccentColor(this));
        String buttonDelete = getString(R.string.delete);
        deleteDialog.setNegativeButton(getString(R.string.cancel), null);
        deleteDialog.setPositiveButton(buttonDelete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteCurrentMedia();
            }
        });

        AlertDialog alertDialog = deleteDialog.create();
        alertDialog.show();
        AlertDialogsHelper.setButtonTextColor(new int[]{DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE},
                ThemeHelper.getAccentColor(this),
                alertDialog);
    }

    /**
     * 将文件加入回收站
     * @return
     */
    private boolean addToTrash(){
        String pathOld = null;
        String oldpath = null;
        int no = 0;
        boolean succ = false;
        if(!allPhotoMode){
            // 某一个相册下的照片
            oldpath = getAlbum().getCurrentMedia().getPath();
        } else {
            // 全部照片模式下的照片信息
            oldpath = AlbumFragment.listAll.get(current_image_pos).getPath();
        }
        File file = new File(Environment.getExternalStorageDirectory() + "/" + ".nomedia");

        if (file.exists() && file.isDirectory()) {
            if (!allPhotoMode) {
                pathOld = getAlbum().getCurrentMedia().getPath();
                succ = getAlbum().moveCurrentMedia(getApplicationContext(), file.getAbsolutePath());
            } else {
                // 全部照片模式
                pathOld = AlbumFragment.listAll.get(current_image_pos).getPath();
                succ = getAlbum().moveAnyMedia(getApplicationContext(), file.getAbsolutePath(), AlbumFragment.listAll.get
                        (current_image_pos).getPath());
            }
            if (succ) {
                Snackbar snackbar = SnackBarHandler.showWithBottomMargin2(parentView, getString(R.string
                                .trashbin_move_onefile),
                        bottomBar.getHeight(), Snackbar.LENGTH_SHORT);
                final String finalOldpath = oldpath;
                snackbar.setAction("撤销", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeTrashObjectFromRealm(finalOldpath);
                        getAlbum().moveAnyMedia(getApplicationContext(), getAlbum().getPath(), finalOldpath);
                    }
                });
                snackbar.show();
            } else {
                SnackBarHandler.showWithBottomMargin(parentView, String.valueOf(no) + " " + getString(R.string
                                .trashbin_move_error),
                        bottomBar.getHeight());
            }
        } else {
            if (file.mkdir()) {
                if (!allPhotoMode) {
                    pathOld = getAlbum().getCurrentMedia().getPath();
                    succ = getAlbum().moveCurrentMedia(getApplicationContext(), file.getAbsolutePath());
                } else {
                    pathOld = getAlbum().getCurrentMedia().getPath();
                    succ = getAlbum().moveAnyMedia(getApplicationContext(), file.getAbsolutePath(), AlbumFragment.listAll.get
                            (current_image_pos).getPath());
                }
                if (succ) {
                    SnackBarHandler.showWithBottomMargin(parentView, String.valueOf(no) + " " + getString(R.string
                                    .trashbin_move_onefile), bottomBar.getHeight());
                } else {
                    SnackBarHandler.showWithBottomMargin(parentView, String.valueOf(no) + " " + getString(R.string
                                    .trashbin_move_error),
                            bottomBar.getHeight());
                }
            }
        }
        addTrashObjectsToRealm(pathOld);
        return succ;
    }

    /**
     * 将被删除的图片写入 realm 数据库
     */
    private void addTrashObjectsToRealm(String mediaPath) {
        String trashbinpath = Environment.getExternalStorageDirectory() + "/" + ".nomedia";
        Realm.init(getContext());
        realm = Realm.getDefaultInstance();

        int index = mediaPath.lastIndexOf("/");
        String name = mediaPath.substring(index + 1);
        String trashpath = trashbinpath + "/" + name;

        // 首先检查当前文件是否已经存在与垃圾箱中（否则会出现主键冲突）
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                TrashBinRealmModel model = realm.where(TrashBinRealmModel.class)
                        .equalTo("trashbinpath", trashpath).findFirst();
                if (model != null) {
                    model.deleteFromRealm();
                }
            }
        });

        realm.beginTransaction();
        TrashBinRealmModel trashBinRealmModel = realm.createObject(TrashBinRealmModel.class, trashpath);
        trashBinRealmModel.setOldpath(mediaPath);
        trashBinRealmModel.setDatetime(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        trashBinRealmModel.setTimeperiod("null");
        realm.commitTransaction();
    }

    /**
     * 删除回收站中的照片信息
     */
    private void removeTrashObjectFromRealm(String mediaPath) {
        String trashbinpath = Environment.getExternalStorageDirectory() + "/" + ".nomedia";
        Realm.init(getContext());
        realm = Realm.getDefaultInstance();

        int index = mediaPath.lastIndexOf("/");
        String name = mediaPath.substring(index + 1);
        String trashpath = trashbinpath + "/" + name;

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                TrashBinRealmModel model = realm.where(TrashBinRealmModel.class)
                        .equalTo("trashbinpath", trashpath).findFirst();
                if (model != null) {
                    model.deleteFromRealm();
                }
            }
        });
    }


    private void deleteCurrentMedia() {
        boolean success = false;
        if (!allPhotoMode) {
            // 某一个相册下显示的图片信息
            if (AlertDialogsHelper.check) {
                success = addToTrash();  // 删除至回收站的操作
            } else {
                success = getAlbum().deleteCurrentMedia(getApplicationContext());
            }

            if (!success) {
                // 申请SD卡权限
                requestSdCardPermissions();
            }

            if (getAlbum().getMedias().size() == 0) {
                if (customUri) {
                    finish();
                } else {
                    getAlbums().removeCurrentAlbum();
                    finish();
                }
            }
            adapter.notifyDataSetChanged();
            getSupportActionBar().setTitle((getAlbum().getCurrentMediaIndex() + 1) + " / " + getAlbum().getMedias().size());
        } else {
            // 以全部的模式显示照片
            int c = all_photo_pos;
            if (AlertDialogsHelper.check) {
                success = addToTrash();  // 收入回收站
            } else {
                deleteMedia(AlbumFragment.listAll.get(all_photo_pos).getPath());
                success = true;
            }

            if (success) {
                AlbumFragment.listAll.remove(all_photo_pos);
                size_all = AlbumFragment.listAll.size();
                adapter.notifyDataSetChanged();
            }

            if (current_image_pos != size_all) {
                getSupportActionBar().setTitle((c + 1) + " / " + size_all);
            }
        }
    }

    private Disposable requestSdCardPermissions() {
        Disposable disposable = new RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) { // Always true pre-M
                        // I can control the camera now
                        SnackBarHandler.show(bottomBar, "权限申请成功", Snackbar.LENGTH_SHORT);
                    } else {
                        // Oups permission denied
                        SnackBarHandler.show(bottomBar, "权限申请失败", Snackbar.LENGTH_SHORT);
                    }
                });
        return disposable;
    }

    private void deleteMedia(String path) {
        String[] projection = {MediaStore.Images.Media._ID};

        // Match on the file path
        String selection = MediaStore.Images.Media.DATA + " = ?";
        String[] selectionArgs = new String[]{path};

        // Query for the ID of the media matching the file path
        Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = getContentResolver();
        Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
        if (c.moveToFirst()) {
            // We found the ID. Deleting the item via the content provider will also remove the file
            long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            contentResolver.delete(deleteUri, null, null);
        }
        c.close();
    }
}
