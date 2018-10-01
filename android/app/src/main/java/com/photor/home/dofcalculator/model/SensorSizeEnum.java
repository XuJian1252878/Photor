package com.photor.home.dofcalculator.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by xujian on 2017/12/30.
 */

public enum SensorSizeEnum {

    Nikon_1_series(0, "Nikon 1 series", 0.011f),
    M43(1, "M43", 0.015f),
    APS_C_Canon(2, "APS-C Canon", 0.018f),
    APS_C_Nikon_Pentax_Sony(3, "APS-C Nikon/Pentax/Sony", 0.018f),
    APS_H_Canon(4, "APS-H Canon", 0.019f),
    Full_Frame_35mm(5, "Full Frame 35mm", 0.023f),
    Medium_Frame_6x4_5(6, "Medium Frame 6x4.5", 0.029f),
    Medium_Frame_6x6(7, "Medium Frame 6x6", 0.047f),
    Medium_Frame_6x6_2(8, "Medium Frame 6x6", 0.053f),
    Medium_Frame_6x7(9, "Medium Frame 6x7", 0.059f),
    Medium_Frame_6x9(10, "Medium Frame 6x9", 0.067f),
    Medium_Frame_6x12(11, "Medium Frame 6x12", 0.083f),
    Medium_Frame_6x17(12, "Medium Frame 6x17", 0.12f),
    Large_Frame_4x5(13, "Large Frame 4x5", 0.11f),
    Large_Frame_5x7(14, "Large Frame 5x7", 0.15f),
    Large_Frame_8x10(15, "Large Frame 8x10", 0.22f);

    // 成员变量
    private int index;
    private String name;
    private double circleOfConfusion;

    // 构造函数
    private SensorSizeEnum(int index, String name, double circleOfConfusion) {
        this.index = index;
        this.name = name;
        this.circleOfConfusion = circleOfConfusion;
    }

    public static double getCircleOfConfusion(int index) {
        for (SensorSizeEnum sse : SensorSizeEnum.values()) {
            if (sse.getIndex() == index) {
                return sse.getCircleOfConfusion();
            }
        }
        return 0.f;
    }

    public static String getSensorSizeName(int index) {
        for (SensorSizeEnum sse : SensorSizeEnum.values()) {
            if (sse.getIndex() == index) {
                return sse.getName();
            }
        }
        return null;
    }

    public static List<SensorSizeEnum> getCircleOfConfusionEnumList () {
        List<SensorSizeEnum> sensorSizeEnums = new ArrayList<>();
        sensorSizeEnums.addAll(Arrays.asList(SensorSizeEnum.values()));
        return sensorSizeEnums;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCircleOfConfusion() {
        return circleOfConfusion;
    }

    public void setCircleOfConfusion(double circleOfConfusion) {
        this.circleOfConfusion = circleOfConfusion;
    }
}
