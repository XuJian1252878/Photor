package com.photor.dofcalculator.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.photor.R;
import com.photor.dofcalculator.constant.ApplicationConstants;
import com.photor.dofcalculator.model.DepthOfFieldCalculator;

public class CustomCircleOfConfusionActivity extends AppCompatActivity {

    private CustomCircleOfConfusionActivity customCircleOfConfusionActivity;
    private EditText mCustomCircleOfConfusionEditView;
    private Button mCustomCircleOfConfusionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dof_custom_circle_of_confusion);

        customCircleOfConfusionActivity = this;

        mCustomCircleOfConfusionEditView = findViewById(R.id.custom_circle_of_confusion_edit_view);
        mCustomCircleOfConfusionButton = findViewById(R.id.custom_circle_of_confusion_button);
        mCustomCircleOfConfusionButton.setClickable(false);


        mCustomCircleOfConfusionEditView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if ("".equals(mCustomCircleOfConfusionEditView.getText().toString())) {
                    mCustomCircleOfConfusionButton.setClickable(false);
                } else {
                    mCustomCircleOfConfusionButton.setClickable(true);
                }
                return false;
            }
        });

        mCustomCircleOfConfusionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String circleOfConfusionStr = mCustomCircleOfConfusionEditView.getText().toString();
                if ("".equals(circleOfConfusionStr)) {
                    return;
                }
                Intent intent = new Intent(customCircleOfConfusionActivity, DofCalcMainActivity.class);
                intent.putExtra(ApplicationConstants.CUSTOM_CIRCLE_OF_CONFUSION, circleOfConfusionStr);
                // 表示自定义的容许弥散圆半径
                DepthOfFieldCalculator.getInstance(customCircleOfConfusionActivity).setCustomCircleOfConfusion(Double.valueOf(circleOfConfusionStr));
                startActivity(intent);

                finish(); // 销毁该activity
            }
        });

        // 对CustomCircleOfConfusionEditView的输入进行检查
        InputFilter customCircleOfConfusionInputFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                                       int dstart, int dend) {
                if ("".equals(source.toString())) {
                    // 要是仅仅删除一个字符的话，source 将会是""这个字符。
                    return "";
                }
                String dValue = dest.toString();

                int dotCount = 0;
                int charSequenceIndex = 0;
                while((charSequenceIndex = dValue.indexOf(".", charSequenceIndex)) != -1) {
                    dotCount ++;
                    charSequenceIndex += 1;
                }
                if ( (dotCount == 1 && ".".equals(source.toString())) // 多个小数点的情况
                       || (".".equals(source.toString()) && dValue.length() <= 0) ) {
                    // 那么说明包含多个小数点，输入不合法
                    Toast.makeText(customCircleOfConfusionActivity, "输入不合法", Toast.LENGTH_SHORT).show();
                    return "";
                    // 当输入的第一个字符是小数点的时候，如果return null。那么会直接略过return null，而执行下面的 return source.toString();不知道是什么操作
                    // 看来对于EditView来说，默认返回 "" 还是靠谱很多的。毕竟他原生的EditView控件就是默认返回 ""的。
                }

                return source.toString();
            }
        };

        mCustomCircleOfConfusionEditView.setFilters(new InputFilter[]{customCircleOfConfusionInputFilter});

    }
}
