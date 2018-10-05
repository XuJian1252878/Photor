package com.photor.album.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.view.IconicsImageView;
import com.photor.R;
import com.photor.album.entity.Media;

import java.lang.reflect.Array;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

    private ArrayList<Media> medias;
    private BitmapDrawable placeholder;
    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mOnLongClickListener;
    private boolean fav = false;
    Context context;

    public MediaAdapter(ArrayList<Media> ph, Context context) {
        this.medias = ph;
        this.context = context;
        updatePlaceholder(context);
    }

    public void updatePlaceholder(Context context) {
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.placeholder);
        placeholder = (BitmapDrawable) drawable;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_photo, parent, false);
        v.setOnClickListener(mOnClickListener);
        v.setOnLongClickListener(mOnLongClickListener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Media f = medias.get(position);

        holder.path.setTag(f);
        holder.icon.setVisibility(View.GONE);

        RequestOptions myOptions = new RequestOptions()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .placeholder(placeholder);
        Glide.with(holder.imageView.getContext())
                .asBitmap()
                .load(f.getUri())
                .transition(withCrossFade(R.anim.fade_in))
                .apply(myOptions)
                .thumbnail(0.5f)
                .into(holder.imageView);

        holder.path.setVisibility(View.GONE);
        holder.icon.setVisibility(View.GONE);

        if (f.isSelected()) {
            // 设置图片被选择的时候，图片预览布局上显示出已经选择的图标
            holder.icon.setIcon(CommunityMaterial.Icon.cmd_check);
            holder.icon.setVisibility(View.VISIBLE);
            holder.imageView.setColorFilter(0x88000000, PorterDuff.Mode.SRC_ATOP);
            holder.layout.setPadding(15, 15, 15, 15);
        } else {
            holder.imageView.clearColorFilter();
            holder.layout.setPadding(0, 0, 0, 0);
        }

    }

    @Override
    public int getItemCount() {
        return medias.size();
    }

    public void setOnClickListener(View.OnClickListener lis) {
        mOnClickListener = lis;
    }

    public void setOnLongClickListener(View.OnLongClickListener lis) {
        mOnLongClickListener = lis;
    }

    public void swapDataSet(ArrayList<Media> asd, boolean fav) {
        this.medias = asd;
        this.fav = fav;
        // 数据变动，通知adapter更新界面信息
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.photo_preview)
        protected ImageView imageView;
        @BindView(R.id.media_card_layout)
        protected View layout;
        @BindView(R.id.photo_path)
        protected TextView path;
        @BindView(R.id.icon)
        protected IconicsImageView icon;

        public ViewHolder(View itemView) {

            super(itemView);
            // 相当于以itemView 作为基础 findViewById
            ButterKnife.bind(this, itemView);
        }
    }

}
