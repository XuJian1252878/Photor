package com.photor.album.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.file.FileUtils;
import com.example.theme.ThemeHelper;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.photor.R;
import com.photor.album.adapter.TrashBinAdapter;
import com.photor.album.entity.Media;
import com.photor.base.activity.BaseActivity;
import com.photor.data.TrashBinRealmModel;
import com.photor.util.ActivitySwitchHelper;
import com.photor.util.AlertDialogsHelper;
import com.photor.util.BasicCallBack;
import com.photor.util.SnackBarHandler;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * @author htwxujian@gmail.com
 * @date 2018/10/22 16:40
 */
public class TrashBinActivity extends BaseActivity {


    @BindView(R.id.trashbin_recycler_view)
    public RecyclerView trashBinRecyclerView;

    @BindView(R.id.toolbar)
    public Toolbar toolbar;

    @BindView(R.id.empty_trash)
    public RelativeLayout emptyView;

    @BindView(R.id.image_trash)
    public ImageView emptyIcon;

    @BindView(R.id.trash_text)
    public TextView trashText;

    @BindView(R.id.trash_message)
    public TextView trashMessage;

    @BindView(R.id.swipeRefreshLayout_trashbin)
    protected SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.view_parent)
    public RelativeLayout parentView;

    private RealmQuery<TrashBinRealmModel> trashBinRealmModelRealmQuery;
    private TrashBinAdapter trashBinAdapter;
    private ArrayList<TrashBinRealmModel> deletedTrash;

    /**
     * 当回收站的浮动菜单项被点击，回收站中无文件的时候
     */
    private BasicCallBack basicCallBack = new BasicCallBack() {
        @Override
        public void callBack(int status, Object data) {
            if (status == 2) {
                emptyView.setVisibility(View.VISIBLE);
                invalidateOptionsMenu();
            }
        }
    };

    /**
     * 当回收站中的照片被点击的时候，显示回收站中的照片
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TrashBinRealmModel trashBinRealmModel = (TrashBinRealmModel) view.findViewById(R.id.delete_date).getTag();
            view.setTransitionName(getString(R.string.transition_photo));
            Intent intent = new Intent("com.android.camera.action.REVIEW", Uri.fromFile(new File(trashBinRealmModel.getTrashbinpath())));
            intent.putExtra("path", trashBinRealmModel.getTrashbinpath());
            intent.putExtra("position", checkpos(trashBinRealmModel.getTrashbinpath()));
            intent.putExtra("size", getTrashObjects().size());
            intent.putExtra("trashbin", true);
            ArrayList<Media> u = loaduploaddata();
            intent.putParcelableArrayListExtra("trashdatalist", u);
            intent.setClass(getApplicationContext(), SingleMediaActivity.class);
            ActivitySwitchHelper.getContext().startActivity(intent);
        }
    };

    /**
     * 检查当前回收站图片路径的下标
     * @param path
     * @return
     */
    private int checkpos(String path) {
        int pos = 0;
        ArrayList<TrashBinRealmModel> trashBinRealmModels = getTrashObjects();
        for (int i = 0; i < trashBinRealmModels.size(); i ++) {
            if (path.equals(trashBinRealmModels.get(i).getTrashbinpath())) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    private ArrayList<TrashBinRealmModel> getTrashObjects() {
        ArrayList<TrashBinRealmModel> list = new ArrayList<>();  // 还存在于回收站中的文件
        ArrayList<TrashBinRealmModel> toDelete = new ArrayList<>();  // 不存在于文件系统中的文件
        for (int i = 0; i < trashBinRealmModelRealmQuery.count(); i ++) {
            if (new File(trashBinRealmModelRealmQuery.findAll().get(i).getTrashbinpath()).exists()) {
                list.add(trashBinRealmModelRealmQuery.findAll().get(i));
            } else {
                toDelete.add(trashBinRealmModelRealmQuery.findAll().get(i));
            }
        }

        for (int i = 0; i < toDelete.size(); i ++) {
            String path = toDelete.get(i).getTrashbinpath();
            Realm.init(this);
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<TrashBinRealmModel> results = realm.where(TrashBinRealmModel.class).equalTo("trashbinpath", path).findAll();
                    results.deleteAllFromRealm();
                }
            });
        }
        return list;
    }


    /**
     * 将回收站中的文件路径转化为media信息
     * @return
     */
    private ArrayList<Media> loaduploaddata() {
        ArrayList<Media> data = new ArrayList<>();
        ArrayList<TrashBinRealmModel> binRealmModelArrayList = getTrashObjects();
        for (int i = 0; i < binRealmModelArrayList.size(); i ++) {
            data.add(new Media(new File(binRealmModelArrayList.get(i).getTrashbinpath())));
        }
        return data;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash_bin);
        ButterKnife.bind(this);

        Realm.init(this);
        Realm realm = Realm.getDefaultInstance();
        // 缓存当前数据库中回收站的RealmQuery信息
        trashBinRealmModelRealmQuery = realm.where(TrashBinRealmModel.class);
        ArrayList<TrashBinRealmModel> trashlist = getTrashObjects();
        trashBinAdapter = new TrashBinAdapter(trashlist, basicCallBack);
        if (trashlist.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            trashEmptyViewSetup();
        } else {
            // 显示回收站中的文件信息
            trashBinAdapter.setOnClickListener(onClickListener);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), columnsCount());
            trashBinRecyclerView.setLayoutManager(gridLayoutManager);
            trashBinRecyclerView.setAdapter(trashBinAdapter);
        }
        setUpUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpUi();
        // 从数据库中初始化回收站之中的图片信息
        Realm.init(this);
        Realm realm = Realm.getDefaultInstance();
        trashBinRealmModelRealmQuery = realm.where(TrashBinRealmModel.class);
        // 初始化回收站中的照片信息
        ArrayList<TrashBinRealmModel> list = getTrashObjects();
        if (list.size() == 0) {
            trashBinRecyclerView.setVisibility(View.INVISIBLE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            trashBinAdapter.setResults(list);
        }
    }

    /**
     * 加载OptionsMenu信息
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_trashbin, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (trashBinRealmModelRealmQuery.count() == 0) {
            menu.findItem(R.id.trashbin_restore).setVisible(false);
            menu.findItem(R.id.delete_action).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.trashbin_restore:
                new RestoreAll().execute();
                return true;
            case R.id.delete_action:
                // 删除全部的回收站信息
                deleteAllMedia();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 删除全部的照片信息
     * @return
     */
    private boolean[] deleteAllMedia() {
        boolean[] delete = {false};
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(TrashBinActivity.this, ThemeHelper.getDialogStyle());
        AlertDialogsHelper.getTextDialog(this, deleteDialog, R.string.clear_trash_title, R.string.delete_all_trash, null);
        deleteDialog.setNegativeButton(getResources().getString(R.string.cancel), null);
        deleteDialog.setPositiveButton(getResources().getString(R.string.ok_action), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new DeleteAll().execute();
            }
        });

        AlertDialog alertDialogDelete = deleteDialog.create();
        alertDialogDelete.show();
        AlertDialogsHelper.setButtonTextColor(new int[]{DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE}, ThemeHelper.getAccentColor(this), alertDialogDelete);
        return delete;
    }

    /**
     * 清除全部回收站照片的后台任务（主要是删除回收站中的数据）
     */
    private class DeleteAll extends AsyncTask<Void, Void, Void> {
        private boolean[] delete = {false};

        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Realm.init(TrashBinActivity.this);
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<TrashBinRealmModel> results = realm.where(TrashBinRealmModel.class).findAll();
                    delete[0] = results.deleteAllFromRealm();
                }
            });
            File binFolder = new File(Environment.getExternalStorageDirectory() + "/" + ".nomedia");
            if (binFolder.exists()) {
                binFolder.delete();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            swipeRefreshLayout.setRefreshing(false);
            if (delete[0] && trashBinRealmModelRealmQuery.count() == 0) {
                emptyView.setVisibility(View.VISIBLE);
                trashBinAdapter.setResults(getTrashObjects());
                SnackBarHandler.showWithBottomMargin(parentView, getResources().getString(R.string.clear_all_success_mssg), 0, Snackbar.LENGTH_SHORT);
            }
        }
    }

    /**
     * 从回收站中恢复文件至原来的文件夹
     */
    private class RestoreAll extends AsyncTask<Void, Void, Void> {
        private int count = 0;  // 已经从回收站中被恢复的数量
        private int originalCount = 0;  // 原始回收站中的图片数量

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            deletedTrash = new ArrayList<>();
            Realm.init(TrashBinActivity.this);
            Realm realm = Realm.getDefaultInstance();
            // RealmQuery<TrashBinRealmModel> 缓存的Query信息
            RealmQuery<TrashBinRealmModel> trashBinRealmModelRealmQuery = realm.where(TrashBinRealmModel.class);
            originalCount = (int)trashBinRealmModelRealmQuery.count();
            // 从回收站中恢复
            for (int i = 0; i < originalCount; i ++) {
                if (restoreImage(trashBinRealmModelRealmQuery.findAll().get(i))) {
                    count ++;
                }
            }
            // 从记录数据中删除回收站的信息
            for (int i = 0; i < deletedTrash.size(); i ++) {
                removeFromRealm(deletedTrash.get(i).getTrashbinpath());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            swipeRefreshLayout.setRefreshing(false);
            trashBinAdapter.setResults(getTrashObjects());
            if (trashBinRealmModelRealmQuery.count() == 0) {
                emptyView.setVisibility(View.VISIBLE);
                SnackBarHandler.showWithBottomMargin(parentView,
                        String.valueOf(count) + " " + getResources().getString(R.string.restore_all_success_mssg),
                        0, Snackbar.LENGTH_SHORT);
            } else {
                SnackBarHandler.showWithBottomMargin(parentView,
                        String.valueOf(count) + " " + getResources().getString(R.string.restore_all_success_mssg) +
                                "，但是" + String.valueOf(originalCount - count) + " " +
                                getResources().getString(R.string.restore_all_fail_mssg),
                        0, Snackbar.LENGTH_SHORT);
            }
        }
    }

    /**
     * 删除数据库中记录的回收站中的照片信息
     * @param path
     * @return
     */
    private boolean removeFromRealm(String path) {
        boolean[] delete = {false};
        Realm.init(this);
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<TrashBinRealmModel> results = realm.where(TrashBinRealmModel.class).equalTo("trashbinpath", path).findAll();
                delete[0] = results.deleteAllFromRealm();
            }
        });
        return delete[0];
    }

    /**
     * 将回收站中的图片恢复到原来的文件夹
     * @param trashBinRealmModel
     * @return
     */
    private boolean restoreImage(TrashBinRealmModel trashBinRealmModel) {
        boolean result = false;
        String oldPath = trashBinRealmModel.getOldpath();
        String oldFolder = oldPath.substring(0, oldPath.lastIndexOf("/"));
        if (restoreMove(getApplicationContext(), trashBinRealmModel.getTrashbinpath(), oldFolder)) {
            result = true;
            deletedTrash.add(trashBinRealmModel);
        }
        return result;
    }

    private boolean restoreMove(Context context, String source, String targetDir) {
        File from = new File(source);
        File to = new File(targetDir);
        return FileUtils.moveFile(context, from, to);
    }

    /**
     * 计算adapter列数
     * @return
     */
    private int columnsCount() {
        // 竖屏2列，横屏3列
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 3;
    }

    /**
     * 设置Ui显示信息
     */
    private void setUpUi() {
        parentView.setBackgroundColor(ThemeHelper.getBackgroundColor(this));
        setupToolbar();
        // 设置刷新控件的颜色
        swipeRefreshLayout.setColorSchemeColors(ThemeHelper.getAccentColor(this));
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ThemeHelper.getBackgroundColor(this));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                trashBinAdapter.setResults(getTrashObjects());
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    /**
     * 设置工具栏信息
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.trash_bin));
        toolbar.setPopupTheme(ThemeHelper.getPopupToolbarStyle(this));
        toolbar.setBackgroundColor(ThemeHelper.getPrimaryColor(this));
        toolbar.setNavigationIcon(new IconicsDrawable(this).icon(CommunityMaterial.Icon.cmd_arrow_left).color(Color.WHITE).sizeDp(19));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private void trashEmptyViewSetup() {
        emptyIcon.setImageResource(R.drawable.ic_delete_sweep_black_24dp);
        trashText.setTextColor(ThemeHelper.getTextColor(this));
        trashMessage.setTextColor(ThemeHelper.getTextColor(this));
    }

}
