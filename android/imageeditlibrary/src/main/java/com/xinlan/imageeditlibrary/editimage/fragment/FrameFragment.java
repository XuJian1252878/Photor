package com.xinlan.imageeditlibrary.editimage.fragment;

import android.app.Dialog;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.xinlan.imageeditlibrary.BaseActivity;
import com.xinlan.imageeditlibrary.R;
import com.xinlan.imageeditlibrary.editimage.EditImageActivity;
import com.xinlan.imageeditlibrary.editimage.ModuleConfig;
import com.xinlan.imageeditlibrary.editimage.view.imagezoom.ImageViewTouchBase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * @author htwxujian@gmail.com
 * @date 2018/10/15 09:58
 */
public class FrameFragment extends BaseEditFragment {

    private static final String TAG = "FrameFragment";
    private static Bitmap original;  // 原始的图片信息
    private RecyclerView frameRecycler;  // 加载相框的RecyclerView
    private ArrayList<Bitmap> arrayList = null;  // 相框的缩略图列表
    private Bitmap lastBitmap;  // 记录上一次经过相框处理的图片信息
    private int lastFrame = 99;  // 当前选择的相框的下标信息
    private View frameView;  // 整个fragment的主界面
    private ImageButton imgBtnDone, imgBtnCancel;  // 相框左右两侧的按钮

    public static final int INDEX = ModuleConfig.INDEX_FRAME;


    public static FrameFragment newInstance() {
        Bundle args = new Bundle();
        FrameFragment fragment = new FrameFragment();
        fragment.setArguments(args);
        // ARGB_8888 代表32位ARGB位图
        // ViewPager在加载的时候主界面的mainImage bitmap还没有加载好，所以发生错误
//        original = bmp.copy(Bitmap.Config.ARGB_8888, true);  // 深度拷贝一份bitmap
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        frameView = inflater.inflate(R.layout.fragment_editor_frames, null);
        return frameView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        frameRecycler = frameView.findViewById(R.id.frameRecyler);
//        imgBtnDone = frameView.findViewById(R.id.done);
        imgBtnCancel = frameView.findViewById(R.id.cancel);
//        imgBtnCancel.setImageResource(R.drawable.ic_close_black_24dp);
//        imgBtnDone.setImageResource(R.drawable.ic_done_black_24dp);

//        onShow();
        setUpLayoutManager();

        FrameAdapter frameAdapter = new FrameAdapter();
        frameRecycler.setAdapter(frameAdapter);

        imgBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.mainImage.setImageBitmap(original);
                backToMain();
            }
        });

