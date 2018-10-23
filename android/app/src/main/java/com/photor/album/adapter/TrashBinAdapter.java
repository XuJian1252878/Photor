package com.photor.album.adapter;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.file.FileUtils;
import com.photor.R;
import com.photor.data.TrashBinRealmModel;
import com.photor.util.ActivitySwitchHelper;
import com.photor.util.BasicCallBack;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * @author htwxujian@gmail.com
 * @date 2018/10/22 16:52
 */
public class TrashBinAdapter extends RecyclerView.Adapter<TrashBinAdapter.ViewHolder> {

    private ArrayList<TrashBinRealmModel> trashItemsList = null;  // 已经被删除的图片文件列表
    private View.OnClickListener onClickListener;
    private BasicCallBack basicCallBack;  // 操作回收站文件之后实时检出回收站的文件是不是为空

    public TrashBinAdapter(ArrayList<TrashBinRealmModel> list, BasicCallBack basicCallBack) {
        trashItemsList = list;
        this.basicCallBack = basicCallBack;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trashbin_item_view, null, false);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        view.setOnClickListener(onClickListener);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (trashItemsList.size() != 0) {
            TrashBinRealmModel trashBinRealmModel = trashItemsList.get(position);
            String date = trashBinRealmModel.getDatetime();

            try {
                DateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                Date parseDate = format.parse(date);
                DateFormat trashDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                DateFormat trashTimeFormat = new SimpleDateFormat("hh:mm:ss");
                holder.deleteDate.setText(trashDateFormat.format(parseDate));
                holder.deleteTime.setText(trashTimeFormat.format(parseDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            holder.deleteDate.setTag(trashBinRealmModel);
            Uri uri = Uri.fromFile(new File(trashBinRealmModel.getTrashbinpath()));
            RequestOptions options = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);

            Glide.with(holder.deletedImage.getContext())
                    .applyDefaultRequestOptions(options)
                    .load(uri)
                    .into(holder.deletedImage);

            holder.popupMenuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 显示浮动菜单信息
                    PopupMenu menu = new PopupMenu(ActivitySwitchHelper.getContext(), holder.popupMenuButton);
                    menu.inflate(R.menu.menu_popup_trashbin);
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch(menuItem.getItemId()) {
                                case R.id.restore_option:  // 从回收站中恢复
                                    restoreImage(trashBinRealmModel, position);
                                    if (trashItemsList.size() == 0) {
                                        basicCallBack.callBack(2, null);
                                    }
                                    return true;

                                case R.id.delete_permanently:  // 从回收站中永久删除

                                    if (deletePermanent(trashBinRealmModel)) {
                                        deleteFromRealm(trashItemsList.get(position).getTrashbinpath());
                                        trashItemsList.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, trashItemsList.size());
                                    }

                                    if (trashItemsList.size() == 0) {
                                        basicCallBack.callBack(2, null);
                                    }

                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    menu.show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return trashItemsList.size();
    }

    /**
     * 恢复回收站的文件至原始文件夹
     * @param trashBinRealmModel
     * @param position
     */
    private void restoreImage(TrashBinRealmModel trashBinRealmModel, int position) {
        String oldPath = trashBinRealmModel.getOldpath();
        String oldFolder = oldPath.substring(0, oldPath.lastIndexOf("/"));
        // 在文件系统中进行文件移动操作，并且更新mediaStorage库
        if (restoreMove(ActivitySwitchHelper.getContext(), trashBinRealmModel.getTrashbinpath(), oldFolder)) {
            // 在数据库中更新信息
            if (removeFromRealm(trashBinRealmModel.getTrashbinpath())) {
                trashItemsList.remove(position);
                // adapter更新信息
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, trashItemsList.size());  // 闭开区间
            }
        }
    }

    private boolean restoreMove(Context context, String source, String targetDir) {
        File from = new File(source);
        File to = new File(targetDir);
        // 移动成功的话会删除源文件，创建新文件，并且更新MediaStorage库
        return FileUtils.moveFile(context, from, to);
    }

    /**
     * 从数据库中删除回收站信息
     * @param path
     * @return
     */
    private boolean removeFromRealm(String path) {
        final boolean[] delete = {false};
        Realm.init(ActivitySwitchHelper.getContext());
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<TrashBinRealmModel> result = realm.where(TrashBinRealmModel.class).equalTo("trashbinpath", path).findAll();
                delete[0] = result.deleteAllFromRealm();
            }
        });
        return delete[0];
    }


    /**
     * 在文件系统中删除文件信息
     * @param trashBinRealmModel
     * @return
     */
    private boolean deletePermanent(TrashBinRealmModel trashBinRealmModel) {
        boolean success = false;
        String path = trashBinRealmModel.getTrashbinpath();
        File file = new File(Environment.getExternalStorageDirectory() + "/" + ".nomedia");
        if (file.exists()) {
            File file1 = new File(path);
            if (file1.exists()) {
                success = file1.delete();
            }
        }
        return success;
    }

    /**
     * 删除数据库中记录的回收站照片信息
     * @param path
     */
    private void deleteFromRealm(String path) {
        Realm.init(ActivitySwitchHelper.getContext());
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<TrashBinRealmModel> results = realm.where(TrashBinRealmModel.class).equalTo("trashbinpath", path).findAll();
                results.deleteAllFromRealm();
            }
        });
    }

    /**
     * 设置整个ItemView被点击时候的事件
     * @param listener
     */
    public void setOnClickListener(View.OnClickListener listener) {
        this.onClickListener = listener;
    }

    /**
     * 设置最终的结果信息
     * @param trashItemsList
     */
    public void setResults(ArrayList<TrashBinRealmModel> trashItemsList) {
        this.trashItemsList = trashItemsList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.delete_time)
        public TextView deleteTime;

        @BindView(R.id.delete_date)
        public TextView deleteDate;

        @BindView(R.id.trashbin_image)
        public ImageView deletedImage;

        @BindView(R.id.textViewOptions)
        public TextView popupMenuButton;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
