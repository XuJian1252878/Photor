package com.photor.home.dofcalculator.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.photor.R;
import com.photor.home.dofcalculator.constant.ApplicationConstants;
import com.photor.home.dofcalculator.model.DepthOfFieldCalculator;
import com.photor.home.dofcalculator.model.SensorSizeEnum;
import com.photor.home.dofcalculator.util.UtilManager;
import com.photor.home.dofcalculator.view.DepthOfFieldView;
import com.photor.home.dofcalculator.view.Wheel;

public class DofCalcMainActivity extends AppCompatActivity {

    private DofCalcMainActivity dofCalcMainActivity;

    // 景深计算器实例
    private DepthOfFieldCalculator mDepthOfFieldCalculator;

    // 控件信息
//    private Spinner mSensorSizeSpinner;
    private TextView mDistanceTextView;
    private TextView mApertureTextView;
    private TextView mFocalTextView;
    private DepthOfFieldView mDepthOfFieldView;
    private Wheel mFocalWheel;
    private Wheel mApertureWheel;
    private Wheel mDistanceWheel;
    private TextView mHyperfocalDistanceTextView;
    private TextView mNearDepthOfFieldTextView;
    private TextView mDepthOfFieldTextView;
    private TextView mFarDepthOfFieldTextView;
    private TextView mSensorSizeTextView;
    private Button mSensorSizeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dof_main);

        // 设置ActionBar
        Toolbar toolbar = findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(toolbar);

        dofCalcMainActivity = this;

        // 初始化景深计算模块实例
        mDepthOfFieldCalculator = DepthOfFieldCalculator.getInstance(this);

        // 初始化控件信息
        mFocalTextView = findViewById(R.id.focal_text_view_number);
        mApertureTextView = findViewById(R.id.aperture_text_view_number);
        mDistanceTextView = findViewById(R.id.distance_text_view_number);
        mDepthOfFieldView = findViewById(R.id.depth_view);
        mFocalWheel = findViewById(R.id.focal_wheel);
        mApertureWheel = findViewById(R.id.aperture_wheel);
        mDistanceWheel = findViewById(R.id.distance_wheel);
        mDepthOfFieldTextView = findViewById(R.id.depth_of_field_text_view);
        mNearDepthOfFieldTextView = findViewById(R.id.near_depth_of_field_text_view);
        mFarDepthOfFieldTextView = findViewById(R.id.far_depth_of_field_text_view);
        mHyperfocalDistanceTextView = findViewById(R.id.hyperfocal_distance_text_view);
        mSensorSizeTextView = findViewById(R.id.sensor_size_text_view);
        mSensorSizeButton = findViewById(R.id.sensor_size_button);


        // 获取sensor size的list
//        TypedArray sensorSizeTypedArray = getResources().obtainTypedArray(R.array.sensers_array);
//        List<SensorSize> sensorSizeList = UtilManager.typedAyyay2SensorSizeList(sensorSizeTypedArray);

        // 设置初始的弥散圆直径 传感器名称
        mSensorSizeTextView.setText(SensorSizeEnum.getSensorSizeName(mDepthOfFieldCalculator.getCircleOfConfusionIndex()));
//        mSensorSizeTextView.setText(sensorSizeList.get(mDepthOfFieldCalculator.getCircleOfConfusionIndex()).getSensorSizeName());

