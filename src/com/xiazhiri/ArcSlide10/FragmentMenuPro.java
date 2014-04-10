package com.xiazhiri.ArcSlide10;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

public class FragmentMenuPro extends Fragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    ActivityMain mActivityMain;
    FrameLayout mFrameLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mActivityMain = (ActivityMain)getActivity();
        mFrameLayout = (FrameLayout)mActivityMain.findViewById(R.id.fragment_content);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contextView = inflater.inflate(R.layout.menu_pro, container, false);
/*        LinearLayout layout = (LinearLayout) contextView.findViewById(R.id.listMenu_layout);
        for (int i = 0; i < layout.getChildCount(); i++)
            layout.getChildAt(i).setOnClickListener(this);*/
        //计算路径
        ((Button)contextView.findViewById(R.id.btnCalcRoute)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivityMain.routingTask.FindRoute();
            }
        });
        return contextView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.cbLoaction:
                if (((CheckBox)mActivityMain.findViewById(R.id.cbLoaction)).isChecked())
                    mActivityMain.ldm.start();
                else
                    mActivityMain.ldm.stop();
                break;
            /*
            case R.id.cbIsMeasure:
                if (((CheckBox)mActivityMain.findViewById(R.id.cbIsMeasure)).isChecked()) {
                    mActivityMain.touchListener.touchMode = "Measure";
                    ((RadioGroup) contextView.findViewById(R.id.rgMeasure)).setClickable(true);
                }
                else {
                    mActivityMain.touchListener.touchMode = "Normal";
                    ((RadioGroup) contextView.findViewById(R.id.rgMeasure)).clearCheck();
                    ((RadioGroup) contextView.findViewById(R.id.rgMeasure)).setClickable(false);
                }
                break;
                */
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i){
            case R.id.rbLongth:
                mActivityMain.measure = new Measure(mActivityMain,mActivityMain.mGraphicsLayer,1);
                break;
            case R.id.rbPolygon:
                mActivityMain.measure = new Measure(mActivityMain,mActivityMain.mGraphicsLayer,2);
            default:
                break;
        }
    }
}