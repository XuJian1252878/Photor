package com.photor.base.fragment;

import android.Manifest;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.photor.MainApplication;
import com.photor.R;
import com.photor.album.adapter.AlbumsAdapter;
import com.photor.album.adapter.MediaAdapter;
import com.photor.album.entity.Album;
import com.photor.album.entity.HandlingAlbums;
import com.photor.album.entity.Media;
import com.photor.album.entity.SortingMode;
import com.photor.album.entity.comparator.MediaComparators;
import com.photor.album.provider.StorageProvider;
import com.photor.album.utils.Measure;
import com.photor.album.utils.PreferenceUtil;
import com.photor.album.views.CustomScrollBarRecyclerView;
import com.photor.album.views.GridSpacingItemDecoration;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.photor.album.entity.SortingMode.DATE;

/**
 * Created by xujian on 2018/2/26.
 */

public class AlbumFragment extends Fragment {

    private PreferenceUtil SP;

    private CustomScrollBarRecyclerView rvAlbums;
    private CustomScrollBarRecyclerView rvMedia;

    private AlbumsAdapter albumsAdapter;
    private SwipeRefreshLayout.OnRefreshListener refreshListener;
    private boolean albumsMode = true;  // 当前是以 相册模式来显示
    private MediaAdapter mediaAdapter;
    private AlbumFragment albumFragment;

    private boolean firstLaunch = true; // 记录当前是否第一次加载

    private GridSpacingItemDecoration rvAlbumsDecoration;
    private GridSpacingItemDecoration rvMediaDecoration;

    // 以全部模式显示照片时
    public boolean all_photos = false;  // true 以照片模式显示当前的图片
    public static ArrayList<Media> listAll;
    public int size;
    private ArrayList<Media> media;
    private ArrayList<Album> albList;


