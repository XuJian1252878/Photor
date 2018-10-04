package com.photor.album.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;
import com.photor.R;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
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
