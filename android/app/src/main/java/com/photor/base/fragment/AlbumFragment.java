package com.photor.base.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.photor.MainApplication;
import com.photor.R;
import com.photor.album.adapter.AlbumsAdapter;
import com.photor.album.adapter.MediaAdapter;
import com.photor.album.entity.Album;
import com.photor.album.entity.HandlingAlbums;
import com.photor.album.entity.Media;
import com.photor.album.entity.SortingMode;
import com.photor.album.entity.SortingOrder;
import com.photor.album.entity.comparator.MediaComparators;
import com.photor.album.provider.StorageProvider;
import com.photor.album.utils.Measure;
import com.photor.album.utils.PreferenceUtil;
import com.photor.album.utils.ThemeHelper;
import com.photor.album.views.CustomScrollBarRecyclerView;
import com.photor.album.views.GridSpacingItemDecoration;
import com.photor.util.AlertDialogsHelper;

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

    // 跟图标设置相关的信息
    private ThemeHelper themeHelper;

    // 应用导航栏信息
    private Toolbar toolbar;
    private BottomNavigationViewEx navigationView;

    // move to, copy to 这些编辑模式
    private boolean editMode = false;
    private boolean hidenav = false;  // 隐藏底部的导航栏

    // 关于顶部导航栏的对话框信息
    private boolean checkForReveal = true;

    // 左边的拉出按钮信息
    private DrawerLayout mDrawerLayout;

    // 当前已经被选中的照片信息
    private ArrayList<Media> selectedMedias = new ArrayList<>();


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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);  // 在 fragment中也能使用 onOptionsItemSelected
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_album, container, false);
        ButterKnife.bind(this, rootView);
        themeHelper = new ThemeHelper(this.getContext());

        SP = PreferenceUtil.getInstance(this.getContext().getApplicationContext());

        toolbar = this.getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.local_folder));
        navigationView = this.getActivity().findViewById(R.id.bottom_main_navigation);

        mDrawerLayout = this.getActivity().findViewById(R.id.drawer_layout);

        albumFragment = this;
        rvAlbums = rootView.findViewById(R.id.grid_albums);
        rvMedia  = rootView.findViewById(R.id.grid_photos);

        initUI();
        // 加载在纯照片模式下的所有照片信息（需要存储权限看能读取数据库中的照片信息）[切换全部照片的模式时使用到]
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
        getActivity().invalidateOptionsMenu();
    }

    private boolean displayData(Bundle data) {
        // 初始化加载Album信息
        displayAlbums(true);
        return false;
    }

    private void displayAlbums() {
        all_photos = false;  // 显示某一个相册下的所有文件信息
        displayAlbums(true);
    }

    private void displayAlbums(boolean reload) {
        toolbar.setTitle(getResources().getString(R.string.local_folder));
        toolbar.setNavigationIcon(themeHelper.getToolbarIcon(GoogleMaterial.Icon.gmd_menu));
        albumsAdapter.swapDataSet(getAlbums().dispAlbums);
        if (reload) new PrepareAlbumTask(albumFragment).execute();

        albumsMode = true;  // 相册模式为true
        editMode = false;
        getActivity().invalidateOptionsMenu();
        mediaAdapter.swapDataSet(new ArrayList<Media>(), false);
        rvMedia.scrollToPosition(0);
    }

    private void displayCurrentAlbumMedia(boolean reload) {
        // 获得当前正在显示的 album，并显示出来
        toolbar.setTitle(getAlbum().getName());
        toolbar.setNavigationIcon(themeHelper.getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));

        mediaAdapter.swapDataSet(getAlbum().getMedias(), false);
        if (reload) {
            // 初始加载相册的时候，每个Album中只有一张照片，现在不要将album中的照片全部加载出来
            // 同时两个RecyclerView的交替显示也是在 Task 里面控制的
            new PreparePhotosTask(this).execute();
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAlbums();
            }
        });
        albumsMode = false; // 当前为显示某一个album下的全部照片文件信息，albumMode为false
        editMode = false;
        getActivity().invalidateOptionsMenu();
    }

    /**
     * 具体的相册图片被单击时的响应事件
     */
    private View.OnClickListener photosOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            Media m = (Media) view.findViewById(R.id.photo_path).getTag();
            int pos = -1;
            if (all_photos) {
                // 全局照片显示的模式下
                pos = getImagePosition(m.getPath());
            }

            if (!all_photos) {
                // 某一个相册下的照片列表
                if (editMode) {
                    // 长按模式
                    appBarOverlay();
                    mediaAdapter.notifyItemChanged(getAlbum().toggleSelectPhoto(m));
                    if (getAlbum().selectedMedias.size() == 0) {
                        getNavigationBar();  // 显示底部导航栏信息
                    }
                    getActivity().invalidateOptionsMenu();
                } else {
                    // 单击模式，进入相册列表
                }
            } else {
                // 全部图片信息
                if (editMode) {
                    // 长按模式
                    mediaAdapter.notifyItemChanged(toggleSelectPhoto(m));
                } else {
                    // 单击模式，进入相册列表
                }
            }
        }
    };

    /**
     * 查找某一副图片在全体图片中的下标信息
     * @param path
     * @return
     */
    private int getImagePosition(String path) {
        int pos = 0;
        if (all_photos) {
            for (int i = 0; i < listAll.size(); i ++) {
                if (listAll.get(i).getPath().equals(path)) {
                    pos = i;
                    break;
                }
            }
        }
        return pos;
    }

    /**
     * 设置具体图片被长按选择的具体状态
     * @param m
     * @return
     */
    private int toggleSelectPhoto(Media m) {
        if (m != null) {
            m.setSelected(!m.isSelected());
            if (m.isSelected()) {
                selectedMedias.add(m);
            } else {
                selectedMedias.remove(m);
            }
        }

        if (selectedMedias.size() == 0) {
            getNavigationBar();
            editMode = false;
            toolbar.setTitle(getString(R.string.all));
        } else {
            toolbar.setTitle(selectedMedias.size() + "/" + listAll.size());
        }

        getActivity().invalidateOptionsMenu();
        return getImagePosition(m.getPath());
    }

    /**
     * 在全体照片显示模式下，设置选择信息
     * @param targetIndex
     * @param adapter
     */
    public void selectAllPhotosUpTo(int targetIndex, MediaAdapter adapter) {
        int indexRightBeforeOrAfter = -1;
        int indexNow;
        for (Media sm : selectedMedias) {
            indexNow = getImagePosition(sm.getPath());
            if (indexRightBeforeOrAfter == -1) indexRightBeforeOrAfter = indexNow;

            if (indexNow > targetIndex) break;
            indexRightBeforeOrAfter = indexNow;
        }

        if (indexRightBeforeOrAfter != -1) {
            for (int index = Math.min(targetIndex, indexRightBeforeOrAfter); index <= Math.max(targetIndex, indexRightBeforeOrAfter); index++) {
                if (listAll.get(index) != null && !listAll.get(index).isSelected()) {
                    listAll.get(index).setSelected(true);
                    selectedMedias.add(listAll.get(index));
                    adapter.notifyItemChanged(index);
                }
            }
        }
        toolbar.setTitle(selectedMedias.size() + "/" + size);
    }

    private View.OnLongClickListener photosOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            if (checkForReveal) {
                enterReveal();
                checkForReveal = false;  // 显示actionbar的动画
            }

            Media m = (Media) view.findViewById(R.id.photo_path).getTag();
            // 隐藏底部的导航栏
            hideNavigationBar();
            hidenav = true;
            if (!all_photos) {
                // 某个相册下的全部文件图片信息
                if (!editMode) {
                    mediaAdapter.notifyItemChanged(getAlbum().toggleSelectPhoto(m));
                    editMode = true;
                } else {
                    getAlbum().selectAllPhotosUpTo(getAlbum().getIndex(m), mediaAdapter);
                }
            } else {
                // 全部文件图片信息
                if (!editMode) {
                    mediaAdapter.notifyItemChanged(toggleSelectPhoto(m));
                    editMode = true;
                } else {
                    selectAllPhotosUpTo(getImagePosition(m.getPath()), mediaAdapter);
                }
            }
            getActivity().invalidateOptionsMenu();
            return true;
        }
    };

    /**
     * 相册被单击时的事件
     */
    private View.OnClickListener albumOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            // 在RecyclerView的holder里面 holder.name(album_name).setTag(album);
            // 之后可以通过 album_name 这个控件取出 album 这个tag
            Album album = (Album) view.findViewById(R.id.album_name).getTag();
            if (editMode) {
                albumsAdapter.notifyItemChanged(getAlbums().toggleSelectAlbum(album));
                if (getAlbums().getSelectedCount() == 0) {
                    getNavigationBar();
                }
                getActivity().invalidateOptionsMenu();
            } else {
                // 设置当前 将要显示的 album详情信息
                getAlbums().setCurrentAlbum(album);  // 点击某一个相册信息的时候，就将该相册设置为当前默认选中的相册
                displayCurrentAlbumMedia(true);
            }

        }
    };

    /**
     * Animate bottom navigation bar from GONE to VISIBLE
     */
    public void showNavigationBar() {
        navigationView.animate()
                .translationY(0)  // 从上往下开始变换
                .alpha(1.0f)  // 最后的透明度信息
                .setDuration(400)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        navigationView.setVisibility(View.VISIBLE);
                    }
                });
    }

    /**
     * Animate bottom navigation bar from VISIBLE to GONE
     */
    public void hideNavigationBar() {
        navigationView.animate()
                .alpha(0.0f)
                .translationYBy(navigationView.getHeight())
                .setDuration(400)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        navigationView.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * 显示底部导航栏
     */
    public void getNavigationBar() {
        if (editMode && hidenav) {
            showNavigationBar();
            hidenav = false;
        }
    }

    /**
     * 当相册或者照片被长按的时候，导航栏的动画模式
     */
    private void enterReveal() {

        // get the center for the clipping circle
        int cx = toolbar.getMeasuredWidth() / 2;
        int cy = toolbar.getMeasuredHeight() / 2;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(toolbar.getWidth(), toolbar.getHeight()) / 2;

        // create the animator for this view
        Animator anim =
                ViewAnimationUtils.createCircularReveal(toolbar, cx, cy, 5, finalRadius);

        anim.start();
    }

    /**
     * 当从编辑模式退出的时候，导航栏的动画模式
     */
    private void exitReveal() {

        // get the center for the clipping circle
        int cx = toolbar.getMeasuredWidth() / 2;
        int cy = toolbar.getMeasuredHeight() / 2;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(toolbar.getWidth(), toolbar.getHeight()) / 2;

        // create the animator for this view
        Animator anim =
                ViewAnimationUtils.createCircularReveal(toolbar, cx, cy, finalRadius, 5);

        anim.start();
    }

    /**
     * 设置相册页面主要的菜单按钮的显示项
     */
    private void togglePrimaryToolbarOptions(final Menu menu) {
        menu.setGroupVisible(R.id.general_action, !editMode);
    }

    private View.OnLongClickListener albumOnLongCLickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View view) {
            Album album = (Album) view.findViewById(R.id.album_name).getTag();
            if (checkForReveal) {
                enterReveal();
                checkForReveal = false;
            }
            if (editMode) {
                // 如果处于编辑模式
                int currentAlbum = getAlbums().getCurrentAlbumIndex(album);
                getAlbums().selectAllPhotosUpToAlbums(currentAlbum, albumsAdapter);
            }
            albumsAdapter.notifyItemChanged(getAlbums().toggleSelectAlbum(album));
            editMode = true;
            getActivity().invalidateOptionsMenu();  // 设置选项菜单无效
            if (getAlbums().getSelectedCount() == 0) {
                getNavigationBar();
            } else {
                hideNavigationBar();
                hidenav = true;
            }
            return true;  // onLongClickListener -> onClickListener (如果return false 会发生莫名其妙的错误)
        }
    };

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

        // 设置相册显示的网格信息
        int spanCount = columnsCount();
        rvAlbumsDecoration = new GridSpacingItemDecoration(spanCount, Measure.pxToDp(3, getActivity().getApplicationContext()), true);
        rvAlbums.addItemDecoration(rvAlbumsDecoration);
        rvAlbums.setLayoutManager(new GridLayoutManager(this.getContext(), spanCount));

        albumsAdapter = new AlbumsAdapter(getAlbums().dispAlbums, getActivity());
        albumsAdapter.setOnClickListener(albumOnClickListener);
        albumsAdapter.setOnLongClickListener(albumOnLongCLickListener);
        rvAlbums.setAdapter(albumsAdapter);

        // 初始化相片显示信息
        // 设置照片显示的网格信息
        spanCount = mediaCount();
        rvMediaDecoration = new GridSpacingItemDecoration(spanCount, Measure.pxToDp(3, getActivity().getApplicationContext()), true);
        rvMedia.addItemDecoration(rvMediaDecoration);
        rvMedia.setLayoutManager(new GridLayoutManager(this.getContext(), spanCount));

        mediaAdapter = new MediaAdapter(getAlbum().getMedias(), getActivity());
        mediaAdapter.setOnClickListener(photosOnClickListener);
        mediaAdapter.setOnLongClickListener(photosOnLongClickListener);
        rvMedia.setAdapter(mediaAdapter);

        //2. 设置下拉刷新事件
        refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (albumsMode) {
                    editMode = false;
                    // 说明是以相册模式来浏览照片
                    getAlbums().clearSelectedAlbums();
                    new PrepareAlbumTask(albumFragment).execute();
                } else {
                    if (!all_photos) {
                        // 说明是 在浏览某个相册下的 照片文件信息
                        new PreparePhotosTask(AlbumFragment.this).execute();
                    } else {
                        // all_photos == true
                        new PrepareAllPhotos(AlbumFragment.this).execute();
                    }
                }
            }
        };
        swipeRefreshLayout.setOnRefreshListener(refreshListener);
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
                (!albumsMode && getAlbum().getMedias().size() == 0) ? View.VISIBLE : View.GONE);
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

    private void displayAllMedia(boolean reload) {
        toolbar.setTitle(getString(R.string.all_media));
        toolbar.setNavigationIcon(themeHelper.getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));

        mediaAdapter.swapDataSet(listAll, false);
        if (reload) {
            new PrepareAllPhotos(albumFragment).execute();
        }
        // 点击返回按钮的时候，按照相册模式显示照片
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAlbums();  // 这里面会设置albumsMode为true  all_photos 为false
            }
        });
        albumsMode = false;  // 当前不是以相册模式来显示照片
        editMode = false;
        getActivity().invalidateOptionsMenu();
    }

    /**
     * 当退出编辑模式的时候被调用
     * （当返回键被按下的时候调用）
     */
    private void finishEditMode() {
        if (editMode) {
            enterReveal(); // 展示切换顶部导航栏的效果
        }
        editMode = false;
        if (albumsMode) {
            // 相册模式
            getAlbums().clearSelectedAlbums();
            albumsAdapter.notifyDataSetChanged();
        } else {
            // 照片模式
            if (!all_photos) {
                getAlbum().clearSelectedPhotos();
                mediaAdapter.notifyDataSetChanged();
            } else {
                clearSelectedPhotos();
                mediaAdapter.notifyDataSetChanged();
            }
        }
        getActivity().invalidateOptionsMenu();
    }

    /**
     * 设置长按状态下，toolbar的显示设置
     */
    private void appBarOverlay() {
        // SCROLL_FLAG_SCROLL:以上所有的标志为都依赖于此标志方能工作。
        // SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED:此标志位依赖SCROLL_FLAG_ENTER_ALWAYS。且会跟minHeight有关。
        // 比如将minHeight设定为14dp。手指往上滚动时View仍然会消失。反过来，跟上面不一样的是，View在NestScrolling没有滑动到顶部的时候，
        // View最多只会更随出现14dp也就是minHeight的高度。当到顶部的时候，方才会慢慢显示剩下的部分。
        //SCROLL_FLAG_EXIT_UNTIL_COLLAPSED:此标志位会跟minHeight有关。手指往上滚动时，也就是View的Exit状态，
        // View会留下minHeight的一段高度露出来，而不会向前面一样完全消失。手指往下滚动时，此标志类似于SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED的状态。
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);  // clear all scroll flags
    }

    /**
     * 设置在退出长按的状态下，toolbar的显示设置
     */
    private void clearOverlay() {
        // SCROLL_FLAG_ENTER_ALWAYS:手指往上滚动时(即Scroll Down时)，View会消失。反之，View会跟随手指滑动慢慢出现。
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
    }

    /**
     * 处理在长按模式下，点击导航栏返回按钮的各种逻辑
     */
    private void updateSelectedStuff() {
        if (albumsMode) {
            if (getAlbums().getSelectedCount() == 0) {
                clearOverlay();
                checkForReveal = true;
                // 允许刷新操作
                swipeRefreshLayout.setEnabled(true);
            } else {
                // 禁止刷新操作
                appBarOverlay();
                swipeRefreshLayout.setEnabled(false);
            }

            if (editMode) {
                // 设置当前的被选择照片的数量，因为是在onPrepareOptionsMenu中被调用，所以每次的长按操作有变化的时候
                // 都需要invalidateOptionsMenu进行menu的更新操作
                toolbar.setTitle(getAlbums().getSelectedCount() + "/" + getAlbums().dispAlbums.size());
            } else {
                toolbar.setTitle(getString(R.string.local_folder));
                toolbar.setNavigationIcon(themeHelper.getToolbarIcon(GoogleMaterial.Icon.gmd_menu));
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDrawerLayout.openDrawer(GravityCompat.START);
                    }
                });
            }
        } else {
            if (!all_photos) {
                // 某一相册下的照片
                if (getAlbum().getSelectedCount() == 0) {
                    clearOverlay();
                    checkForReveal = true;
                    swipeRefreshLayout.setEnabled(true);
                } else {
                    appBarOverlay();
                    swipeRefreshLayout.setEnabled(false);
                }
            } else {
                // 全部照片的模式
                if (selectedMedias.size() == 0) {
                    clearOverlay();
                    checkForReveal = true;
                    swipeRefreshLayout.setEnabled(true);
                } else {
                    appBarOverlay();
                    swipeRefreshLayout.setEnabled(false);
                }
            }

            // 设置标题栏上的文字信息
            if (editMode) {
                // 处于编辑模式
                if (!all_photos) {
                    // 相册下的照片信息
                    toolbar.setTitle(getAlbum().getSelectedCount() + "/" + getAlbum().getMedias().size());
                } else {
                    // 全部相片的列出模式
                    toolbar.setTitle(selectedMedias.size() + "/" + listAll.size());
                }
            } else {
                // 处于非编辑模式
                if (!all_photos) {
                    toolbar.setTitle(getAlbum().getName());
                } else {
                    toolbar.setTitle(getString(R.string.all_media));
                }
                // 在纯照片模式下点击返回按钮返回相册页面
                toolbar.setNavigationIcon(themeHelper.getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        displayAlbums();
                    }
                });
            }
        }

        // editmode的时候，显示已经被选择的照片或者是相册的数量
        if (editMode) {
            toolbar.setNavigationIcon(themeHelper.getToolbarIcon(GoogleMaterial.Icon.gmd_clear));
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getNavigationBar();  // 显示底部的导航栏
                    finishEditMode();  // 清空被选择的相册、相册信息
                    clearSelectedPhotos();
                }
            });
        }
    }

    /**
     * 清空已经选择的照片信息
     */
    public void clearSelectedPhotos() {
        for (Media m : selectedMedias) {
            m.setSelected(false);
        }
        if (selectedMedias != null) {
            selectedMedias.clear();
        }
        toolbar.setTitle(getString(R.string.local_folder));
    }


    /**
     * 选择列出全部照片模式下的全部照片信息
     */
    public void selectAllPhotos() {
        if (all_photos) {
            for (Media m: listAll) {
                m.setSelected(true);
                selectedMedias.add(m);
            }
            toolbar.setTitle(selectedMedias.size() + "/" + listAll.size());
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (albumsMode) {
            // 相册模式下（全部照片、选择全部）
            editMode = getAlbums().getSelectedCount() != 0;
            menu.setGroupVisible(R.id.album_options_menu, editMode);
            menu.setGroupVisible(R.id.photos_option_men, false);
            menu.findItem(R.id.all_photos).setVisible(true);
            menu.findItem(R.id.select_all).setVisible(getAlbums().getSelectedCount() != albumsAdapter.getItemCount() ? true : false);

            if (getAlbums().getSelectedCount() >= 1) {
                if (getAlbums().getSelectedCount() > 1) {
                    menu.findItem(R.id.album_details).setVisible(false);
                }
            }
        } else {
            if (!all_photos) {
                // 某个相册下的照片
                editMode = getAlbum().areMediaSelected();
                menu.setGroupVisible(R.id.photos_option_men, editMode);
                menu.setGroupVisible(R.id.album_options_menu, !editMode);
                menu.findItem(R.id.all_photos).setVisible(false);
                menu.findItem(R.id.album_details).setVisible(false);
            } else {
                // 全部的照片
                editMode = selectedMedias.size() != 0;
                menu.setGroupVisible(R.id.photos_option_men, editMode);
                menu.setGroupVisible(R.id.album_options_menu, !editMode);
                menu.findItem(R.id.all_photos).setVisible(false);
                menu.findItem(R.id.album_details).setVisible(false);
            }
            menu.findItem(R.id.select_all).setVisible(
                    (getAlbum().getSelectedCount() == mediaAdapter.getItemCount()
                            || selectedMedias.size() == listAll.size())
                            ? false : true);
        }
        togglePrimaryToolbarOptions(menu);  // 控制是否显示排序菜单按钮
        updateSelectedStuff();  // 更新顶部导航栏信息
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        getNavigationBar();  // 显示底部导航栏信息
        switch (item.getItemId()) {
            case R.id.all_photos:
                // 显示全部照片
                if (!all_photos) {
                    all_photos = true;
                    displayAllMedia(true);
                } else {
                    displayAlbums();
                }
                return true;
            case R.id.album_details:
                // 显示相册详情信息
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialog_Light);
                AlertDialog detailDialog = AlertDialogsHelper.getAlbumDetailsDialog(getActivity(), builder, getAlbums().getSelectedAlbum(0));

                detailDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok_action).toUpperCase(),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finishEditMode();
                            }
                        });
                detailDialog.show();
                AlertDialogsHelper.setButtonTextColor(new int[] {DialogInterface.BUTTON_POSITIVE}, R.color.md_light_blue_500, detailDialog);
                return true;
            case R.id.select_all:
                if (albumsMode) {
                    // 相册模式下
                    getAlbums().selectAllAlbums();
                    albumsAdapter.notifyDataSetChanged();
                } else {
                    if (!all_photos) {
                        // 某一相册下的图片信息
                        getAlbum().selectAllPhotos();
                        mediaAdapter.notifyDataSetChanged();
                    } else {
                        // 全部相片信息
                        clearSelectedPhotos();
                        selectAllPhotos();  // 必须先调用clearSelectedPhotos之后调用，否则selectMedias会出现重复的media
                        mediaAdapter.notifyDataSetChanged();
                    }
                }
                getActivity().invalidateOptionsMenu();
                return true;
            case R.id.name_sort_action:
                if (albumsMode) {
                    // 对相册进行排序时，以 HandlingAlbums 中存储的照片排序信息进行存取，信息存储在SP中
                    getAlbums().setDefaultSortingMode(SortingMode.NAME);
                    new SortingUtilsAlbums(albumFragment).execute();
                } else {
                    // 设置每一个相册内的照片的排序信息，信息存储在SQL Lite数据库中
                    new SortModeSet(albumFragment).execute(SortingMode.NAME);
                    if (!all_photos) {
                        // 某一个相册内的照片信息
                        new SortingUtilsPhotos(albumFragment).execute();
                    } else {
                        // 全部照片之间的排序信息
                        new SortingUtilsListAll(albumFragment).execute();
                    }
                }
                item.setChecked(true);
                return true;
            case R.id.date_taken_sort_action:
                if (albumsMode) {
                    // 对相册进行排序时，以 HandlingAlbums 中存储的照片排序信息进行存取，信息存储在SP中
                    getAlbums().setDefaultSortingMode(SortingMode.DATE);
                    new SortingUtilsAlbums(albumFragment).execute();
                } else {
                    // 设置每一个相册内的照片的排序信息，信息存储在SQL Lite数据库中
                    new SortModeSet(albumFragment).execute(SortingMode.DATE);
                    if (!all_photos) {
                        // 某一个相册内的照片信息
                        new SortingUtilsPhotos(albumFragment).execute();
                    } else {
                        // 全部照片之间的排序信息
                        new SortingUtilsListAll(albumFragment).execute();
                    }
                }
                item.setChecked(true);
                return true;
            case R.id.size_sort_action:
                if (albumsMode) {
                    // 对相册进行排序时，以 HandlingAlbums 中存储的照片排序信息进行存取，信息存储在SP中
                    getAlbums().setDefaultSortingMode(SortingMode.SIZE);
                    new SortingUtilsAlbums(albumFragment).execute();
                } else {
                    // 设置每一个相册内的照片的排序信息，信息存储在SQL Lite数据库中
                    new SortModeSet(albumFragment).execute(SortingMode.SIZE);
                    if (!all_photos) {
                        // 某一个相册内的照片信息
                        new SortingUtilsPhotos(albumFragment).execute();
                    } else {
                        // 全部照片之间的排序信息
                        new SortingUtilsListAll(albumFragment).execute();
                    }
                }
                item.setChecked(true);
                return true;
            case R.id.ascending_sort_action:
                if (albumsMode) {
                    getAlbums().setDefaultSortingAscending(item.isChecked() ? SortingOrder.DESCENDING : SortingOrder.ASCENDING);
                    new SortingUtilsAlbums(albumFragment).execute();
                } else {
                    getAlbum().setDefaultSortingAscending(albumFragment.getContext(),
                            item.isChecked() ? SortingOrder.DESCENDING : SortingOrder.ASCENDING);
                    if (!all_photos) {
                        new SortingUtilsPhotos(albumFragment).execute();
                    } else {
                        new SortingUtilsListAll(albumFragment).execute();
                    }
                }
                item.setTitle(!item.isChecked() ? R.string.descending : R.string.ascending);
                item.setChecked(!item.isChecked());
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 设置全部照片的排序信息
     */
    private static class SortingUtilsListAll extends AsyncTask<Void, Void, Void> {

        private WeakReference<AlbumFragment> reference;

        public SortingUtilsListAll(AlbumFragment reference) {
            this.reference = new WeakReference<>(reference);
        }

        @Override
        protected void onPreExecute() {
            AlbumFragment albumFragment = reference.get();
            albumFragment.swipeRefreshLayout.setRefreshing(true);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            AlbumFragment albumFragment = reference.get();
            Collections.sort(listAll, MediaComparators.getComparator(
                    albumFragment.getAlbum().settings.getSortingMode(),
                    albumFragment.getAlbum().settings.getSortingOrder()
            ));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            AlbumFragment albumFragment = reference.get();
            albumFragment.swipeRefreshLayout.setRefreshing(false);
            albumFragment.mediaAdapter.swapDataSet(listAll, false);
            super.onPostExecute(aVoid);
        }
    }

    /**
     * 设置某一个相册下的照片排序信息
     */
    private static class SortingUtilsPhotos extends AsyncTask<Void, Void, Void> {

        private WeakReference<AlbumFragment> reference;

        public SortingUtilsPhotos(AlbumFragment reference) {
            this.reference = new WeakReference<>(reference);
        }

        @Override
        protected void onPreExecute() {
            AlbumFragment albumFragment = reference.get();
            albumFragment.swipeRefreshLayout.setRefreshing(true);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            AlbumFragment albumFragment = reference.get();
            albumFragment.getAlbum().sortPhotos();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            AlbumFragment albumFragment = reference.get();
            albumFragment.swipeRefreshLayout.setRefreshing(false);
            albumFragment.mediaAdapter.swapDataSet(albumFragment.getAlbum().getMedias(), false);
            super.onPostExecute(aVoid);
        }
    }

    /**
     * 对全部的相册信息进行排序，相册间的排序信息是存储在SP中的
     */
    private static class SortingUtilsAlbums extends AsyncTask<Void, Void, Void> {

        private WeakReference<AlbumFragment> reference;

        public SortingUtilsAlbums(AlbumFragment reference) {
            this.reference = new WeakReference<>(reference);
        }

        @Override
        protected void onPreExecute() {
            AlbumFragment albumFragment = reference.get();
            albumFragment.swipeRefreshLayout.setRefreshing(true);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            AlbumFragment albumFragment = reference.get();
            // 对当前的相册信息进行排序（当前相册的排序信息从SP中读取），排序的字段以及升降序信息之前在SP中就已经设置好了
            albumFragment.getAlbums().sortAlbums();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            AlbumFragment albumFragment = reference.get();
            albumFragment.swipeRefreshLayout.setRefreshing(false);
            super.onPostExecute(aVoid);
            albumFragment.albumsAdapter.swapDataSet(albumFragment.getAlbums().dispAlbums);
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
            albumFragment.mediaAdapter.swapDataSet(albumFragment.getAlbum().getMedias(), false);

            albumFragment.checkNothing();
            albumFragment.swipeRefreshLayout.setRefreshing(false);
        }
    }


}
