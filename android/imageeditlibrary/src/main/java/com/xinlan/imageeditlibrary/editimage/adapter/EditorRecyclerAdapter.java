package com.xinlan.imageeditlibrary.editimage.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xinlan.imageeditlibrary.R;
import com.xinlan.imageeditlibrary.editimage.EditImageActivity;

/**
 * @author htwxujian@gmail.com
 * @date 2018/10/14 09:32
 */
public class EditorRecyclerAdapter extends RecyclerView.Adapter<EditorRecyclerAdapter.ViewHolder> {

    private TypedArray iconlist, titlelist;  // 每一个小项的type icon
    public static final String[] stickerPath = {"stickers/type1", "stickers/type2", "stickers/type3", "stickers/type4", "stickers/type5", "stickers/type6"};
    private Context context;
    private RecyclerView recyclerView;  // 设置为当前Adapter的Recycler

    private int fragmentMode; // 标记当前是处于哪一个设置fragment

    private int currentSelection;  // 当前选中项的下标
    private OnEditorItemClickListener onEditorItemClickListener;

    public interface OnEditorItemClickListener {
        public abstract void onEditorItemClick(int position, View itemView);
    }

    public EditorRecyclerAdapter(Context context, RecyclerView recyclerView, int fragmentMode,
                                 OnEditorItemClickListener onEditorItemClickListener) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.fragmentMode = fragmentMode;
        this.onEditorItemClickListener = onEditorItemClickListener;

        switch (fragmentMode) {
            case EditImageActivity.MODE_ENHANCE:
                iconlist = context.getResources().obtainTypedArray(R.array.enhance_icons);
                titlelist = context.getResources().obtainTypedArray(R.array.enhance_titles);
                break;
            case EditImageActivity.MODE_STICKERS:
                iconlist = context.getResources().obtainTypedArray(R.array.sticker_icons);
                titlelist = context.getResources().obtainTypedArray(R.array.sticker_titles);
                break;
            default:
                break;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.editor_iconitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        if (fragmentMode == EditImageActivity.MODE_STICKERS) {
            holder.itemView.setTag(stickerPath[position]);
        }

        int iconImageSize = (int) context.getResources().getDimension(R.dimen.icon_item_image_size_recycler);
        int midRowSize = (int) context.getResources().getDimension(R.dimen.editor_mid_row_size);

        holder.icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        // 设置icon资源
        holder.icon.setImageResource(iconlist.getResourceId(position, R.drawable.ic_photo_filter));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(iconImageSize, iconImageSize);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        holder.icon.setLayoutParams(layoutParams);
        holder.title.setText(titlelist.getString(position));
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(midRowSize, midRowSize);
        layoutParams1.gravity = Gravity.CENTER;
        holder.wrapper.setLayoutParams(layoutParams1);
        holder.wrapper.setBackgroundColor(Color.TRANSPARENT);

        if (currentSelection == position) {
            // 设置选中状态
            holder.wrapper.setBackgroundColor(ContextCompat.getColor(context, R.color.md_grey_200));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                highlightSelectedOption(position, view);
                if (onEditorItemClickListener != null) onEditorItemClickListener.onEditorItemClick(position, view);
                itemClicked(position, view);
            }
        });
    }

    @Override
    public int getItemCount() {
        return titlelist.length();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        LinearLayout wrapper;
        View view;

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            icon = itemView.findViewById(R.id.editor_item_image);
            title = itemView.findViewById(R.id.editor_item_title);
            wrapper = itemView.findViewById(R.id.ll_effect_wrapper);
        }
    }

    private void highlightSelectedOption(int position, View v) {
        int color = ContextCompat.getColor(v.getContext(), R.color.md_grey_200);
        if (currentSelection != position) {
            notifyItemChanged(currentSelection);
            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        }

        if (currentSelection != -1 && recyclerView.findViewHolderForAdapterPosition(currentSelection) != null) {
            // 设置未选中状态
            ((EditorRecyclerAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(currentSelection))
                    .wrapper
                    .setBackgroundColor(Color.TRANSPARENT);
        }

        ((ViewHolder) recyclerView.findViewHolderForAdapterPosition(position))
                .wrapper
                .setBackgroundColor(color);

        currentSelection = position;
    }

    private void itemClicked(int position, View view) {
        // 设置 滑动栏的 出现消失之类
        switch(fragmentMode) {
            case EditImageActivity.MODE_STICKERS:
                break;
            default:
                break;
        }
    }

}
