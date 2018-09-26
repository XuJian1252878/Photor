package com.photor.dofcalculator.model;


import com.photor.R;

/**
 * Created by xujian on 2017/12/30.
 */

public enum DistanceInfoEnum {

    METERS(0, R.string.meter, 1.0f), CENTIMETERS(1, R.string.centimeter, 0.01f),
    FEET(2, R.string.feet, 0.3048f), INCHES(3, R.string.inch, 0.0254f);

    // 成员变量
    private int index;
    private int name;
    private double unitLength; // 转换成以米作为标准单位

    // 构造函数
    private DistanceInfoEnum(int index, int name, double unitLength) {
        this.index = index;
        this.name = name;
        this.unitLength = unitLength;
    }

    // 根据index获取对应名称
    public static int getName(int index) {
        // .values() 获得每一个enum对象
        for (DistanceInfoEnum die : DistanceInfoEnum.values()) {
            if (die.getIndex() == index) {
                return die.getName();
            }
        }
        return -1;
    }

    // 获得unitLength信息
    public static double getUnitLength(int index) {
        for (DistanceInfoEnum die : DistanceInfoEnum.values()) {
            if (die.getIndex() == index) {
                return die.getUnitLength();
            }
        }
        return  0.f;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    public double getUnitLength() {
        return unitLength;
    }

    public void setUnitLength(double unitLength) {
        this.unitLength = unitLength;
    }
}
