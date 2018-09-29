package com.photor.camera.view;


import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.photor.R;

public class MessageView extends LinearLayout {

    private TextView message;
    private TextView title;

    public MessageView(Context context) {
        this(context, null);
    }

    public MessageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTitle(String title) {
//        this.title.setText(title);
    }

    public void setMessage(String message) {
//        this.message.setText(message);
    }
}
