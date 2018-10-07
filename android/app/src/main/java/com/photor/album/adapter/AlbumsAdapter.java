package com.photor.album.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.view.IconicsImageView;
import com.photor.MainApplication;
import com.photor.R;
import com.photor.album.entity.Album;
import com.photor.album.entity.Media;

import java.util.ArrayList;

import static com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {

    private ArrayList<Album> albums;
    private BitmapDrawable placeholder;
    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mOnLongClickListener;
    Context context;

    public AlbumsAdapter(ArrayList<Album> albums, Context context) {
        this.albums = albums;
        this.context = context;
        updateTheme();
    }

    public void updateTheme() {
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.placeholder);
        this.placeholder = (BitmapDrawable) drawable;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_album, parent, false);
        v.setOnClickListener(mOnClickListener);
        v.setOnLongClickListener(mOnLongClickListener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 获得对应的album信息
        Album a = ((MainApplication) context.getApplicationContext()).getAlbums().dispAlbums.get(position);
        Media f = a.getCoverAlbum();

        holder.storage.setVisibility(View.INVISIBLE);
        holder.pin.setVisibility(View.INVISIBLE);

        String hexAccentColor = "0xFFFFFF";

        RequestOptions requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .priority(Priority.HIGH)
                .centerCrop()
                .error(R.drawable.ic_error)
                .placeholder(placeholder);

        Glide.with(context)
                .asBitmap()
                .load(f.getUri())
                .apply(requestOptions)
//                .transition(withCrossFade(R.anim.fade_in))
                .into(holder.picture);

        holder.name.setTag(a);
        String textColor = "#FAFAFA";
        if (a.isSelected()) {
            holder.selectedIcon.setColor(Color.WHITE);
            holder.selectedIcon.setIcon(CommunityMaterial.Icon.cmd_check);
            holder.selectedIcon.setVisibility(View.VISIBLE);
            holder.layout.setBackgroundColor(Color.parseColor("#FF33B5E5"));
            holder.picture.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
        } else {
            holder.picture.clearColorFilter();
            holder.selectedIcon.setVisibility(View.GONE);
            holder.layout.setBackgroundColor(ColorUtils.setAlphaComponent(Color.rgb(200, 200, 200), 200));
        }

        String albumNameHtml = "<i><font color='" + textColor + "'>" + a.getName() + "</font></i>";
        String albumPhotoCountHtml = "<b><font color='" + hexAccentColor + "'>" + a.getCount() + "</font></b>" + "<font " +
                "color='" + textColor + "'> " + holder.nPhotos.getContext().getString(R.string.album_photo_count_html_media) + "</font>";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.name.setText(Html.fromHtml(albumNameHtml, Html.FROM_HTML_MODE_LEGACY));
            holder.nPhotos.setText(Html.fromHtml(albumPhotoCountHtml, Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.name.setText(Html.fromHtml(albumNameHtml));
            holder.nPhotos.setText(Html.fromHtml(albumPhotoCountHtml));
        }

    }

    public void setOnClickListener(View.OnClickListener lis) {
        mOnClickListener = lis;
    }

    public void setOnLongClickListener(View.OnLongClickListener lis) {
        mOnLongClickListener = lis;
    }

    public void swapDataSet(ArrayList<Album> asd) {
        // 因为改写了Album的equals，所以两个Album List中对应位置的Album的封面路径不一样的时候
        // 说明该Album的封面数据有更新，需要更新Album信息（相册有新增或者删除的情况）
//        if( ((MainApplication)context.getApplicationContext()).getAlbums().dispAlbums.equals(asd) ) {
////            return;
////        }
        ((MainApplication)context.getApplicationContext()).getAlbums().dispAlbums = asd;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return ((MainApplication)context.getApplicationContext()).getAlbums().dispAlbums.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView picture;
        private View layout;
        private IconicsImageView selectedIcon;
        private TextView name, nPhotos;
        private ImageView pin;
        private ImageView storage;

        // 每一个小项的视图
        public ViewHolder(View itemView) {
            super(itemView);
            picture = (ImageView) itemView.findViewById(R.id.album_preview);
            selectedIcon = (IconicsImageView) itemView.findViewById(R.id.selected_icon);
            layout = itemView.findViewById(R.id.linear_card_text);
            name = (TextView) itemView.findViewById(R.id.album_name);
            nPhotos = (TextView) itemView.findViewById(R.id.album_photos_count);
            pin = (ImageView) itemView.findViewById(R.id.icon_pinned);
            storage = (ImageView) itemView.findViewById(R.id.storage_icon);
        }
    }
}
