package com.photor.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.view.Display;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xujian on 2018/3/5.
 */

public class ImageUtils {

    private static final Bitmap.Config BITMAP_CONFIG_ARGB_8888;

    public static final Bitmap createBitmapFromPath(String path, Context context) {
        WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int screenW = display.getWidth();
        int screenH = display.getHeight();
        return createBitmapFromPath(path, context, screenW, screenH);
    }


    public static int computeBitmapSimple(int realPixels, int maxPixels) {
        try {
            if(realPixels <= maxPixels) {
                return 1;
            } else {
                int scale;
                for(scale = 2; realPixels / (scale * scale) > maxPixels; scale *= 2) {
                }

                return scale;
            }
        } catch (Exception var3) {
            return 1;
        }
    }

    public static int getBitmapExifRotate(String path) {
        int digree = 0;
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(path);
        } catch (IOException var4) {
            var4.printStackTrace();
            return 0;
        }

        if(exif != null) {
            int ori = exif.getAttributeInt("Orientation", 0);
            switch(ori) {
                case 3:
                    digree = 180;
                    break;
                case 6:
                    digree = 90;
                    break;
                case 8:
                    digree = 270;
                    break;
                default:
                    digree = 0;
            }
        }

        return digree;
    }

    public static Bitmap rotate(Context context, Bitmap bitmap, int degree, boolean isRecycle) {
        Matrix m = new Matrix();
        m.setRotate((float)degree, (float)bitmap.getWidth() / 2.0F, (float)bitmap.getHeight() / 2.0F);

        try {
            Bitmap bm1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            if(isRecycle) {
                bitmap.recycle();
            }

            return bm1;
        } catch (OutOfMemoryError var6) {
            var6.printStackTrace();
            return null;
        }
    }

    public static Bitmap rotateBitmapByExif(Bitmap bitmap, String path, boolean isRecycle) {
        int digree = getBitmapExifRotate(path);
        if(digree != 0) {
            bitmap = rotate((Context)null, bitmap, digree, isRecycle);
        }

        return bitmap;
    }

    public static final Bitmap createBitmapFromPath(String path, Context context, int maxResolutionX, int maxResolutionY) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = null;

        try {
            options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            int width = options.outWidth;
            int height = options.outHeight;
            options.inSampleSize = computeBitmapSimple(width * height, maxResolutionX * maxResolutionY);
            options.inPurgeable = true;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inDither = false;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(path, options);
            return rotateBitmapByExif(bitmap, path, true);
        } catch (OutOfMemoryError var8) {
            options.inSampleSize *= 2;
            bitmap = BitmapFactory.decodeFile(path, options);
            return rotateBitmapByExif(bitmap, path, true);
        } catch (Exception var9) {
            var9.printStackTrace();
            return null;
        }
    }

    public static int dp2px(Context context, float dp) {
        return (int)(context.getResources().getDisplayMetrics().density * dp + 0.5F);
    }

    public static Bitmap getBitmapFromDrawable(Drawable drawable) {
        if(drawable == null) {
            return null;
        } else if(drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        } else {
            try {
                Bitmap bitmap;
                if(drawable instanceof ColorDrawable) {
                    bitmap = Bitmap.createBitmap(2, 2, BITMAP_CONFIG_ARGB_8888);
                } else {
                    bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG_ARGB_8888);
                }

                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                return bitmap;
            } catch (Exception var3) {
                var3.printStackTrace();
                return null;
            }
        }
    }

    static {
        BITMAP_CONFIG_ARGB_8888 = Bitmap.Config.ARGB_8888;
    }


    /**
     * 加载原始图像信息
     * @param path
     * @return
     */
    public static Bitmap loadImageByPath(String path, int sampleSize) {
        // 加载原始图像信息
        Bitmap bitmap = null;
        BitmapFactory.Options options = null;

        try {
            options = new BitmapFactory.Options();
            options.inPurgeable = true;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inDither = false;
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            bitmap = BitmapFactory.decodeFile(path, options);
            return bitmap;
        } catch (OutOfMemoryError var8) {
            options.inSampleSize *= 2;
            bitmap = BitmapFactory.decodeFile(path, options);
            return bitmap;
        } catch (Exception var9) {
            var9.printStackTrace();
            return null;
        }
    }

    /**
     * @param bitmap
     * @param memoryThreshold  M 为单位 默认为2M
     * @return
     */
    public static int getImageSampleSizeByMemoryThreshold(Bitmap bitmap, int memoryThreshold) {
        int byteCount = bitmap.getByteCount();
        int sampleSize = 1;
        while ( byteCount / 1024 / 1024 > memoryThreshold ) {
            sampleSize *= 2;
            byteCount /= 2;

            // 图片长宽有一端缩减到1000以下的时候，停止压缩操作
            if (bitmap.getWidth() / sampleSize < 1000 || bitmap.getHeight() / sampleSize < 1000) {
                break;
            }
        }
        return sampleSize;
    }

    /**
     * 获取比例被压缩后的Bitmap
     * @param paths
     * @param memoryThreshold
     * @return
     */
    public static List<Bitmap> getCompressedImage(List<String> paths, int memoryThreshold) {

        if (paths == null || paths.size() <= 0) {
            return null;
        }

        Bitmap sampleSizeBmp = loadImageByPath(paths.get(0), 1);
        int sampleSize = getImageSampleSizeByMemoryThreshold(sampleSizeBmp, memoryThreshold);

        List<Bitmap> bitmaps = new ArrayList<>();
        for (int index = 0; index < paths.size(); index ++) {
            Bitmap bitmap = loadImageByPath(paths.get(index), sampleSize);
            bitmaps.add(bitmap);
        }

        return bitmaps;
    }

}
