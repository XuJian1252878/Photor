package com.photor.home.exposure.event;

import com.photor.R;
import com.photor.util.ActivitySwitchHelper;

/**
 * @author htwxujian@gmail.com
 * @date 2018/11/10 12:38
 */
public enum ToneMappingMethodEnum {
    Drago(0, R.string.Drago, R.id.drago_params_panel),
    Durand(1, R.string.Durand, R.id.durand_params_panel),
    Mantiuk(2, R.string.Mantiuk, R.id.mantiuk_params_panel),
    Reinhard(3, R.string.Reinhard, R.id.reinhard_params_panel);

    private int methodId;
    private int methodNameResId;
    private int toneMappingPanelResId;

    ToneMappingMethodEnum(int methodId, int methodNameResId, int toneMappingPanelResId) {
        this.methodId = methodId;
        this.methodNameResId = methodNameResId;
        this.toneMappingPanelResId = toneMappingPanelResId;
    }

    public int getMethodId() {
        return methodId;
    }

    public void setMethodId(int methodId) {
        this.methodId = methodId;
    }

    public int getMethodNameResId() {
        return methodNameResId;
    }

    public void setMethodNameResId(int methodNameResId) {
        this.methodNameResId = methodNameResId;
    }

    public int getToneMappingPanelResId() {
        return toneMappingPanelResId;
    }

    public void setToneMappingPanelResId(int toneMappingPanelResId) {
        this.toneMappingPanelResId = toneMappingPanelResId;
    }
}
