package com.photor.album.adapter;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.photor.R;
import com.photor.album.entity.Media;
import com.photor.util.BasicCallBack;

import java.util.ArrayList;
import java.util.zip.Inflater;

import static com.photor.util.ActivitySwitchHelper.context;
import static com.photor.util.ActivitySwitchHelper.getContext;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    BasicCallBack basicCallBack;
    private ArrayList<Media> media;
    private OnSingleTap onSingleTap;
    private EnterTransition mEnterTransitions;

    /**
     * Interface for calling up the toggleUI on single tap on the image.
     */
    public interface OnSingleTap {
        void singleTap();
    }

    /**
     * 在Glide完成加载图片资源的时候调用
     */
    public interface EnterTransition {
        void startPostponedTransition();
    }

    // 为了方便获取View的宽度和高度
    private void startPostponedTransition(final View sharedElement){
        // 在视图将要绘制时调用该监听事件，会被调用多次， OnPreDrawListener
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        // 抑制view的自己测量本身的高度和宽度（imageview设置为跟当前window的宽度和高度相同）
                        // 启动共享元素动画相关
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        mEnterTransitions.startPostponedTransition();
                        return true;
                    }
                });
    }

    public ImageAdapter(BasicCallBack basicCallBack, ArrayList<Media> media, OnSingleTap onSingleTap, EnterTransition mEnterTransitions) {
        this.basicCallBack = basicCallBack;
        this.media = media;
        this.onSingleTap = onSingleTap;
        this.mEnterTransitions = mEnterTransitions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.unit_image_pager, null, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 设置过度动画相关
        holder.imageView.setTransitionName(context.getString(R.string.transition_photo));

        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.DATA);

        Glide.with(context)
                .load(media.get(position).getUri())
                .apply(options)
                .thumbnail(0.5f)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        // 资源加载完成之后，抑制view的自己测量本身的高度和宽度
                        startPostponedTransition(holder.imageView);
                        return false;
                    }
                })
                .into(holder.imageView);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                basicCallBack.callBack(0, null);
            }
        });
    }

    @Override
    public int getItemCount() {
        return media.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = new ImageView(getContext());
            layout = itemView.findViewById(R.id.unit_image_pager_layout);
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            layout.addView(imageView);
        }
    }
}
