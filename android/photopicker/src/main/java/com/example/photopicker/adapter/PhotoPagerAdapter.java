package com.example.photopicker.adapter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.lifecycle.AndroidLifecycleUtils;
import com.example.photopicker.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xujian on 2018/2/8.
 */

/**
 * A very simple PagerAdapter may choose to use the page Views themselves as key objects, returning
 * them from instantiateItem(ViewGroup, int) after creation and adding them to the parent ViewGroup.
 * A matching destroyItem(ViewGroup, int, Object) implementation would remove the View from the
 * parent ViewGroup and isViewFromObject(View, Object) could be implemented as return view == object;
 * 实现图片文件夹下的翻页效果
 */

public class PhotoPagerAdapter extends PagerAdapter {

    private List<String> paths = new ArrayList<>();
    private RequestManager mGlide;

    public PhotoPagerAdapter(List<String> paths, RequestManager mGlide) {
        this.paths = paths;
        this.mGlide = mGlide;
    }

    // Return the number of views available.
    @Override
    public int getCount() {
        return paths.size();
    }

    // Determines whether a page View is associated with a specific key object as returned by
    // instantiateItem(ViewGroup, int). This method is required for a PagerAdapter to function properly.
    @Override
    public boolean isViewFromObject(View view, Object object) {
        // 在这里 view 是instantiateItem(ViewGroup container, int position) 中对应位置取出的
        // object 是 instantiateItem(ViewGroup container, int position) 返回的key object
        // key object是一个必须包含 view的对象，所以在 isViewFromObject 中需要比较 view 与 object中包含的view
        // 是否相同。以为这里直接将 view 作为object，所以 直接 view == object 这样子判断
        return view == object;
    }

    // 这里的 object 是key object，一定要是包含view的对象
    // container 就是缓冲的图片加载队列，这里已经设置成五张图片，随着用户滑动图片，就有缓冲区图片的 instantiateItem
    // 和 destroyItem 操作
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final Context context = container.getContext();

        // 在这里加载每一个ViewPager的View对象
        View itemView = LayoutInflater.from(context).inflate(R.layout.__picker_item_pager, container, false);

        ImageView imageView = itemView.findViewById(R.id.iv_pager);

        String path = paths.get(position);
        Uri uri;

        // 网络图片
        if (path.startsWith("http")) {
            // Creates a Uri which parses the given encoded URI string.
            uri = Uri.parse(path);
        } else {
            // 这个uri是供glide使用的，不是供系统使用的（比如照相机，所以不需要用FileProvider）
            uri = Uri.fromFile(new File(path));
        }

        boolean canLoadImage = AndroidLifecycleUtils.canLoadImage(context);

        if (canLoadImage) {
            RequestOptions options = new RequestOptions();
            options.dontAnimate()
                    .dontTransform()
                    .override(800, 800)  // 实际上都会按照 xml 布局文件中的布局来
                    .placeholder(R.drawable.__picker_ic_photo_black_48dp)
                    .error(R.drawable.__picker_ic_broken_image_black_48dp);
            mGlide.setDefaultRequestOptions(options)
                    .load(uri)
                    .thumbnail(0.5f)
                    .into(imageView);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在图片预览界面单击图片，使得图片退出
                if (context instanceof Activity) {
                    if (!((Activity) context).isFinishing()) {
                        ((Activity) context).onBackPressed();
                    }
                }
            }
        });

        // 这一步是必须的，在设置完成itemView的事件以及数据之后，必须将itemView加入container
        container.addView(itemView);
        return itemView;  // 直接使用itemView对象作为Key Object
    }

    // Called when the host view is attempting to determine if an item's position has changed.
    // Returns POSITION_UNCHANGED if the position of the given item has not changed or
    // POSITION_NONE if the item is no longer present in the adapter.
    // The default implementation assumes that items will never change position and always returns POSITION_UNCHANGED.

    /**
     * Viewpager 的刷新过程是这样的，在每次调用 PagerAdapter 的 notifyDataSetChanged() 方法时，
     * 都会激活 getItemPosition(Object object) 方法，该方法会遍历 ViewPager 的所有 Item
     * （由缓存的 Item 数量决定，默认为当前页和其左右加起来共3页，这个可以自行设定，但是至少会缓存2页），
     * 为每个 Item 返回一个状态值（POSITION_NONE/POSITION_UNCHANGED），如果是 POSITION_NONE，
     * 那么该 Item 会被 destroyItem(ViewGroup container, int position, Object object) 方法 remove 掉，
     * 然后重新加载，如果是 POSITION_UNCHANGED，就不会重新加载，默认是 POSITION_UNCHANGED，所以如果不重写
     * getItemPosition(Object object)，修改返回值，就无法看到 notifyDataSetChanged() 的刷新效果。
     * @param object
     * @return
     */
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    // Remove a page for the given position. The adapter is responsible for removing the view from
    // its container, although it only must ensure this is done by the time it returns from finishUpdate(ViewGroup).
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // 如果view不在缓存范围之内，那么需要取消 view中glide的请求
        container.removeView((View) object);
        // Cancel any pending loads Glide may have for the view and
        // free any resources that may have been loaded for the view.
        mGlide.clear((View) object);
    }
}
