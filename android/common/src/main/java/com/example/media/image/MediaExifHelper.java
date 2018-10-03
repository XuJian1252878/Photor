package com.example.media.image;

import android.media.ExifInterface;

import com.example.media.GpsUtil;
import com.orhanobut.logger.Logger;

import java.io.IOException;

public class MediaExifHelper {

    public static float getExposureTime(String imagePath) {
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);
            float exposureTime = Float.valueOf(exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME));
            return exposureTime;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getPhotoTokenDate(String imagePath) {
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);
            String photoTokenTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            return photoTokenTime == null ? "无" : photoTokenTime;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 返回原始的exif信息
     * @param imagePath
     * @return
     */
    public static ExifInterface getOriExifInfo(String imagePath) {
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
            Logger.d(e.getMessage());
        }
        return exifInterface;
    }

    public static String getExifLocation(String imagePath) {
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);
            String latitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String longitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);

            if (latitude == null || longitude == null) {
                return "无";
            }

            //转换经纬度格式
            double lat = score2dimensionality(latitude);
            double lon = score2dimensionality(longitude);
            /**
             * 将wgs坐标转换成百度坐标
             * 就可以用这个坐标通过百度SDK 去获取该经纬度的地址描述
             */
            double[] wgs2bd = GpsUtil.wgs2bd(lat, lon);
            return String.valueOf(wgs2bd[0]) + "," + String.valueOf(wgs2bd[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void getExifInfo(String imagePath) {
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);

            String guangquan = exifInterface.getAttribute(ExifInterface.TAG_APERTURE);
            String shijian = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            String baoguangshijian = exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
            String jiaoju = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
            String chang = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
            String kuan = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
            String moshi = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
            String zhizaoshang = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
            String iso = exifInterface.getAttribute(ExifInterface.TAG_ISO);
            String jiaodu = exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
            String baiph = exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE);
            String altitude_ref = exifInterface.getAttribute(ExifInterface
                    .TAG_GPS_ALTITUDE_REF);
            String altitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
            String latitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String latitude_ref = exifInterface.getAttribute(ExifInterface
                    .TAG_GPS_LATITUDE_REF);
            String longitude_ref = exifInterface.getAttribute(ExifInterface
                    .TAG_GPS_LONGITUDE_REF);
            String longitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String timestamp = exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
            String processing_method = exifInterface.getAttribute(ExifInterface
                    .TAG_GPS_PROCESSING_METHOD);

            //转换经纬度格式
            double lat = score2dimensionality(latitude);
            double lon = score2dimensionality(longitude);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("光圈 = " + guangquan+"\n")
                    .append("时间 = " + shijian+"\n")
                    .append("曝光时长 = " + baoguangshijian+"\n")
                    .append("焦距 = " + jiaoju+"\n")
                    .append("长 = " + chang+"\n")
                    .append("宽 = " + kuan+"\n")
                    .append("型号 = " + moshi+"\n")
                    .append("制造商 = " + zhizaoshang+"\n")
                    .append("ISO = " + iso+"\n")
                    .append("角度 = " + jiaodu+"\n")
                    .append("白平衡 = " + baiph+"\n")
                    .append("海拔高度 = " + altitude_ref+"\n")
                    .append("GPS参考高度 = " + altitude+"\n")
                    .append("GPS时间戳 = " + timestamp+"\n")
                    .append("GPS定位类型 = " + processing_method+"\n")
                    .append("GPS参考经度 = " + latitude_ref+"\n")
                    .append("GPS参考纬度 = " + longitude_ref+"\n")
                    .append("GPS经度 = " + lat+"\n")
                    .append("GPS经度 = " + lon+"\n");

            /**
             * 将wgs坐标转换成百度坐标
             * 就可以用这个坐标通过百度SDK 去获取该经纬度的地址描述
             */
            double[] wgs2bd = GpsUtil.wgs2bd(lat, lon);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * 将 112/1,58/1,390971/10000 格式的经纬度转换成 112.99434397362694格式
     * @param string 度分秒
     * @return 度
     */
    private static double score2dimensionality(String string) {
        double dimensionality = 0.0;
        if (null==string){
            return dimensionality;
        }

        //用 ，将数值分成3份
        String[] split = string.split(",");
        for (int i = 0; i < split.length; i++) {

            String[] s = split[i].split("/");
            //用112/1得到度分秒数值
            double v = Double.parseDouble(s[0]) / Double.parseDouble(s[1]);
            //将分秒分别除以60和3600得到度，并将度分秒相加
            dimensionality=dimensionality+v/Math.pow(60,i);
        }
        return dimensionality;
    }
}
