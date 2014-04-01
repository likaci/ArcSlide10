package com.xiazhiri.ArcSlide10;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.tasks.geocode.LocatorReverseGeocodeResult;

import java.util.Map;

/**
 * Created by Administrator on 2014/3/29.
 */
public class TouchListener extends MapOnTouchListener {
		private int routeHandle = -1;
        MapView mMapView;
        ActivityMain activityMain;
        String touchMode = "Normal";

		@Override
		public void onLongPress(MotionEvent point) {
			// Our long press will clear the screen
			//mStops.clearFeatures();
			//mGraphicsLayer.removeAll();
			//mMapView.getCallout().hide();
			if (activityMain.mLocator == null) {
				activityMain.popToast("定位器未加载", true);
                return;
			}
			Point mapPoint = mMapView.toMapPoint(point.getX(), point.getY());
            activityMain.mGraphicsLayer.removeAll();
			Graphic graphic = new Graphic(mapPoint, new PictureMarkerSymbol(activityMain.getResources().getDrawable(R.drawable.icon_openmap_mark)));
			activityMain.mGraphicsLayer.addGraphic(graphic);
			String stopAddress = "";
			try {
				SpatialReference mapRef = mMapView.getSpatialReference();
				//LocatorReverseGeocodeResult result = mLocator.reverseGeocode(mapPoint, 5000, mapRef, mapRef);
				LocatorReverseGeocodeResult result = activityMain.mLocator.reverseGeocode(mapPoint, 5000, mapRef, mapRef);
				StringBuilder address = new StringBuilder();
				if (result != null && result.getAddressFields() != null) {
					Map<String, String> addressFields = result.getAddressFields();
					address.append(String.format("%s", addressFields.get("SingleKey")));
				}
				stopAddress = address.toString();
				activityMain.showCallout(stopAddress, mapPoint);

			} catch (Exception e) {
				Log.v("Reverse Geocode", e.getMessage());
				activityMain.showCallout(mapPoint.getX() + "," + mapPoint.getY(), mapPoint);
			}
			// Add the touch event as a stop
			//StopGraphic stop = new StopGraphic(graphic);
			//stop.setName(stopAddress.toString());
			//mStops.addFeature(stop);
		}

		@Override
		public boolean onSingleTap(MotionEvent point) {
            if (touchMode == "Measure") {
                Point mapPoint = mMapView.toMapPoint(point.getX(), point.getY());
                activityMain.measure.AddPoint(mapPoint);
            }
            return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent point) {

           /*
			// Return default behavior if we did not initialize properly.
			if (mRouteTask == null) {
				popToast("RouteTask uninitialized.", true);
				return super.onDoubleTap(point);
			}

			try {
				// Set the correct input spatial reference on the stops and the
				// desired output spatial reference on the RouteParameters object.
				SpatialReference mapRef = mMapView.getSpatialReference();
				RouteParameters params = mRouteTask.retrieveDefaultRouteTaskParameters();
				params.setOutSpatialReference(mapRef);
				mStops.setSpatialReference(mapRef);

				// Set the stops and since we want driving directions,
				// returnDirections==true
				params.setStops(mStops);
				params.setReturnDirections(true);

				// Perform the solve
				RouteResult results = mRouteTask.solve(params);

				// Grab the results; for offline routing, there will only be one
				// result returned on the output.
				Route result = results.getRoutes().get(0);

				// Remove any previous route Graphics
				if (routeHandle != -1)
					mGraphicsLayer.removeGraphic(routeHandle);

				// Add the route shape to the graphics layer
				Geometry geom = result.getRouteGraphic().getGeometry();
				routeHandle = mGraphicsLayer.addGraphic(new Graphic(geom, new SimpleLineSymbol(0x99990055, 5)));
				mMapView.getCallout().hide();

				// Get the list of directions from the result
				List<RouteDirection> directions = result.getRoutingDirections();

				// Find the directions spinner
				//Spinner dSpinner = (Spinner) findViewById(R.id.directionsSpinner);

				// Iterate through all of the individual directions items and
				// create a nicely fomatted string for each.
				List<String> formattedDirections = new ArrayList<String>();
				for (int i = 0; i < directions.size(); i++) {
					RouteDirection direction = directions.get(i);
					formattedDirections.add(String.format("%s\nGo %.2f %s For %.2f Minutes", direction.getText(),
							direction.getLength(), params.getDirectionsLengthUnit().name(), direction.getMinutes()));
				}

				// Add a summary String
				formattedDirections.add(0, String.format("Total time: %.2f Mintues", result.getTotalMinutes()));

				// Create a simple array adapter to visualize the directions in
				// the Spinner
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
						android.R.layout.simple_spinner_item, formattedDirections);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				//dSpinner.setAdapter(adapter);

				// Add a custom OnItemSelectedListener to the spinner to allow
				// panning to each directions item.
				//dSpinner.setOnItemSelectedListener(new DirectionsItemListener(directions));

			} catch (Exception e) {
				popToast("导航失败: " + e.getMessage(), true);
				e.printStackTrace();
			}
			*/
			return true;
		}

		public TouchListener(Context context,ActivityMain activity, MapView view) {
			super(context, view);
            mMapView = view;
            activityMain = activity;
		}
}
