package com.photor;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.photor.fragment.util.BottomNavigationEnum;
import com.photor.fragment.util.FragmentDataGenerator;

public class MainActivity extends AppCompatActivity {

    private Fragment[] mMainFragments;
    private BottomNavigationView mBottomNavigationView;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI(); // 初始化MainActivity的UI信息
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 加载位于屏幕右上角的菜单信息（右上角的三竖点）
        getMenuInflater().inflate(R.menu.top_tool_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                // HomeAsUp按钮的id永远是R.id.home
                // mDrawerLayout.openDrawer(GravityCompat.START);
                // openDrawer 将滑动菜单显示出来，GravityCompat.START指明滑动菜单的滑动形式
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.backup:
                Toast.makeText(this, "You click backup menu item", Toast.LENGTH_SHORT).show();
                break;
            case R.id.delete:
                Toast.makeText(this, "You click delete menu item", Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings:
                Toast.makeText(this, "You click settings menu item", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return true;
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

        // 3. 初始化底部导航栏
        mMainFragments = FragmentDataGenerator.getMainFragments(getSupportFragmentManager());
        mBottomNavigationView = findViewById(R.id.bottom_main_navigation);

        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                onBottomNavigationItemSelected(item.getItemId());
                return true;
            }
        });
        // 由于第一次进来没有回调onNavigationItemSelected，因此需要手动调用一下切换状态的方法
        onBottomNavigationItemSelected(R.id.menu_main_bottom_tab_home);

        // 4. 初始化浮动小圆点
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Snackbar.make 的view参数只要是当前界面的任意一个布局就可以，他会使用这个View自动查找最外层的布局
                Snackbar.make(findViewById(R.id.main_coordinatorLayout), "Data Deleted", Snackbar.LENGTH_SHORT)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(MainActivity.this, "Data Restored", Toast.LENGTH_SHORT).show();
                            }
                        }).show();

            }
        });
    }


    private void onBottomNavigationItemSelected(int id) {
        Fragment fragment = null;

        switch (id) {
            case R.id.menu_main_bottom_tab_home:
                fragment = mMainFragments[BottomNavigationEnum.HOME.getNavigationItemIndex()];
                break;
            case R.id.menu_main_bottom_tab_gallery:
                fragment = mMainFragments[BottomNavigationEnum.GALLERY.getNavigationItemIndex()];
                break;
            case R.id.menu_main_bottom_tab_resource:
                fragment = mMainFragments[BottomNavigationEnum.RESOURCE.getNavigationItemIndex()];
                break;
            default:
                break;
        }

        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_container, fragment, BottomNavigationEnum.HOME.getTag())
                    .commit();
        }
    }
}
