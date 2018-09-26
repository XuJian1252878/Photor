package com.photor.dofcalculator.model;

import android.content.Context;
import android.content.res.TypedArray;

import com.photor.R;

import static com.photor.dofcalculator.util.UtilManager.typedArray2DoubleList;

/**
 * Created by xujian on 2017/12/28.
 */

public class DepthOfFieldCalculator {

    private Double[] mDistanceList; // 像物距离列表
    private Double[] mApertureList; // 光圈大小列表
    private Double[] mFocalList; // 焦距大小列表

    private double mCurDistance; // 当前像物距离
    private double mCurAperture; // 当前光圈
    private double mCurFocal; // 当前焦距
    private double mCurCustomCircleOfConfusion = 0.22f; // 当前自定义的容许弥散圆直径

    private int mDistanceUnitIndex; // 当前距离计量单位下标
    private int mCircleOfConfusionIndex; // 当前的容许弥散圆直径下标

    private static DepthOfFieldCalculator mDepthOfFieldCalculator = null;

    public static DepthOfFieldCalculator getInstance(Context context) {
        if (mDepthOfFieldCalculator == null) {
            // 读取设置在values arrays.xml中的数组
            /**
             * The Android resource system keeps track of all non-code assets associated with an application.
             * You can use this class to access your application's resources. You can generally acquire
             * the Resources instance associated with your application with getResources().
             */
            TypedArray distanceArray = context.getResources().obtainTypedArray(R.array.distance_list);
            Double[] distanceList = typedArray2DoubleList(distanceArray);
            distanceArray.recycle();

            TypedArray apertureArray = context.getResources().obtainTypedArray(R.array.aperture_list);
            Double[] apertureList = typedArray2DoubleList(apertureArray);
            apertureArray.recycle();

            TypedArray focalArray = context.getResources().obtainTypedArray(R.array.focal_list);
            Double[] focalList = typedArray2DoubleList(focalArray);
            focalArray.recycle();

            // 初始化景深计算模块实例，分别以三个列表的对应项作为初始值
            mDepthOfFieldCalculator = new DepthOfFieldCalculator(distanceList, apertureList, focalList);
        }

        return mDepthOfFieldCalculator;
    }

    public DepthOfFieldCalculator(Double[] distanceList, Double[] apertureList, Double[] focalList) {
        mDistanceList = distanceList;
        mApertureList = apertureList;
        mFocalList = focalList;

        // 当前像物距离、光圈、焦距初始为各自列表中的第一个值
        mCurDistance = mDistanceList[0];
        mCurAperture = mApertureList[0];
        mCurFocal = mFocalList[0];

        mDistanceUnitIndex = 0; // 默认以m作为距离的单位
        mCircleOfConfusionIndex = 5; //默认的容许弥散圆直径为0.023mm ，列表第5个下标对应的数值
    }

    // 焦距相关操作
    public double getCurFocal() {
        return mCurFocal;
    }
    public double getFocalAtIndex(int position) {
        return mFocalList[position];
    }
    public int getFocalCount() {
        return mFocalList.length;
    }

    // 光圈相关操作
    public double getCurAperture() {
        return mCurAperture;
    }
    public double getApertureAtIndex(int position) {
        return mApertureList[position];
    }
    public int getApertureCount() {
        return mApertureList.length;
    }

    // 像物距离相关操作
    public int getDistanceCount() {
        return mDistanceList.length;
    }
    public double getDistanceAtIndex(int position) {
        return mDistanceList[position];
    }
    // 获得当前像物距离的 衡量单位，默认是m
    public int getDistanceUnitIndex() {
        return mDistanceUnitIndex;
    }
    // 设置当前像物距离的衡量单位
    public void setDistanceUnitIndex(int value) {
        mDistanceUnitIndex = value;
    }
    public double getCurDistance() {
        return mCurDistance;
    }


    // 获得滑动条两个区域之间 某一个确定位置的对应值
    public static double getSmoothedValue(double lower, double upper, double lowerDerivative,
                                          double upperDerivative, double offset) {
        double a = lower;
        double b = lowerDerivative;
        double c = upper * 3 - upperDerivative - lower * 3l - lowerDerivative * 2;
        double d = (-upper * 2) + upperDerivative + (lower * 2) + lowerDerivative;

        return (a + b * offset + c * offset * offset + d * offset * offset * offset);
    }

