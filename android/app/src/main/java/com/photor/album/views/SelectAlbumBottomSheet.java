package com.photor.album.views;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.view.IconicsImageView;
import com.photor.MainApplication;
import com.photor.R;
import com.photor.album.entity.Album;
import com.photor.album.entity.FoldersFileFilter;
import com.photor.util.ThemeHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class SelectAlbumBottomSheet extends BottomSheetDialogFragment {


    private String title;
    private ArrayList<File> folders;

    private BottomSheetAlbumsAdapter adapter;

    private boolean canGoBack = false; //
    private IconicsImageView imgExploreMode;
    private TextView currentFolderPath;


    private SelectAlbumInterface selectAlbumInterface;  // 选择了某一个相册之后的回调接口

    public interface SelectAlbumInterface {
        // path 为当前被绑定的view相关联的 path信息
        void folderSelected(String path);
    }

    public void setSelectAlbumInterface(SelectAlbumInterface selectAlbumInterface) {
        this.selectAlbumInterface = selectAlbumInterface;
    }

    private boolean canGoBack() {
        return canGoBack;
    }


    /**
     * 收集当前文件夹的父文件夹，自文件夹的信息
     * @param dir
     */
    private void displayContentFolder(File dir) {
        canGoBack = false;
        if (dir.canRead()) {
            folders = new ArrayList<>();
            File parent = dir.getParentFile();
            if (parent.canRead()) {
                canGoBack = true;
                folders.add(0, parent);
            }
            File[] files = dir.listFiles(new FoldersFileFilter());
            if (files != null && files.length > 0) {
                folders.addAll(new ArrayList<>(Arrays.asList(files)));
                currentFolderPath.setText(dir.getAbsolutePath());
            }
            currentFolderPath.setText(dir.getAbsolutePath());
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 初始化当前相册文件夹的数据
     */
    private void toggleExplorerMode() {
        folders = new ArrayList<>();

        currentFolderPath.setText(getString(R.string.local_folder));
        for (Album album: ((MainApplication)getActivity().getApplicationContext()).getAlbums().dispAlbums) {
            folders.add(new File(album.getPath()));
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * 设置弹出式对话框的标题信息
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View contentView = View.inflate(getContext(), R.layout.select_folder_bottom_sheet, null);
        RecyclerView mRecyclerView = (RecyclerView) contentView.findViewById(R.id.folders);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        adapter = new BottomSheetAlbumsAdapter();
        mRecyclerView.setAdapter(adapter);

        currentFolderPath = (TextView) contentView.findViewById(R.id.bottom_sheet_sub_title);
        ThemeHelper.setColorScrollBarDrawable(getContext(), ContextCompat.getDrawable(dialog.getContext(), R.drawable.ic_scrollbar));

        toggleExplorerMode();  // 加载当前的相册folder信息

        contentView.findViewById(R.id.ll_bottom_sheet_title).setBackgroundColor(ThemeHelper.getPrimaryColor(getContext()));
        contentView.findViewById(R.id.ll_select_folder).setBackgroundColor(ThemeHelper.getCardBackgroundColor(getContext()));
        ((TextView) contentView.findViewById(R.id.bottom_sheet_title)).setText(title);


        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams layoutParams =
                (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
        adapter.notifyDataSetChanged();

    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }
        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String path = view.findViewById(R.id.name_folder).getTag().toString();
            selectAlbumInterface.folderSelected(path);
        }
    };

    private class BottomSheetAlbumsAdapter extends RecyclerView.Adapter<BottomSheetAlbumsAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // 根据parent的content 加载每一个item项的布局
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_folder_bottom_sheet_item, parent, false);
            v.setOnClickListener(onClickListener);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // 将对应的数据自相设置入对应的view holder中
            File f = folders.get(position);
            String[] list = f.list();
            int count = list == null ? 0 : list.length;

            holder.folderName.setText(f.getName());
            holder.folderName.setTag(f.getPath());

            // /storage/emulated/0 (主外部储存，在手机中)
            if (f.getPath().contains(Environment.getExternalStorageDirectory().getPath())) {
                holder.sdfolder.setVisibility(View.INVISIBLE);
            } else {
                holder.sdfolder.setVisibility(View.VISIBLE);
            }

            holder.cardViewParent.setCardBackgroundColor(ThemeHelper.getCardBackgroundColor(getContext()));
            holder.folderName.setTextColor(ThemeHelper.getTextColor(getContext()));

            holder.folderCount.setText(Html.fromHtml("<b><font color='" +
                    ThemeHelper.getTextColor(getContext()) + "'>" +
                    count + "</font></b>" + "<font " + "color='" +
                    ThemeHelper.getSubTextColor(getContext()) + "'> Media</font>"));
            holder.imgFolder.setColor(ThemeHelper.getIconColor(getContext()));
            holder.imgFolder.setIcon(ThemeHelper.getIcon(getContext(), CommunityMaterial.Icon.cmd_folder));
        }

        @Override
        public int getItemCount() {
            return folders.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView folderName;
            private TextView folderCount;
            private ImageView sdfolder;
            private CardView cardViewParent;
            private IconicsImageView imgFolder;

            public ViewHolder(View itemView) {
                super(itemView);
                folderName = (TextView) itemView.findViewById(R.id.name_folder);
                sdfolder = (ImageView) itemView.findViewById(R.id.sd_card_folder);
                folderCount = (TextView) itemView.findViewById(R.id.count_folder);
                imgFolder = (IconicsImageView) itemView.findViewById(R.id.folder_icon_bottom_sheet_item);
                cardViewParent = (CardView) itemView.findViewById(R.id.card_view);
            }

        }

    }

}
