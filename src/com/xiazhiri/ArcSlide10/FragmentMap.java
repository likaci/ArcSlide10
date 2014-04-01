package com.xiazhiri.ArcSlide10;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;

public class FragmentMap extends Fragment {

	public MapView mapView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frame_map, container,false);
		mapView = (MapView)view.findViewById(R.id.map_in_fragment);
		//mapView.addLayer(new ArcGISTiledMapServiceLayer("http://cache1.arcgisonline.cn/ArcGIS/rest/services/ChinaOnlineStreetColor/MapServer"));

        ((Button)view.findViewById(R.id.btnInFragment))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((Button)view).setText("hello");
                        mapView.addLayer(new ArcGISTiledMapServiceLayer("http://cache1.arcgisonline.cn/ArcGIS/rest/services/ChinaOnlineStreetColor/MapServer"));
                    }
                });

		return view;
	}
}
