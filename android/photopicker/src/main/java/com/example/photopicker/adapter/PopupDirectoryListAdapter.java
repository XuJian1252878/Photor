package com.example.photopicker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.photopicker.R;
import com.example.media.image.entity.PhotoDirectory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xujian on 2018/2/6.
 */

public class PopupDirectoryListAdapter extends BaseAdapter {

    private List<PhotoDirectory> directories = new ArrayList<>();
    private RequestManager glide;

    public PopupDirectoryListAdapter(List<PhotoDirectory> directories, RequestManager glide) {
        this.directories = directories;
        this.glide = glide;
    }

    @Override
    public int getCount() {
        return this.directories.size();
    }

    @Override
    public Object getItem(int position) {
        return this.directories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return this.directories.get(position).hashCode();
    }

    // 自定义的getView ViewHolder操作中，getView放回的是已经填装好数据的ViewHolder，所以getView中的ViewHolder要做好填装数据的工作
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.__picker_item_directory, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // 获取holder最重要的一步就是bindData
        holder.bindData(directories.get(position));

        return convertView;
    }

    private class ViewHolder {
        private ImageView ivCover;
        private TextView tvName;
        private TextView tvCount;

        public ViewHolder(View rootView) {
            ivCover = rootView.findViewById(R.id.iv_dir_cover);
            tvName = rootView.findViewById(R.id.tv_dir_name);
            tvCount = rootView.findViewById(R.id.tv_dir_count);
        }

        public void bindData(PhotoDirectory directory) {
            RequestOptions options = new RequestOptions();
            options.dontAnimate()
                    .dontTransform()
                    .override(800, 800)
                    .placeholder(R.drawable.__picker_ic_photo_black_48dp)
                    .error(R.drawable.__picker_ic_broken_image_black_48dp);

            glide.setDefaultRequestOptions(options)
                    .load(directory.getCoverPath())
                    .thumbnail(0.5f)
                    .into(ivCover);

            tvName.setText(directory.getName());
            tvCount.setText(tvCount.getContext().getString(R.string.__picker_image_count, directory.getPhotos().size()));
        }
    }
}
