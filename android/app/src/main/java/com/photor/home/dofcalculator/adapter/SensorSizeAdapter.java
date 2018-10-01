package com.photor.home.dofcalculator.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.photor.R;
import com.photor.home.dofcalculator.activity.CustomCircleOfConfusionActivity;
import com.photor.home.dofcalculator.activity.DofCalcMainActivity;
import com.photor.home.dofcalculator.activity.SensorSizeActivity;
import com.photor.home.dofcalculator.constant.ApplicationConstants;
import com.photor.home.dofcalculator.model.DepthOfFieldCalculator;
import com.photor.home.dofcalculator.model.SensorSizeEnum;

import java.util.List;

/**
 * Created by xujian on 2017/12/30.
 */

public class SensorSizeAdapter extends RecyclerView.Adapter<SensorSizeAdapter.ViewHolder> {

    private List<SensorSizeEnum> mSensorSizeList;
    private Context mContext; // 用于在View Holder里面启动activity等，是一个技巧。

    // adapter的构造函数
    public SensorSizeAdapter(List<SensorSizeEnum> sensorSizeList) {
        mSensorSizeList = sensorSizeList;
    }

    // 每一个子项都是一个view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mSensorSizeItemTextView;
        public ViewHolder(View itemView) {
            super(itemView);
            mSensorSizeItemTextView = itemView.findViewById(R.id.sensor_size_item_text_view);
        }
    }

    // 指定每一个view holder的布局，返回设定响应事件以及布局之后的view holder
    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View sensorSizeItemView = LayoutInflater.from(mContext).inflate(R.layout.dof_sensor_size_item, parent, false);

        final ViewHolder holder = new ViewHolder(sensorSizeItemView);
        holder.mSensorSizeItemTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取当前被点击的是哪一项
                int position = holder.getAdapterPosition();
                if (position <= 0) {
                    // 如果点击的是第一项，那么用户自定义允许弥散圆的大小
                    Intent intent = new Intent(mContext, CustomCircleOfConfusionActivity.class);
                    mContext.startActivity(intent);
                } else {
//                    SensorSizeEnum sensorSize = mSensorSizeList.get(position);
                    // 返回MainActivity，使用更新之后的传感器更新主页面视图
                    Intent intent = new Intent(mContext, DofCalcMainActivity.class);
                    intent.putExtra(ApplicationConstants.SENSOR_SIZE_ITEM_NAME_TAG, SensorSizeEnum.getSensorSizeName(position - 1 ));
                    // 记录当前的弥散圆直径
                    // position - 1 因为第一项添加了一个手动输入弥散圆直径的选项，而DepthOfFieldCalculator记录的是没有第一项的所有数据，所以position - 1
                    DepthOfFieldCalculator.getInstance(mContext).setCircleOfConfusionIndex(position - 1);
                    mContext.startActivity(intent);
                }

                // 点击之后关闭当前的 SensorSizeActivity
                if (mContext instanceof SensorSizeActivity) {
                    ((SensorSizeActivity) mContext).finish();
                }
            }
        });

        return holder;
    }

    // 加载了 RecyclerView的每一个item的布局之后（以view holder的形式展现），对item（view holder）
    // 中的子控件进行获取操作之后，对子控件内容进行填充
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (position <= 0) {
            holder.mSensorSizeItemTextView.setText(mContext.getResources().
                    getString(R.string.input_custom_circle_of_confusion_recycler_view_item));
        } else {
            holder.mSensorSizeItemTextView.setText(SensorSizeEnum.getSensorSizeName(position - 1));
        }
    }

    @Override
    public int getItemCount() {
        return mSensorSizeList.size() + 1;
    }

}
