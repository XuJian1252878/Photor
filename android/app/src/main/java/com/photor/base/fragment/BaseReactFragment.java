package com.photor.base.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.photor.MainApplication;

/**
 * Created by xujian on 2018/2/23.
 * BaseReactFragment 继承Fragment类 ，
 * 它们在 Fragment 的 onAttach 方法中获取ReactRootView和ReactInstanceManager，并在 onCreateView 方法中返回该 ReactRootView
 * 在 onActivityCreated 方法中即可使用我们的 React Native 组件，这里需要子类实现 getMainPageName 抽象方法，获取到对应的 React Native 组件。
 */

public abstract class BaseReactFragment extends Fragment {

    private ReactRootView mReactRootView;
    private ReactInstanceManager mReactInstanceManager;

    // This method returns the name of our top-level component to show
    public abstract String getMainComponentName();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mReactRootView = new ReactRootView(context);
        mReactInstanceManager = ((MainApplication) getActivity().getApplication()).getReactNativeHost().getReactInstanceManager();
    }

    @Override
    public ReactRootView onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return mReactRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mReactRootView.startReactApplication(
                mReactInstanceManager,
                getMainComponentName(),
                null
        );
    }
}
