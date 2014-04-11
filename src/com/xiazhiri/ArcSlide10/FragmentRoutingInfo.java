package com.xiazhiri.ArcSlide10;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class FragmentRoutingInfo extends Fragment {

    ActivityMain activityMain;
    String startPointName;
    //FrameLayout frameLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        activityMain = (ActivityMain)getActivity();
        //frameLayout = (FrameLayout)activityMain.findViewById(R.id.fragment_content);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contextView = inflater.inflate(R.layout.fragment_routing_info, container, false);
        ((Button)contextView.findViewById(R.id.RoutingInfoFindRoute)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityMain.routingTask.FindRoute();
            }
        });
        ((TextView)contextView.findViewById(R.id.RoutingInfoStartPoint)).setText(startPointName);
        return contextView;
    }

}