    @BindView(R.id.swipeRefreshLayout)
    protected SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.nothing_to_show)
    protected TextView nothingToShow;
    @BindView(R.id.no_search_results)
    protected TextView textView;
    @BindView(R.id.star_image_view)
    protected ImageView starImageView;


    public static AlbumFragment newInstance() {
        AlbumFragment albumFragment = new AlbumFragment();

        Bundle args = new Bundle();
        albumFragment.setArguments(args);

        return albumFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_album, container, false);
        ButterKnife.bind(this, rootView);

        SP = PreferenceUtil.getInstance(this.getContext().getApplicationContext());

        albumFragment = this;
        rvAlbums = rootView.findViewById(R.id.grid_albums);
        rvMedia  = rootView.findViewById(R.id.grid_photos);

        initUI();
        // 加载在纯照片模式下的所有照片信息（需要存储权限看能读取数据库中的照片信息）
        new InitAllPhotos().execute();
        // 设置每一个相册的默认排序方式
        new SortModeSet(albumFragment).execute(DATE);
        // 进行相册数据的预加载
        displayData(savedInstanceState);
        // 检查当前可显示的album信息是否为空
        checkNothing();
        // 填充当前已有的album信息
        populateAlbum();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (all_photos) {
            // 照片模式下
            new PrepareAllPhotos(albumFragment).execute();
            mediaAdapter.notifyDataSetChanged();
        }

        if (albumsMode) {
            // 相册模式下
            new PrepareAlbumTask(albumFragment).execute();
            albumsAdapter.notifyDataSetChanged();
        }

        firstLaunch = false;
    }

    private boolean displayData(Bundle data) {
        // 初始化加载Album信息
        displayAlbums(true);
        return false;
    }

    private void displayAlbums(boolean reload) {
        albumsAdapter.swapDataSet(getAlbums().dispAlbums);
        if (reload) new PrepareAlbumTask(albumFragment).execute();
        albumsMode = true;

        mediaAdapter.swapDataSet(new ArrayList<Media>(), false);
        rvMedia.scrollToPosition(0);
    }

    /**
     * 调整当前Album的排序方式
     */
    private static class SortModeSet extends AsyncTask<SortingMode, Void, Void> {

        private WeakReference<AlbumFragment> reference;

        public SortModeSet(AlbumFragment reference) {
            this.reference = new WeakReference<>(reference);
        }

        @Override
        protected Void doInBackground(SortingMode... sortingModes) {
            for (Album album: reference.get().getAlbums().dispAlbums) {
                if (album.settings.getSortingMode().getValue() != sortingModes[0].getValue()) {
                    album.setDefaultSortingMode(reference.get().getContext(), sortingModes[0]);
                }
            }
            return null;
        }
    }

    /**
     * 加载所有的照片路径信息，供纯照片模式下使用
     */
    private class InitAllPhotos extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            listAll = StorageProvider.getAllShownImages(AlbumFragment.this.getActivity());
            size = listAll.size();
            media = listAll;
            Collections.sort(listAll, MediaComparators.getComparator(getAlbum().settings.getSortingMode(), getAlbum().settings.getSortingOrder()));
            return null;
        }
    }

    private void initUI() {
        //1. 初始化加载相册信息
        rvAlbums.setHasFixedSize(true);
        rvAlbums.setItemAnimator(new DefaultItemAnimator());
        rvMedia.setHasFixedSize(true);
        rvMedia.setItemAnimator(new DefaultItemAnimator());

        albumsAdapter = new AlbumsAdapter(getAlbums().dispAlbums, AlbumFragment.this.getContext());
        rvAlbums.setAdapter(albumsAdapter);

        // 初始化相片显示信息
        mediaAdapter = new MediaAdapter(getAlbum().getMedia(), AlbumFragment.this.getContext());
        rvMedia.setAdapter(mediaAdapter);

        //2. 设置下拉刷新事件
        refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (albumsMode) {
                    getAlbums().clearSelectedAlbums();
                    new PrepareAlbumTask(albumFragment).execute();
                }
            }
        };
        swipeRefreshLayout.setOnRefreshListener(refreshListener);

        // 设置相册显示的网格信息
        int spanCount = columnsCount();
        rvAlbumsDecoration = new GridSpacingItemDecoration(spanCount, Measure.pxToDp(3, getActivity().getApplicationContext()), true);
        rvAlbums.addItemDecoration(rvAlbumsDecoration);
        rvAlbums.setLayoutManager(new GridLayoutManager(this.getContext(), spanCount));

        // 设置照片显示的网格信息
        spanCount = mediaCount();
        rvMediaDecoration = new GridSpacingItemDecoration(spanCount, Measure.pxToDp(3, getActivity().getApplicationContext()), true);
        rvMedia.addItemDecoration(rvMediaDecoration);
        rvMedia.setLayoutManager(new GridLayoutManager(this.getContext(), spanCount));
    }

    /**
     * 显示相册的时候，相册显示的列数（横屏和竖屏的状态下）
     * @return
     */
    public int columnsCount() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                ? SP.getInt("n_columns_folders", 2)
                : SP.getInt("n_columns_folders_landscape", 3);
    }

    /**
     * 以照片的模式显示的时候，照片显示的列数（横屏和竖屏状态下）
     * @return
     */
    public int mediaCount() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                ? SP.getInt("n_columns_media", 3)
                : SP.getInt("n_columns_media_landscape", 4);
    }

    private void checkNothing() {
        nothingToShow.setText(getString(R.string.there_is_nothing_to_show));
        nothingToShow.setVisibility((albumsMode && getAlbums().dispAlbums.size() == 0) ||
                (!albumsMode && getAlbum().getMedia().size() == 0) ? View.VISIBLE : View.GONE);
        starImageView.setVisibility(View.GONE);
    }


    /**
     * 如果是相册模式 rvAlbums显示；如果是照片模式 rvMedia 显示
     * @param albumsMode
     */
    private void toggleRecyclersVisibility(boolean albumsMode) {
        rvAlbums.setVisibility(albumsMode ? View.VISIBLE : View.GONE);
        rvMedia.setVisibility(albumsMode ? View.GONE : View.VISIBLE);
        nothingToShow.setVisibility(View.GONE);
        starImageView.setVisibility(View.GONE);
//        if (albumsMode)
//            fabScrollUp.hide();
        //touchScrollBar.setScrollBarHidden(albumsMode);

    }

    /**
     * 如果当前Album数量不为0，那么获取当前选择的或者默认第一个的album信息；否则，返回一个空的album（Setting信息为默认）
     * @return
     */
    public Album getAlbum() {
        return ((MainApplication) this.getContext().getApplicationContext()).getAlbum();
    }

    /**
     * 用于获取全部的相册信息
     * @return
     */
    private HandlingAlbums getAlbums() {
        return ((MainApplication)this.getContext().getApplicationContext()).getAlbums();
    }


    public void populateAlbum() {
        albList = new ArrayList<>();
        for (Album album : getAlbums().dispAlbums) {
            albList.add(album);
        }
    }

    /**
     * 后台加载全部的相册信息
     */
    private static class PrepareAlbumTask extends AsyncTask<Void, Integer, Void> {

        // 被弱引用关联的对象只能生存到下一次垃圾回收发生之前
        // 下一次垃圾回收的时候，不管内存是否足够，都会回收只被弱引用关联的对象
        private WeakReference<AlbumFragment> reference;

        PrepareAlbumTask(AlbumFragment albumFragment) {
            this.reference = new WeakReference<>(albumFragment);
        }

        @Override
        protected void onPreExecute() {
            AlbumFragment albumFragmentRef = this.reference.get();
            // 开启刷新提示UI
            albumFragmentRef.swipeRefreshLayout.setRefreshing(true);
            // 处于相片浏览模式
            albumFragmentRef.toggleRecyclersVisibility(true);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // 初始化加载相册信息（相册里面具体的图片信息并没有被加载出来）
            ((MainApplication) reference.get().getContext().getApplicationContext()).getAlbums()
                    .loadAlbums(reference.get().getContext(), false);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            AlbumFragment albumFragment = reference.get();
            // 设置初始化时的Album相册信息
            albumFragment.albumsAdapter.swapDataSet(
                    ((MainApplication) reference.get().getContext().getApplicationContext()).getAlbums().dispAlbums
            );
            albumFragment.albList = new ArrayList<>();
            albumFragment.populateAlbum();
            albumFragment.swipeRefreshLayout.setRefreshing(false);
            super.onPostExecute(aVoid);
        }
    }

    /**
     * 加载设备中的全部照片信息
     */
    private static class PrepareAllPhotos extends AsyncTask<Void, Void, Void> {

        private WeakReference<AlbumFragment> reference;

        public PrepareAllPhotos(AlbumFragment reference) {
            this.reference = new WeakReference<>(reference);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AlbumFragment albumFragment = reference.get();
            // 设置刷新按钮处于刷新状态
            albumFragment.swipeRefreshLayout.setRefreshing(true);
            // 设置当前是属于相片的显示模式
            albumFragment.toggleRecyclersVisibility(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            AlbumFragment albumFragment = reference.get();
            // 更新当前album下所有的照片文件信息
            albumFragment.getAlbum().updatePhotos(albumFragment.getContext());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            AlbumFragment albumFragment = reference.get();
            listAll = StorageProvider.getAllShownImages(albumFragment.getActivity());
            albumFragment.size = listAll.size();
            Collections.sort(listAll, MediaComparators.getComparator(albumFragment.getAlbum().settings.getSortingMode(),
                    albumFragment.getAlbum().settings.getSortingOrder()));
            // 开始更新照片数据信息
            albumFragment.mediaAdapter.swapDataSet(listAll, false);

            albumFragment.checkNothing();
            albumFragment.swipeRefreshLayout.setRefreshing(false);  // 刷新结束
        }
    }

    /**
     * 后台更新某一个文件夹下的照片信息
     */
    private static class PreparePhotosTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<AlbumFragment> reference;

        public PreparePhotosTask(AlbumFragment reference) {
            this.reference = new WeakReference<>(reference);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AlbumFragment albumFragment = reference.get();
            albumFragment.swipeRefreshLayout.setRefreshing(true);
            // 当前是照片显示模式
            albumFragment.toggleRecyclersVisibility(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // 更新当前相册下的照片信息
            reference.get().getAlbum().updatePhotos(reference.get().getContext());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            AlbumFragment albumFragment = reference.get();
            albumFragment.mediaAdapter.swapDataSet(albumFragment.getAlbum().getMedia(), false);

            albumFragment.checkNothing();
            albumFragment.swipeRefreshLayout.setRefreshing(false);
        }
    }
}
