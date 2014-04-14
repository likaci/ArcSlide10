package com.xiazhiri.ArcSlide10;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import com.esri.android.map.FeatureLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.core.geometry.Point;
import com.esri.core.map.Feature;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.tasks.identify.IdentifyTask;
import com.esri.core.tasks.na.StopGraphic;

/**
 * 地图点击事件监听器
 */
public class TouchListener extends MapOnTouchListener {
        MapView mMapView;
        ActivityMain activityMain;
        String touchMode = "Normal";
        public StopGraphic currentStopGraphic;
        IdentifyTask identifyTask;

		@Override
		public void onLongPress(MotionEvent point) {
            Point mapPoint = mMapView.toMapPoint(point.getX(), point.getY());
            activityMain.searchTask.Search(mapPoint);
		}

		@Override
		public boolean onSingleTap(MotionEvent point) {
            if (touchMode == "Measure") {
                Point mapPoint = mMapView.toMapPoint(point.getX(), point.getY());
                activityMain.measure.AddPoint(mapPoint);
                return false;
            }
            else {
                activityMain.mGraphicsLayer.setRenderer(new SimpleRenderer(new SimpleFillSymbol(Color.BLACK)));
                Point mapPoint = mMapView.toMapPoint(point.getX(), point.getY());
                StringBuilder featureNameValue = new StringBuilder();
                for (Layer layer : mMapView.getLayers()) {
                    if (layer instanceof FeatureLayer) {
                        try {
                            FeatureLayer featureLayer = (FeatureLayer)layer;
                            Feature feature = featureLayer.getFeature(featureLayer.getFeatureIDs(point.getX(), point.getY(), 50)[0]);
                            featureNameValue.append(featureLayer.getName() + "\n");
                            for (String name : feature.getAttributes().keySet()) {
                                featureNameValue.append(name + ":" + feature.getAttributes().get(name).toString() + "\n");
                            }

                            featureNameValue.append("\n");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                activityMain.showCallout(featureNameValue.toString(),mapPoint);
            }
                return super.onSingleTap(point);
		}

		@Override
		public boolean onDoubleTap(MotionEvent point) {
			return super.onDoubleTap(point);
		}

		public TouchListener(Context context,ActivityMain activity, MapView view) {
			super(context, view);
            mMapView = view;
            activityMain = activity;
		}
}
