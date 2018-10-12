package com.example.media.image;

import android.media.ExifInterface;

import com.example.media.GpsUtil;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.text.DecimalFormat;

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

    public static String getExifInfo(String imagePath) {

        DecimalFormat decimalFormat = new DecimalFormat("##0.000");

        String nullValue = "无";

        StringBuilder stringBuilder = new StringBuilder();
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);

            String guangquan = exifInterface.getAttribute(ExifInterface.TAG_APERTURE);
            String shijian = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);

            String baoguangshijian = exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
            baoguangshijian = baoguangshijian != null ? decimalFormat.format(Double.valueOf(baoguangshijian)) : null;

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

            stringBuilder.append("光圈 = " + (guangquan == null ? nullValue : guangquan) + "\n")
                    .append("时间 = " + (shijian == null ? nullValue : shijian) + "\n")
                    .append("曝光时长 = " + (baoguangshijian == null ? nullValue : baoguangshijian) + "\n")
                    .append("焦距 = " + (jiaoju == null ? nullValue : jiaoju) + "\n")
                    .append("长 = " + (chang == null ? nullValue : chang) + "\n")
                    .append("宽 = " + (kuan == null ? nullValue : kuan) + "\n")
                    .append("型号 = " + (moshi == null ? nullValue :moshi) + "\n")
                    .append("制造商 = " + (zhizaoshang == null ? nullValue : zhizaoshang) + "\n")
                    .append("ISO = " + (iso == null ? nullValue : iso) + "\n")
                    .append("角度 = " + (jiaodu == null ? nullValue : jiaodu) + "\n")
                    .append("白平衡 = " + (baiph == null ? nullValue : baiph) + "\n")
                    .append("海拔高度 = " + (altitude_ref == null ? nullValue : altitude_ref) + "\n")
                    .append("GPS参考高度 = " + (altitude == null ? nullValue : altitude) + "\n")
                    .append("GPS时间戳 = " + (timestamp == null ? nullValue : timestamp) + "\n")
                    .append("GPS定位类型 = " + (processing_method == null ? nullValue : processing_method) + "\n")
                    .append("GPS参考经度 = " + (latitude_ref == null ? nullValue : latitude_ref) + "\n")
                    .append("GPS参考纬度 = " + (longitude_ref == null ? nullValue : longitude_ref) + "\n")
//                    .append("GPS经度 = " + (lat == null ? nullValue : lat) + "\n")
//                    .append("GPS经度 = " + (lon == null ? nullValue : lon) +"\n")
            ;

            /**
             * 将wgs坐标转换成百度坐标
             * 就可以用这个坐标通过百度SDK 去获取该经纬度的地址描述
             */
            double[] wgs2bd = GpsUtil.wgs2bd(lat, lon);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();

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
