package com.photor.home.dofcalculator.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.theme.ThemeHelper;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.photor.R;
import com.photor.home.dofcalculator.adapter.SensorSizeAdapter;
import com.photor.home.dofcalculator.model.SensorSizeEnum;

import java.util.List;

public class SensorSizeActivity extends AppCompatActivity {

    //控件获取
    private RecyclerView mSensorSizeRecyclerView;

    private SensorSizeAdapter mSensorSizeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dof_sensor_size);

        Toolbar toolbar = findViewById(R.id.sensor_size_activity_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle(R.string.dof_calc_name);
        toolbar.setNavigationIcon(ThemeHelper.getToolbarIcon(getApplicationContext(), CommunityMaterial.Icon.cmd_arrow_left));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // 获取RecyclerView控件
        mSensorSizeRecyclerView = findViewById(R.id.sensor_size_recycler_view);
        // 获取sensor size的list
//        TypedArray sensorSizeTypedArray = getResources().obtainTypedArray(R.array.sensers_array);
//        List<SensorSize> sensorSizeList = UtilManager.typedAyyay2SensorSizeList(sensorSizeTypedArray);
//        sensorSizeList.add(0, new SensorSize(ApplicationConstants.CUSTOM_CIRCLE_OF_CONFUSION_LABEL_IN_RECTCLER_VIEW));

        List<SensorSizeEnum> sensorSizeList = SensorSizeEnum.getCircleOfConfusionEnumList();

        // 设定layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mSensorSizeRecyclerView.setLayoutManager(linearLayoutManager);

        // 设定Adapter
        mSensorSizeAdapter = new SensorSizeAdapter(sensorSizeList);
        mSensorSizeRecyclerView.setAdapter(mSensorSizeAdapter);

    }



}
