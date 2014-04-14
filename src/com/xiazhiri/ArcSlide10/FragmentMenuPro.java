package com.xiazhiri.ArcSlide10;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

public class FragmentMenuPro extends Fragment implements View.OnClickListener {

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
        LinearLayout layout = (LinearLayout) contextView.findViewById(R.id.listMenu_layout);
        for (int i = 0; i < layout.getChildCount(); i++)
            layout.getChildAt(i).setOnClickListener(this);
        return contextView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.takePic:
                break;
            default:
                break;
        }
    }
}
