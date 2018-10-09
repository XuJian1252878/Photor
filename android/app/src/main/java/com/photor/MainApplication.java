package com.photor;

import android.app.Application;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.photor.album.entity.Album;
import com.photor.album.entity.HandlingAlbums;
import com.photor.util.ActivitySwitchHelper;

import java.util.Arrays;
import java.util.List;

public class MainApplication extends Application implements ReactApplication {

    private HandlingAlbums albums = null;  // 相册加载相关信息

    /**
     * 如果当前有正在显示的album，那么取出被点击的album信息
     * 否则获取一个空的album信息
     * @return
     */
    public Album getAlbum() {
        return albums.dispAlbums.size() > 0 ? albums.getCurrentAlbum() : Album.getEmptyAlbum();
    }


    private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
    @Override
    public boolean getUseDeveloperSupport() {
      return BuildConfig.DEBUG;
    }

    @Override
    protected List<ReactPackage> getPackages() {
        return Arrays.<ReactPackage>asList(
          new MainReactPackage()
        );
    }

    @Override
    protected String getJSMainModuleName() {
      return "index";
    }
    };

    @Override
    public ReactNativeHost getReactNativeHost() {
    return mReactNativeHost;
    }

    @Override
    public void onCreate() {

        // 相册处理信息初始化
        albums = new HandlingAlbums(getApplicationContext());

        // 设置全局的applicationContext信息
        ActivitySwitchHelper.setContext(getApplicationContext());

        super.onCreate();
        SoLoader.init(this, /* native exopackage */ false);
    }


    /**
     * 获得相册处理类信息
     * @return
     */
    public HandlingAlbums getAlbums() {
        return albums;
    }

}
