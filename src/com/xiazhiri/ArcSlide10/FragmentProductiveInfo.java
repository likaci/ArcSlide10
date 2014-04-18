package com.xiazhiri.ArcSlide10;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.esri.core.map.Feature;

/**
 * Created by Administrator on 2014/4/17.
 */
public class FragmentProductiveInfo extends Fragment {

    Feature feature;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View contextView = inflater.inflate(R.layout.fragment_productive_info, container, false);
        return contextView;
    }

    @Override
    public void onResume() {
        super.onResume();
        TableLayout tableLayout =  (TableLayout)getView().findViewById(R.id.tableProductiveLayout);
        for (String key : feature.getAttributes().keySet()) {
            TableRow tableRow = new TableRow(getView().getContext());
            TextView keyTxt = new TextView(getView().getContext());
            keyTxt.setText(key);
            TextView valueTxt= new TextView(getView().getContext());
            try {
                valueTxt.setText(feature.getAttributes().get(key).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            TextView colonTxt= new TextView(getView().getContext());
            colonTxt.setText(":");
            tableRow.addView(keyTxt);
            tableRow.addView(colonTxt);
            tableRow.addView(valueTxt);
            tableLayout.addView(tableRow);
        }
    }
}
