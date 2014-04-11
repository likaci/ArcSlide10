package com.xiazhiri.ArcSlide10;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.esri.core.tasks.na.StopGraphic;

public class FragmentSearchInfo extends Fragment implements View.OnClickListener {

    ActivityMain activityMain;
    FrameLayout mFrameLayout;
    String infoName;
    com.esri.core.geometry.Point point;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        activityMain = (ActivityMain)getActivity();
        mFrameLayout = (FrameLayout)activityMain.findViewById(R.id.fragment_content);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contextView = inflater.inflate(R.layout.fragment_search_info, container, false);
        ((TextView)contextView.findViewById(R.id.SearchInfoName)).setText(infoName);
        ((TextView)contextView.findViewById(R.id.SearchInfoCoord)).setText((int)point.getX() + "," + (int)point.getY());
        ((Button)contextView.findViewById(R.id.SearchInfoAddToRoute)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityMain.routingTask.AddPoint(new StopGraphic(point),infoName);
            }
        });
        return contextView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.cbLoaction:
                if (((CheckBox)activityMain.findViewById(R.id.cbLoaction)).isChecked())
                    activityMain.ldm.start();
                else
                    activityMain.ldm.stop();
                break;
            default:
                break;
        }
    }

}
