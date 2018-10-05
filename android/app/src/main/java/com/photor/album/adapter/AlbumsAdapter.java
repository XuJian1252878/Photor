package com.photor.album.adapter;

import android.content.Context;
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

import com.mikepenz.iconics.view.IconicsImageView;
import com.photor.R;
import com.photor.album.entity.Album;

import java.util.ArrayList;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {

    private ArrayList<Album> albums;
    private BitmapDrawable placeholder;
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
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 0;
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