    // 设置滑动的像物距离的具体位置
    public void setDistancePosition(int position, double offset) {
        if (position >= mDistanceList.length - 1) {
            mCurDistance = mDistanceList[mDistanceList.length - 1];
            return;
        }

        double lower = mDistanceList[position];
        double upper = mDistanceList[position + 1];
        double lowerDerivative = position <= 0 ? 0.0f : lower - mDistanceList[position - 1];
        double upperDerivative = upper - lower;

        mCurDistance = getSmoothedValue(lower, upper, lowerDerivative, upperDerivative, offset);
    }

    // 设置滑动的光圈大小的具体位置
    public void setAperturePosition(int position, double offset) {
        if (position >= mApertureList.length - 1) {
            mCurAperture = mApertureList[mApertureList.length - 1];
            return;
        }

        double lower = mApertureList[position];
        double upper = mApertureList[position + 1];
        double lowerDerivative = position <= 0 ? 0.0f : lower - mApertureList[position - 1];
        double upperDerivative = upper - lower;

        mCurAperture = getSmoothedValue(lower, upper, lowerDerivative, upperDerivative, offset);
    }

    // 设置滑动的光圈的大小
    public void setFocalPosition(int position, double offset) {
        if (position >= mFocalList.length - 1) {
            mCurFocal = mFocalList[mFocalList.length - 1];
            return;
        }

        double lower = mFocalList[position];
        double upper = mFocalList[position + 1];
        double lowerDerivative = position <= 0 ? 0.0f : lower - mFocalList[position - 1];
        double upperDerivative = upper - lower;

        mCurFocal = getSmoothedValue(lower, upper, lowerDerivative, upperDerivative, offset);
    }


    // 关于容许弥散圆的设置
    public double getCircleOfConfusion() {
        if (mCircleOfConfusionIndex < 0) {
            // 说明当前是自定义的容许弥散圆直径，那么直接返回自定义的容许弥散圆直径
            return mCurCustomCircleOfConfusion;
        }
        return SensorSizeEnum.getCircleOfConfusion(mCircleOfConfusionIndex);
    }
    public int getCircleOfConfusionIndex() {
        return mCircleOfConfusionIndex;
    }
    public void setCircleOfConfusionIndex(int index) {
        mCircleOfConfusionIndex = index;
    }
    // 设置自定义的容许弥散圆直径
    public void setCustomCircleOfConfusion(double customCircleOfConfusion) {
        mCurCustomCircleOfConfusion = customCircleOfConfusion;
        mCircleOfConfusionIndex = -1; // 自定义容许弥散圆直径的标识。
    }


    // 关于前景深的计算
    public double getNearDepthOfField() {
        double N = getCurAperture(); // 光圈
        double f = getCurFocal() / 1000.0f; // 焦距 mm -> m
        double s = getCurDistance(); // 物距
        double c = getCircleOfConfusion() / 1000.0f; // 容许弥散圆直径 mm -> m

        double denominator = (f * f - c * N * f + c * N * s);
        if (denominator <= 0.0f) {
            return Double.POSITIVE_INFINITY;
        }

        return (s * f * f) / denominator;
    }

    // 关于后景深的计算
    public double getFarDepthOfField() {
        double N = getCurAperture(); // 光圈
        double f = getCurFocal() / 1000.0f; // 焦距 mm -> m
        double s = getCurDistance(); // 物距
        double c = getCircleOfConfusion() / 1000.0f; // 容许弥散圆直径 mm -> m

        double denominator = (f * f + c * N * f - c * N * s);
        if (denominator <= 0.0f) {
            return Double.POSITIVE_INFINITY;
        }

        return (s * f * f) / denominator;
    }

    // 计算超焦距
    public double getHyperfocalDistance() {
        double N = getCurAperture(); // 光圈
        double f = getCurFocal() / 1000.0f; // 焦距 mm -> m
        double c = getCircleOfConfusion() / 1000.0f; // 容许弥散圆直径 mm -> m

        double denominator = (N * c);
        if (denominator <= 0.0f) {
            return Double.POSITIVE_INFINITY;
        }

        return f + f * f / denominator;
    }

}
