package com.photor.home.dofcalculator.util;

import android.content.Context;
import android.content.res.TypedArray;

import com.photor.R;
import com.photor.home.dofcalculator.model.DistanceInfoEnum;
import com.photor.home.dofcalculator.model.UnitSystemEnum;

import java.text.DecimalFormat;

/**
 * Created by xujian on 2017/12/28.
 */

public class UtilManager {

    // 单例模式，程序中只有一个UnitManager实例
    private static UtilManager instance = new UtilManager();

    // 默认是使用公尺作为计量单位的
    private static UnitSystemEnum mUnitType = UnitSystemEnum.METRIC;
    public boolean setUnitSystem(UnitSystemEnum unitSystemType) {
        mUnitType = unitSystemType;
        return true;
    }

    public static UtilManager getInstance() {
        return instance;
    }

    public float getDistanceUnitLength(DistanceInfoEnum distanceInfoEnum) {
        // 返回枚举对象的序数
//        int ordinal = distanceUnit.ordinal();
        // 返回对应计量单位数组中对应的距离单位信息
        return (float)distanceInfoEnum.getUnitLength();
    }

    public String getDistanceUnitName(Context context, DistanceInfoEnum distanceInfoEnum) {
        // 返回枚举对象的序数
//        int ordinal = distanceUnit.ordinal();
        return context.getString(distanceInfoEnum.getName());
    }

    // 显示相机以拍摄物体间的距离文字
    // 根据context和传入的数值v返回对应的 单位 字符串表示
    public String getCompatDistanceText(Context context, double v) {

        // 处理特殊情况下的距离显示文字
        if (v == Double.POSITIVE_INFINITY) {
            return context.getString(R.string.infinity);
        } else if (v == Double.NEGATIVE_INFINITY) {
            return context.getString(R.string.negative_infinity);
        } else if (v == Double.NaN) {
            return context.getString(R.string.no_a_number);
        }

        // 处理正常情况下的距离显示文字
        // DecimalFormat 类主要靠 # 和 0 两种占位符号来指定数字长度。0 表示如果位数不足则以 0 填充，# 表示只要有可能就把数字拉上这个位置。
        DecimalFormat decimalFormator = new DecimalFormat("#.##"); // 数字以 x.xx 来表示
        if (v < 0.01f) {
            // 如果距离小于1cm，那么将距离以毫米的单位显示
            return String.format("%s%s", decimalFormator.format(v * 1000.0), "mm");
        } else if (v < 0.1f) {
            // 如果距离在1cm ～ 10cm，那么将距离以cm单位显示
            return String.format("%s%s", decimalFormator.format(v * 100.0), "cm");
        } else if (v < 1000f) {
            // 当距离在10cm ~ 1000m的时候以m为单位显示
            return String.format("%s%s", decimalFormator.format(v), "m");
        } else {
            // 超过1km的时候以km为计量单位
            return String.format("%s%s", decimalFormator.format(v / 1000.0), "km");
        }
    }

    // 显示当前光圈大小的文字。
    public String getApertureText(Context context, double v) {
        DecimalFormat decimalFormator = new DecimalFormat("#.##");
        return String.format("F/%s", decimalFormator.format(v));
    }

    // 显示当前焦距大小的文字
    public String getFocalText(Context context, double v) {
        DecimalFormat decimalFormator = new DecimalFormat("#.##");
        return String.format("%s%s", decimalFormator.format(v), "mm");
    }

    // 获取超焦距大小的文字
    public String getHyperfocalText(Context context, double v) {
        return getFocalText(context, v);
    }

    // 获取前景深大小的文字
    public String getNearDepthOfFieldText(Context context, double v) {
        return getCompatDistanceText(context, v);
    }

    // 获取后景深大小的文字
    public String getFarDepthOfFieldText(Context context, double v) {
        return getCompatDistanceText(context, v);
    }

    public String getDepthOfFieldText(Context context, double v) {
        return getCompatDistanceText(context, v);
    }

    // 获取设置在values文件中的array信息（转化为double数字）
    public static Double[] typedArray2DoubleList(TypedArray array) {
        Double[] list = new Double[array.length()];
        for (int i = 0; i < list.length; i ++) {
            list[i] = (double)array.getFloat(i, 0.0f);
        }
        return list;
    }

}