//        // 传感器大小选择spinner
//        mSensorSizeSpinner = findViewById(R.id.sensor_size_spinner);
//        // 建立Spinner 的 Adapter并且绑定数据源
//        ArrayAdapter<CharSequence> sensorSizeAdapter = ArrayAdapter.createFromResource(this,
//                R.array.sensers_array, android.R.layout.simple_spinner_item);
//        sensorSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        // 绑定Adapter到控件
//        mSensorSizeSpinner.setAdapter(sensorSizeAdapter);
//        // 设置默认被选中的传感器
//        mSensorSizeSpinner.setSelection(mDepthOfFieldCalculator.getCircleOfConfusionIndex());
//        mSensorSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                onCameraParameterChanged();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                onCameraParameterChanged();
//            }
//        });


        // 为被摄物体距离的Wheel设置adapter
        // values array中设置的array信息 -> MainActivity中读入 -> 初始化 depthOfFiledCalculator
        mDistanceWheel.setAdapter(new Wheel.WheelAdapter() {
            @Override
            public int getCount() {
                return mDepthOfFieldCalculator.getDistanceCount();
            }

            @Override
            public String getItemText(int position) {
                return UtilManager.getInstance().getCompatDistanceText(dofCalcMainActivity,
                        mDepthOfFieldCalculator.getDistanceAtIndex(position));
            }
        });
        mDistanceWheel.setOnScrollListener(new Wheel.OnScrollListener() {
            @Override
            public void onWheelScroll(int position, float offset) {
                // position 当前处于第几小节 position 当前小节的下标
                // offset 当前的停止位置在当前的Wheel小节中所占的百分比 数值在[0.0, 1.0]
                mDepthOfFieldCalculator.setDistancePosition(position, offset);
                onCameraParameterChanged();
            }
        });

        // 设置光圈的Wheel信息
        mApertureWheel.setAdapter(new Wheel.WheelAdapter() {
            @Override
            public int getCount() {
                return mDepthOfFieldCalculator.getApertureCount();
            }

            @Override
            public String getItemText(int position) {
                return UtilManager.getInstance().getApertureText(dofCalcMainActivity,
                        mDepthOfFieldCalculator.getApertureAtIndex(position));
            }
        });
        mApertureWheel.setOnScrollListener(new Wheel.OnScrollListener() {
            @Override
            public void onWheelScroll(int position, float offset) {
                mDepthOfFieldCalculator.setAperturePosition(position, offset);
                onCameraParameterChanged();
            }
        });

        // 设置焦距的Wheel信息
        mFocalWheel.setAdapter(new Wheel.WheelAdapter() {
            @Override
            public int getCount() {
                return mDepthOfFieldCalculator.getFocalCount();
            }

            @Override
            public String getItemText(int position) {
                return UtilManager.getInstance().getFocalText(dofCalcMainActivity,
                        mDepthOfFieldCalculator.getFocalAtIndex(position));
            }
        });
        mFocalWheel.setOnScrollListener(new Wheel.OnScrollListener() {
            @Override
            public void onWheelScroll(int position, float offset) {
                mDepthOfFieldCalculator.setFocalPosition(position, offset);
                onCameraParameterChanged();
            }
        });

        mSensorSizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(dofCalcMainActivity, SensorSizeActivity.class);
                startActivity(intent);
            }
        });

        // 首先按照初始数据绘制DepthView
        onCameraParameterChanged();
    }


    @Override
    protected void onStart() {
        super.onStart();
        // 查看有没有其他activity传递过来的信息
        Intent intent = getIntent();
        if (intent != null) {
            String sensorSizeText = intent.getStringExtra(ApplicationConstants.SENSOR_SIZE_ITEM_NAME_TAG);
            if (sensorSizeText != null) {
                mSensorSizeTextView.setText(sensorSizeText);
                // 参数变化，重绘主界面显示
                onCameraParameterChanged();
            }
            String customCircleOfConfusion = intent.getStringExtra(ApplicationConstants.CUSTOM_CIRCLE_OF_CONFUSION);
            if (customCircleOfConfusion != null) {
                customCircleOfConfusion += "mm";
                mSensorSizeTextView.setText(customCircleOfConfusion);
                // 参数变化，重绘主界面显示
                onCameraParameterChanged();
            }
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    public void onCameraParameterChanged() {

        // 设置当前三个Wheel上方的TextView的实时数字
        mDistanceTextView.setText(UtilManager.getInstance().getCompatDistanceText(this,
                mDepthOfFieldCalculator.getCurDistance()));
        mApertureTextView.setText(UtilManager.getInstance().getApertureText(this,
                mDepthOfFieldCalculator.getCurAperture()));
        mFocalTextView.setText(UtilManager.getInstance().getFocalText(this,
                mDepthOfFieldCalculator.getCurFocal()));

        // 设置前景深、景深、超焦距、后景深的信息
        mHyperfocalDistanceTextView.setText(UtilManager.getInstance().getHyperfocalText(this,
                mDepthOfFieldCalculator.getHyperfocalDistance()));
        mNearDepthOfFieldTextView.setText(UtilManager.getInstance().getNearDepthOfFieldText(this,
                mDepthOfFieldCalculator.getNearDepthOfField()));
        mFarDepthOfFieldTextView.setText(UtilManager.getInstance().getFarDepthOfFieldText(this,
                mDepthOfFieldCalculator.getFarDepthOfField()));
        mDepthOfFieldTextView.setText(UtilManager.getInstance().getDepthOfFieldText(this,
                mDepthOfFieldCalculator.getFarDepthOfField() - mDepthOfFieldCalculator.getNearDepthOfField()));

        // 由于摄影参数的变化，DepthView需要按照这些参数重绘
        mDepthOfFieldView.setData(mDepthOfFieldCalculator.getFarDepthOfField()
                - mDepthOfFieldCalculator.getNearDepthOfField(), mDepthOfFieldCalculator.getCurDistance(),
                mDepthOfFieldCalculator.getNearDepthOfField(),
                mDepthOfFieldCalculator.getFarDepthOfField());
    }
}
