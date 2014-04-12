package com.xiazhiri.ArcSlide10;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

public class FragmentMenuNormal extends Fragment implements View.OnClickListener, CheckBox.OnCheckedChangeListener {

    ActivityMain mActivityMain;
    FrameLayout mFrameLayout;
    View contexView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mActivityMain = (ActivityMain)getActivity();
        mFrameLayout = (FrameLayout)mActivityMain.findViewById(R.id.fragment_content);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contextView = inflater.inflate(R.layout.menu_normal, container, false);
        TableLayout layout = (TableLayout) contextView.findViewById(R.id.menu_normal_list);
        for (int i = 0; i < layout.getChildCount(); i++)
            try {
                layout.getChildAt(i).setOnClickListener(this);
                ((CheckBox)layout.getChildAt(i)).setOnCheckedChangeListener(this);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        this.contexView = contextView;
        return contextView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            default:
                break;

            case R.id.cbLoaction:
                if (((CheckBox)mActivityMain.findViewById(R.id.cbLoaction)).isChecked())
                    mActivityMain.ldm.start();
                else
                    mActivityMain.ldm.stop();
                break;
            case R.id.longthMeasure:
                if (((CheckBox)mActivityMain.findViewById(R.id.longthMeasure)).isChecked())
                    mActivityMain.measure = new Measure(mActivityMain,mActivityMain.mGraphicsLayer,1);
                break;
            case R.id.areaMeasure:
                if (((CheckBox)mActivityMain.findViewById(R.id.areaMeasure)).isChecked())
                    mActivityMain.measure = new Measure(mActivityMain,mActivityMain.mGraphicsLayer,2);
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()){
            case R.id.cbLoaction:
                if (b)
                    mActivityMain.ldm.start();
                else
                    mActivityMain.ldm.stop();
                break;
            case R.id.longthMeasure:
                if (b) {
                    mActivityMain.measure = new Measure(mActivityMain, mActivityMain.mGraphicsLayer, 1);
                    ((CheckBox)contexView.findViewById(R.id.areaMeasure)).setChecked(false);
                }
                if (((CheckBox)contexView.findViewById(R.id.longthMeasure)).isChecked() | ((CheckBox)contexView.findViewById(R.id.areaMeasure)).isChecked())
                    mActivityMain.touchListener.touchMode = "Measure";
                else {
                    mActivityMain.touchListener.touchMode = "Normal";
                    mActivityMain.measure.Reset();
                }
                break;
            case R.id.areaMeasure:
                if (b) {
                    mActivityMain.measure = new Measure(mActivityMain, mActivityMain.mGraphicsLayer, 2);
                    ((CheckBox)contexView.findViewById(R.id.longthMeasure)).setChecked(false);
                }
                if (((CheckBox)contexView.findViewById(R.id.longthMeasure)).isChecked() | ((CheckBox)contexView.findViewById(R.id.areaMeasure)).isChecked())
                    mActivityMain.touchListener.touchMode = "Measure";
                else {
                    mActivityMain.touchListener.touchMode = "Normal";
                    mActivityMain.measure.Reset();
                }
                break;
            default:
                break;
        }

    }
}
