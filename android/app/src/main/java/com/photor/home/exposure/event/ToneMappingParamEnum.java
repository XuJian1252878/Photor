package com.photor.home.exposure.event;

import com.photor.R;

import static com.photor.home.exposure.event.ToneMappingMethodEnum.Drago;
import static com.photor.home.exposure.event.ToneMappingMethodEnum.Durand;
import static com.photor.home.exposure.event.ToneMappingMethodEnum.Mantiuk;
import static com.photor.home.exposure.event.ToneMappingMethodEnum.Reinhard;

/**
 * @author htwxujian@gmail.com
 * @date 2018/11/10 16:19
 */
public enum ToneMappingParamEnum {
    // Drago
    gamma_drago(0, R.string.gamma_drago, 1.0f, 1.0f, 0.0f, 5.0f, R.id.gamma_drago_seek_bar, Drago.getMethodId(), R.string.Drago),
    saturation_drago(1, R.string.saturation_drago, 1.0f, 1.0f, -2.0f, 3.0f, R.id.saturation_drago_bar, Drago.getMethodId(), R.string.Drago),
    bias_drago(2, R.string.bias_drago, 0.85f, 0.85f, 0.0f, 1.0f, R.id.bias_drago_bar, Drago.getMethodId(), R.string.Drago),

    // Durand
    gamma_durand(3, R.string.gamma_durand, 1.0f, 1.0f, 0.0f, 5.0f, R.id.gamma_durand_seek_bar, Durand.getMethodId(), R.string.Durand),
    saturation_durand(4, R.string.saturation_durand, 1.0f, 1.0f, -2.0f, 3.0f, R.id.saturation_durand_seek_bar, Durand.getMethodId(), R.string.Durand),
    contrast_durand(5, R.string.contrast_durand, 4.0f, 4.0f, 0.0f, 10.0f, R.id.contrast_durand_seek_bar, Durand.getMethodId(), R.string.Durand),
    sigma_space_durand(6, R.string.sigma_space_durand, 2.0f, 2.0f, 0.0f, 5.0f, R.id.sigma_space_durand_seek_bar, Durand.getMethodId(), R.string.Durand),
    sigma_color_durand(7, R.string.sigma_color_durand, 2.0f, 2.0f, 0.0f, 5.0f, R.id.sigma_color_durand_seek_bar, Durand.getMethodId(), R.string.Durand),

    // Mantiuk
    gamma_mantiuk(8, R.string.gamma_mantiuk, 1.0f, 1.0f, 0.0f, 5.0f, R.id.gamma_mantiuk_seek_bar, Mantiuk.getMethodId(), R.string.Mantiuk),
    saturation_mantiuk(9, R.string.saturation_mantiuk, 1.0f, 1.0f, -2.0f, 3.0f, R.id.saturation_mantiuk_seek_bar, Mantiuk.getMethodId(), R.string.Mantiuk),
    scale_mantiuk(10, R.string.scale_mantiuk, 0.7f, 0.7f, 0.0f, 1.0f, R.id.scale_mantiuk_seek_bar, Mantiuk.getMethodId(), R.string.Mantiuk),

    // Reinhard
    gamma_reinhard(11, R.string.gamma_reinhard, 1.0f, 1.0f, 0.0f, 5.0f, R.id.gamma_reinhard_seek_bar, Reinhard.getMethodId(), R.string.Reinhard),
    color_adapt_reinhard(12, R.string.color_adapt_reinhard, 0.0f, 0.0f, 0.0f, 1.0f, R.id.color_adapt_reinhard_seek_bar, Reinhard.getMethodId(), R.string.Reinhard),
    light_adapt_reinhard(13, R.string.light_adapt_reinhard, 1.0f, 1.0f, 0.0f, 1.0f, R.id.light_adapt_reinhard_seek_bar, Reinhard.getMethodId(), R.string.Reinhard),
    intensity_reinhard(14, R.string.intensity_reinhard, 0.0f, 0.0f, -8.0f, 8.0f, R.id.intensity_reinhard_seek_bar, Reinhard.getMethodId(), R.string.Reinhard)

    ;

    private int paramIndex;
    private int nameResId;

    private float value;  // 当前值
    private float tmpValue; // 临时值
    private float minValue;  // 最小值
    private float maxValue;  // 最大值
    private int seekBarResId;  // 当前SeekBarId信息

    private int methodId; // 色调映射函数的下标
    private int belongMethodResId;

    ToneMappingParamEnum(int paramIndex, int nameResId, float value, float tmpValue, float minValue, float maxValue, int seekBarResId, int methodId, int belongMethodResId) {
        this.paramIndex = paramIndex;
        this.nameResId = nameResId;
        this.value = value;
        this.tmpValue = tmpValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.seekBarResId = seekBarResId;
        this.methodId = methodId;
        this.belongMethodResId = belongMethodResId;
    }

    public int getParamIndex() {
        return paramIndex;
    }

    public void setParamIndex(int paramIndex) {
        this.paramIndex = paramIndex;
    }

    public int getNameResId() {
        return nameResId;
    }

    public void setNameResId(int nameResId) {
        this.nameResId = nameResId;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getTmpValue() {
        return tmpValue;
    }

    public void setTmpValue(float tmpValue) {
        this.tmpValue = tmpValue;
    }

    public float getMinValue() {
        return minValue;
    }

    public void setMinValue(float minValue) {
        this.minValue = minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }

    public int getSeekBarResId() {
        return seekBarResId;
    }

    public void setSeekBarResId(int seekBarResId) {
        this.seekBarResId = seekBarResId;
    }

    public int getMethodId() {
        return methodId;
    }

    public void setMethodId(int methodId) {
        this.methodId = methodId;
    }

    public int getBelongMethodResId() {
        return belongMethodResId;
    }

    public void setBelongMethodResId(int belongMethodResId) {
        this.belongMethodResId = belongMethodResId;
    }
}
