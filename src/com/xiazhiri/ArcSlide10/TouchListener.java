package com.xiazhiri.ArcSlide10;

import android.content.Context;
import android.view.MotionEvent;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.core.geometry.Point;
import com.esri.core.tasks.na.StopGraphic;

/**
 * 地图点击事件监听器
 */
public class TouchListener extends MapOnTouchListener {
        MapView mMapView;
        ActivityMain activityMain;
        String touchMode = "Normal";
        public StopGraphic currentStopGraphic;

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
            else
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
