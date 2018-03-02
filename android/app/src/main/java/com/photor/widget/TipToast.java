package com.photor.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.TextView;

import com.photor.R;

/**
 * Created by xujian on 2018/3/2.
 */

public class TipToast extends Dialog {

    private ImageView toastTipImageView;
    private TextView toastTipTextView;
    private String message;
    private Context context;

    public TipToast(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置tip dialog布局
        setContentView(R.layout.toast_tip);
        toastTipImageView = findViewById(R.id.toast_tip_iv);
        toastTipTextView = findViewById(R.id.toast_tip_tv);
    }

    @Override
    public void show() {
        super.show();
        // 设置 dialog 布局中的内容
        toastTipTextView.setText(message);
    }

    public static class Builder {
        private TipToast tipToast;

        public Builder(Context context) {
            this.tipToast = new TipToast(context);
        }

        public Builder setMessage(String message) {
            this.tipToast.message = message;
            return this;
        }

        public TipToast create() {
            return tipToast;
        }
    }
}
