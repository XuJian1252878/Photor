package com.photor.base;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.photor.R;
import com.photor.album.activity.TrashBinActivity;
import com.photor.base.View.MainAcitvityViewPager;
import com.photor.base.activity.BaseActivity;
import com.photor.base.adapters.MainViewPagerAdapter;
import com.photor.base.fragment.AlbumFragment;
import com.photor.base.fragment.util.BottomNavigationEnum;
import com.photor.base.fragment.util.FragmentDataGenerator;

import org.opencv.android.OpenCVLoader;

import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

import static com.example.constant.PhotoOperator.REQUEST_ACTION_CHART_LET;

public class MainActivity extends BaseActivity {

    private BottomNavigationViewEx mBottomNavigationView;
    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private MainAcitvityViewPager mMainViewPager; // 主页面的ViewPager
    private int previousBtmNavItemId = -1; // 上一次下部导航栏所在的item的下标
    private boolean doubleBackToExitPressedOnce = false; // 处理返回键被按下时的boolean

    // Used to load the 'native-lib' library on application startup.
    static {
        // opencv 测试
        if (!OpenCVLoader.initDebug()) {
            Log.e("MainActivity Jni", "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d("MainActivity Jni", "  OpenCVLoader.initDebug(), working.");
            System.loadLibrary("native-lib");
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI(); // 初始化MainActivity的UI信息
        // Jni 测试
        Log.d("jni output", "Jni output: " + stringFromJNI());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 加载位于屏幕右上角的菜单信息（右上角的三竖点）
        getMenuInflater().inflate(R.menu.top_tool_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                // HomeAsUp按钮的id永远是R.id.home
                // mDrawerLayout.openDrawer(GravityCompat.START);
                // openDrawer 将滑动菜单显示出来，GravityCompat.START指明滑动菜单的滑动形式
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.backup:
                Toast.makeText(this, "You click backup menu item", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.delete:
                Toast.makeText(this, "You click delete menu item", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.settings:
                Toast.makeText(this, "You click settings menu item", Toast.LENGTH_SHORT).show();
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * 初始化MainActivity的UI信息
     */
    private void initUI() {

        // 1. 初始化action bar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 获得当前ActionBar的实例
        ActionBar actionBar = getSupportActionBar();  // 当前ActionBar已经跟Toolbar结合到一起了
        if (actionBar != null) {
            // 让Activity中的导航栏按钮显示出来
            actionBar.setDisplayHomeAsUpEnabled(true);
            // 设置导航栏按钮图标
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        // 2. 初始化左侧导航栏
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.left_main_nav_view);
        // 设置默认选中
        navView.setCheckedItem(R.id.nav_call);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.ll_drawer_trashbin:
                        // 点击了回收站的按钮
                        Intent intent = new Intent(MainActivity.this, TrashBinActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }

                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        mBottomNavigationView = findViewById(R.id.bottom_main_navigation);
        // 开始或关闭子菜单位移模式。 如果为 true，除了当前选中项，其他项的文本将会隐藏。 当菜单数大于3时，默认为 true。
        mBottomNavigationView.enableItemShiftingMode(false);
        mMainViewPager = findViewById(R.id.main_container);

        // 3. 初始化底部导航栏
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationViewEx.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                onBottomNavigationItemSelected(item.getItemId());
                return true;
            }
        });

        // 测试BottomNavigationView的小气泡功能
        // add badge
        addBadgeAt(2, 1);

        // 4. 设置主页面的ViewPager
        mMainViewPager.setAdapter(new MainViewPagerAdapter(getSupportFragmentManager()));

        mMainViewPager.addOnPageChangeListener(new MainAcitvityViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                // ViewPager和BottomNavigationView联动绑定
                mBottomNavigationView.setCurrentItem(position);
                AlbumFragment fragment = (AlbumFragment) FragmentDataGenerator.FRAGMENTS[BottomNavigationEnum.GALLERY.getNavItemIndex()];
                if (position == BottomNavigationEnum.CAMERA.getNavItemIndex()) {
                    mMainViewPager.setCanScroll(false); // 到Camera Fragment的时候禁止ViewPager 的滑动
                    toolbar.setVisibility(View.GONE); // 隐藏导航栏
                    fragment.gotoInitStatus();  // 将相册界面初始化，防止逻辑混乱
                } else if (position == BottomNavigationEnum.GALLERY.getNavItemIndex()) {
                    mMainViewPager.setCanScroll(true);
                    toolbar.setVisibility(View.VISIBLE); // 恢复显示导航栏
                } else if (position == BottomNavigationEnum.HOME.getNavItemIndex()) {
                    mMainViewPager.setCanScroll(true);
                    toolbar.setVisibility(View.VISIBLE); // 恢复显示导航栏
                    fragment.gotoInitStatus();  // 将相册界面初始化，防止逻辑混乱
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // 由于第一次进来没有回调onNavigationItemSelected，因此需要手动调用一下切换状态的方法
        onBottomNavigationItemSelected(R.id.menu_main_bottom_tab_gallery);  // 应用进入时默认选择相册选项卡

        // 5. 初始化浮动小圆点
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Snackbar.make 的view参数只要是当前界面的任意一个布局就可以，他会使用这个View自动查找最外层的布局
//                Snackbar.make(findViewById(R.id.main_coordinatorLayout), "Data Deleted", Snackbar.LENGTH_SHORT)
//                        .setAction("Undo", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                Toast.makeText(MainActivity.this, "Data Restored", Toast.LENGTH_SHORT).show();
//                            }
//                        }).show();
//
//            }
//        });
    }

    /**
     * 根据当前的fragment tab项显示定了个步导航栏信息
     */
    public void changeToolbarStatus() {
        if (mMainViewPager.getCurrentItem() == BottomNavigationEnum.CAMERA.getNavItemIndex()) {
            toolbar.setVisibility(View.GONE);
        } else {
            toolbar.setVisibility(View.VISIBLE);
        }
    }

    // ViewPager和BottomNavigationView联动绑定
    private void onBottomNavigationItemSelected(int id) {
        if (previousBtmNavItemId != id) {
            // only set item when item changed
            previousBtmNavItemId = id;
            mMainViewPager.setCurrentItem(BottomNavigationEnum.findNavIndexById(id));
        }
    }


    private Badge addBadgeAt(int position, int number) {
        // add badge
        return new QBadgeView(this)
                .setBadgeNumber(number)
                .setGravityOffset(35, 0, true)
                .bindTarget(mBottomNavigationView.getBottomNavigationItemView(position))
                .setOnDragStateChangedListener(new Badge.OnDragStateChangedListener() {
                    @Override
                    public void onDragStateChanged(int dragState, Badge badge, View targetView) {
                        if (Badge.OnDragStateChangedListener.STATE_SUCCEED == dragState)
                            Toast.makeText(MainActivity.this, "Badge test", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 当返回键按下的时候
    @Override
    public void onBackPressed() {
        int curBottomNavIndex = mMainViewPager.getCurrentItem();
        if (curBottomNavIndex == BottomNavigationEnum.GALLERY.getNavItemIndex()) {
            // 是相册导航栏的返回键信息
            AlbumFragment albumFragment = (AlbumFragment) FragmentDataGenerator.FRAGMENTS[curBottomNavIndex];
            // 设置backPress的点击信息
            albumFragment.onAlbumBackPress();
        } else {
            doubleBackToExitPressedOnce();
        }
    }

    /**
     * 再点击一次返回键退出程序
     */
    public void doubleBackToExitPressedOnce() {
        if (doubleBackToExitPressedOnce && isTaskRoot()) {
            finish();
        } else if (isTaskRoot()) {
            doubleBackToExitPressedOnce = true;
            // root view of your activity
            View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar
                    .make(rootView, R.string.press_back_again_to_exit, Snackbar.LENGTH_LONG)
                    .setAction(R.string.exit, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // 关闭当前activity所在栈中的所有的activity
                            finishAffinity();
                        }
                    })
                    .setActionTextColor(com.example.theme.ThemeHelper.getAccentColor(this));
            // 设置Snackbar显示在底部导航栏之上
            View sbView = snackbar.getView();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)sbView.getLayoutParams();
            params.setMargins(
                    params.leftMargin,
                    params.topMargin,
                    params.rightMargin,
                    params.bottomMargin + mBottomNavigationView.getHeight()
            );
            sbView.setLayoutParams(params);
            snackbar.show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        } else {
            // no
            onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            int curBottomNavIndex = mMainViewPager.getCurrentItem();
            switch (requestCode) {
                case REQUEST_ACTION_CHART_LET:
                    if (curBottomNavIndex == BottomNavigationEnum.GALLERY.getNavItemIndex()) {
                        // 是相册导航栏的返回键信息
                        AlbumFragment albumFragment = (AlbumFragment) FragmentDataGenerator.FRAGMENTS[curBottomNavIndex];
                        // 保存已经编辑好的相册贴图信息
                        albumFragment.handleImageAfterEditor(data);
                    }
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
