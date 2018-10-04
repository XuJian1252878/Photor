package com.photor.album.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

public class AlbumsAdapter {

    private static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView picture;
        private View layout;


        // 每一个小项的视图
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

}