//        imgBtnDone.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!lastBitmap.sameAs(original)) {
//                    activity.mainImage.setImageBitmap(lastBitmap);
//                }
//            }
//        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lastBitmap = null;
        System.gc();
    }

    /**
     * 设置RecyclerView的LayoutManager
     */
    private void setUpLayoutManager() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        frameRecycler.setLayoutManager(layoutManager);
    }

    @Override
    public void onShow() {
        activity.setBottomGalleryHeight(R.dimen.editor_frame_mid_row_size);
        activity.mode = EditImageActivity.MODE_FRAME;
        activity.bannerFlipper.showNext();
        // 设置主页面显示的图片信息
        activity.mainImage.setImageBitmap(activity.getMainBit());
        activity.mainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        original = activity.getMainBit().copy(Bitmap.Config.ARGB_8888, true);  // 深度拷贝一份bitmap

        /**
         * 执行加载相框缩略图任务
         */
        AsyncThumbs asyncThumbs = new AsyncThumbs();
        asyncThumbs.execute();
    }

    /**
     * 在主界面的应用按钮被点击之后
     */
    public void applyFrame() {
        if (!lastBitmap.sameAs(original)) {
            activity.changeMainBitmap(lastBitmap, true);
            backToMain();
        }
    }

    @Override
    public void backToMain() {
        activity.setBottomGalleryHeight(R.dimen.bottom_banner_height);
        setVisibility(false);
        activity.mode = EditImageActivity.MODE_NONE;
        activity.mainImage.setImageBitmap(activity.getMainBit());
        activity.bottomGallery.setCurrentItem(0);
        activity.bannerFlipper.showPrevious(); // 应用 -> 保存
    }

    /**
     * 返回当前取消按钮是否可见
     * @return
     */
    private boolean checkVisibility() {
        return imgBtnCancel.getVisibility() == View.VISIBLE;
    }

    /**
     * 设置确定取消按钮的可见状态
     * @param visibility
     */
    private void setVisibility(boolean visibility) {
        if (visibility) {
//            imgBtnDone.setVisibility(View.VISIBLE);
            imgBtnCancel.setVisibility(View.VISIBLE);
        } else {
//            imgBtnDone.setVisibility(View.GONE);
            imgBtnCancel.setVisibility(View.GONE);
        }
    }

    /**
     * 加载处于position位置的相框信息
     * @param pos
     */
    private void loadFrame(int pos) {
        new AsyncFrame().execute(pos);
    }

    // 异步加载相册的缩略图信息
    private class AsyncThumbs extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            arrayList = new ArrayList<>();
            InputStream in = null;
            Bitmap tempBitmap;
            String frameFolder = "frames";
            AssetManager assetManager = getResources().getAssets();
            try {
                String[] list = assetManager.list(frameFolder);  // 列出asset/frames 文件夹下的文件信息
                for (int file = 0; file < list.length; file ++) {
                    in = assetManager.open(frameFolder + File.separator + file + ".png");  // 相框文件是以数字直接命名的
                    // 创建一个新的、缩放后的bitmap
                    tempBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(in),
                            140,
                            160,
                            false);
                    arrayList.add(tempBitmap);
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (arrayList == null) {
                return;
            }
            frameRecycler.getAdapter().notifyDataSetChanged();
        }
    }

    /**
     * 相框加载的Adapter信息
     */
    private class FrameAdapter extends RecyclerView.Adapter<FrameAdapter.ViewHolder> {


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.frames, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

            if (arrayList == null || arrayList.size() <= 0) {
                return;
            }

            int frameImageSize = (int)getResources().getDimension(R.dimen.icon_item_image_size_frame_preview);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(frameImageSize, frameImageSize);
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            holder.imageView.setLayoutParams(layoutParams);
            holder.imageView.setImageBitmap(arrayList.get(position));
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!checkVisibility()) {
                        setVisibility(true);
                    }

                    // 如果当前选中的相框不是之前选中的相框，那么需要重新加载相框信息
                    if (lastFrame != position) {
                        loadFrame(position);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return arrayList == null ? 0 : arrayList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private ImageView imageView;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.frame_image);
            }
        }
    }

    /**
     * 加载处于position位置的Frame信息
     */
    private class AsyncFrame extends AsyncTask<Integer, Integer, Bitmap> {

        private Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = BaseActivity.getLoadingDialog(getActivity(), R.string.handing, false);
            dialog.show();
        }

        @Override
        protected Bitmap doInBackground(Integer... integers) {
            int position = integers[0]; // execute 传入的参数信息
            return drawFrame(position);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            activity.mainImage.setImageBitmap(bitmap);
            lastBitmap = bitmap;
            dialog.dismiss();
        }

        /**
         * 将选择的相框信息整合原图
         * @param pos
         * @return
         */
        private Bitmap drawFrame(int pos) {
            InputStream is;
            try {
                if (original != null && pos < 11) {  //  原始图像不为空，数量不超过相框的总数
                    is = getResources().getAssets().open("frames" + File.separator + pos + ".png");
                    Offset of = offset(pos);
                    int width = of.getWidth();
                    int height = of.getHeight();

                    Bitmap main = original;
                    Bitmap temp = main.copy(Bitmap.Config.ARGB_8888, true);  // 拷贝一份原始的图片信息
                    Bitmap frame = BitmapFactory.decodeStream(is).copy(Bitmap.Config.ARGB_8888, true);
                    is.close();

                    // 创建能刚好匹配原始图像的相框信息
                    Bitmap draw = Bitmap.createScaledBitmap(frame, 2 * width + temp.getWidth(), 2 * height + temp.getHeight(), false);

                    of = null;
                    of = offset(draw);
                    int widthForTemp = of.getWidth();
                    int heightForTemp = of.getHeight();

                    // 根据缩放后的相框信息创建Bitmap
                    // createBitmap() mutable = true；BitmapFactory.decodeResource  isMutable=false
                    Bitmap lastBitmap = Bitmap.createBitmap(2 * widthForTemp + temp.getWidth(), 2 * heightForTemp + temp.getHeight(), Bitmap.Config.ARGB_8888);
                    Bitmap frameNew = Bitmap.createScaledBitmap(frame, 2 * widthForTemp + temp.getWidth(), 2 * heightForTemp + temp.getHeight(), false);
                    frame.recycle();

                    Canvas can = new Canvas(lastBitmap);  // 以创建的 lastBitmap 作为Canvas画板的底
                    can.drawBitmap(temp, widthForTemp, heightForTemp, null); // 将原图信息画到canvas上
                    can.drawBitmap(frameNew, 0, 0, null); // 绘制相框信息

                    temp.recycle();
                    frameNew.recycle();
                    lastFrame = pos;
                    return lastBitmap;
                } else {
                    Bitmap temp = original.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas can = new Canvas(temp);  // 直接以原图片作为画面的底部
                    is = getResources().getAssets().open("frames" + File.separator + pos + ".png");
                    Bitmap frame = BitmapFactory.decodeStream(is);  // bitmap decode来的不能改动，所以需要再copy或者create一遍
                    is.close();
                    Bitmap frameNew = Bitmap.createScaledBitmap(frame, temp.getWidth(), temp.getHeight(), false);
                    can.drawBitmap(frameNew, 0, 0, null);
                    frameNew.recycle();
                    frame.recycle();
                    lastFrame = pos;
                    return temp;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        private class Offset {
            private int width, height;

            public Offset(int width, int height) {
                this.width = width;
                this.height = height;
            }

            public int getWidth() {
                return width;
            }

            public void setWidth(int width) {
                this.width = width;
            }

            public int getHeight() {
                return height;
            }

            public void setHeight(int height) {
                this.height = height;
            }
        }

        /**
         * 获取相框边框的宽度和高度
         * @param pos
         * @return
         */
        private Offset offset(int pos) {
            int point_x = 0;
            int point_y = 0;
            int width_off = 0;
            int height_off = 0;
            Bitmap temp = null;
            try {
                temp = BitmapFactory.decodeStream(getResources().getAssets().open("frames" + File.separator + pos + ".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (temp.getPixel(point_x, temp.getHeight() / 2) != 0) {
                point_x ++;
                width_off ++;
            }

            while (temp.getPixel(temp.getWidth() / 2, point_y) != 0) {
                point_y ++;
                height_off ++;
            }

            return new Offset(width_off + 2, height_off + 2);
        }

        /**
         * 根据bitmap信息
         * @param bitmap
         * @return
         */
        private Offset offset(Bitmap bitmap) {
            int point_x = 0;
            int point_y = 0;
            int width_off = 0;
            int height_off = 0;
            Bitmap temp = null;
            // bitmap 不可改变的话就不能在 Canvas 上进行绘制
            if (bitmap.isMutable()) {  // bitmap的像素是否可以被外界改变
                temp = bitmap;
            } else {
                temp = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            }

            while (temp.getPixel(point_x, temp.getHeight() / 2) != 0) {
                point_x ++;
                width_off ++;
            }

            while (temp.getPixel(temp.getWidth() / 2, point_y) != 0) {
                point_y ++;
                height_off ++;
            }

            return new Offset(width_off + 2, height_off + 2);
        }

    }
}
