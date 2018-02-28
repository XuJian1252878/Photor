package com.photor.staralign.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.photopicker.utils.AndroidLifecycleUtils;
import com.photor.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xujian on 2018/2/28.
 */

public class StarPhotoAdapter extends RecyclerView.Adapter<StarPhotoAdapter.StarPhotoViewHolder> {

    private List<String> photoPaths = new ArrayList<>();
    private LayoutInflater inflator;
    private Context mContext;
    public final static int MAX_PHOTO_COUNT = 9;

    public final static int TYPE_PHOTO = 1;
    public final static int TYPE_ADD = 2;

    public StarPhotoAdapter(List<String> photoPaths, Context mContext) {
        this.photoPaths = photoPaths;
        this.mContext = mContext;
        inflator = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public StarPhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = null;
        switch(viewType) {
            case TYPE_PHOTO:
                itemView = inflator.inflate(R.layout.__picker_item_photo, parent, false);
                break;
            case TYPE_ADD:
                itemView = inflator.inflate(R.layout.__photo_picker_item_add, parent, false);
                break;
        }

        return new StarPhotoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(StarPhotoViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_PHOTO) {
            Uri uri = Uri.fromFile(new File(this.photoPaths.get(position)));

            // 图片选择界面的activity被关闭之后才能在主界面上 显示图片
            boolean canLoadImage = AndroidLifecycleUtils.canLoadImage(holder.ivPhoto.getContext());

            if (canLoadImage) {
                RequestOptions options = new RequestOptions();
                options.centerCrop()
                        .placeholder(R.drawable.__picker_ic_photo_black_48dp)
                        .error(R.drawable.__picker_ic_broken_image_black_48dp);

                Glide.with(mContext)
                        .load(uri)
                        .apply(options)
                        .thumbnail(0.1f)
                        .into(holder.ivPhoto);
            }
        }
    }

    @Override
    public int getItemCount() {
        int count = this.photoPaths.size() + 1;
        if (count > MAX_PHOTO_COUNT) {
            count = MAX_PHOTO_COUNT;
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == this.photoPaths.size() && position != MAX_PHOTO_COUNT) ? TYPE_ADD : TYPE_PHOTO;
    }

    public class StarPhotoViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivPhoto;
        private ImageView vSelected;

        public StarPhotoViewHolder(View itemView) {
            super(itemView);

            ivPhoto = itemView.findViewById(R.id.iv_photo);
            vSelected = itemView.findViewById(R.id.v_selected);

            if (vSelected != null) {
                vSelected.setVisibility(View.GONE);
            }
        }
    }

}
