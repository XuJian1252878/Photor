package com.photor.base;

import android.Manifest;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.photor.MainApplication;
import com.photor.R;
import com.photor.album.entity.Album;
import com.photor.album.entity.HandlingAlbums;
import com.photor.base.View.MainAcitvityViewPager;
import com.photor.base.activity.BaseActivity;
import com.photor.base.adapters.MainViewPagerAdapter;
import com.photor.base.fragment.AlbumFragment;
import com.photor.base.fragment.util.BottomNavigationEnum;
import com.photor.base.fragment.util.FragmentDataGenerator;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.opencv.android.OpenCVLoader;

import java.util.List;

import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

public class MainActivity extends BaseActivity {

    private BottomNavigationViewEx mBottomNavigationView;
    private DrawerLayout mDrawerLayout;
    private MainAcitvityViewPager mMainViewPager; // 主页面的ViewPager
    private int previousBtmNavItemId = -1; // 上一次下部导航栏所在的item的下标

    // 当前被选中的album信息
    private Album album;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
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

        requestPermission();

        // opencv 测试
        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }

        // Jni 测试
        Log.d("jni output", "Jni output: " + stringFromJNI());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 加载位于屏幕右上角的菜单信息（右上角的三竖点）
        getMenuInflater().inflate(R.menu.top_tool_bar_menu, menu);
        return true;
    }

    /**
     * 检查应用的权限开启信息
     * @return
     */
    private boolean requestPermission() {
        Disposable disposable = new RxPermissions(this).requestEach(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(permission -> { // will emit 2 Permission objects
                    if (permission.granted) {
                        // `permission.name` is granted !
                        initUI(); // 初始化MainActivity的UI信息
                        // 初始化加载相册信息
                        new PrefetchAlbumsData().execute();
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        // Denied permission without ask never again
                    } else {
                        // Denied permission with ask never again
                        // Need to go to the settings
                    }
                });
        return disposable.isDisposed();
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
        Toolbar toolbar = findViewById(R.id.toolbar);
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
        // 由于第一次进来没有回调onNavigationItemSelected，因此需要手动调用一下切换状态的方法
        onBottomNavigationItemSelected(R.id.menu_main_bottom_tab_home);

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
                if (position == BottomNavigationEnum.CAMERA.getNavItemIndex()) {
                    mMainViewPager.setCanScroll(false); // 到Camera Fragment的时候禁止ViewPager 的滑动
                } else {
                    mMainViewPager.setCanScroll(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

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

    // 加载相册信息
    private class PrefetchAlbumsData extends AsyncTask<Boolean, Boolean, Boolean> {
        HandlingAlbums albums = ((MainApplication) MainActivity.this.getApplication()).getAlbums();
        @Override
        protected Boolean doInBackground(Boolean... booleans) {
            albums.restoreBackup(MainActivity.this);
            if (albums.dispAlbums.size() == 0) {
                // 加载全部的相册信息
                albums.loadAlbums(MainActivity.this.getApplicationContext(), false);
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                albums.saveBackup(getApplicationContext());
            }
            super.onPostExecute(result);
        }
    }

    // 加载照片信息
    private class PrefetchPhotosData extends AsyncTask<Void, Void, Void> {
        HandlingAlbums albums = ((MainApplication) MainActivity.this.getApplication()).getAlbums();
        @Override
        protected Void doInBackground(Void... voids) {
            album.updatePhotos(MainActivity.this);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //
            albums.addAlbum(0, album);
        }
    }
}
