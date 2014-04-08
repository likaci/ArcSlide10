package com.xiazhiri.ArcSlide10;

import android.os.AsyncTask;
import com.esri.core.tasks.na.NAFeaturesAsFeature;
import com.esri.core.tasks.na.RouteTask;
import com.esri.core.tasks.na.StopGraphic;

/**
 * 异步规划路径
 */
public class RoutingTask extends AsyncTask {

    ActivityMain activityMain;
    RouteTask routeTask;
    NAFeaturesAsFeature stops = new NAFeaturesAsFeature();
    final String Tag = "RouteTask";

    public RoutingTask(){

    }

//    public RoutingTask(ActivityMain activityMain,String databasePath,String networkName ){
//        this.activityMain = activityMain;
//        try {
//            routeTask = RouteTask.createLocalRouteTask(databasePath,networkName);
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.e(Tag,"创建RouteTask错误");
//            return;
//        }
//        Log.v(Tag,"创建RouteTask完成");
//    }

    public void AddPoint(StopGraphic stopGraphic)
    {
        stops.addFeature(stopGraphic);
    }

    public void Reset()
    {
        stops.clearFeatures();
    }

    @Override
    protected Object doInBackground(Object[] objects) {
//        // Return default behavior if we did not initialize properly.
//        if (routeTask == null) {
//            activityMain.popToast("RouteTask uninitialized.", true);
//            return false;
//        }
//        try {
//            // Set the correct input spatial reference on the stops and the
//            // desired output spatial reference on the RouteParameters object.
//            SpatialReference mapRef = activityMain.mMapView.getSpatialReference();
//            RouteParameters params = routeTask.retrieveDefaultRouteTaskParameters();
//            params.setOutSpatialReference(mapRef);
//            stops.setSpatialReference(mapRef);
//
//            // Set the stops and since we want driving directions,
//            // returnDirections==true
//            params.setStops(stops);
//            params.setReturnDirections(true);
//
//            // Perform the solve
//            RouteResult results = routeTask.solve(params);
//
//            // Grab the results; for offline routing, there will only be one
//            // result returned on the output.
//            Route result = results.getRoutes().get(0);
//
//            // Remove any previous route Graphics
////            if (routeHandle != -1)
////                mGraphicsLayer.removeGraphic(routeHandle);
//
//            // Add the route shape to the graphics layer
//            Geometry geom = result.getRouteGraphic().getGeometry();
//            //routeHandle = mGraphicsLayer.addGraphic(new Graphic(geom, new SimpleLineSymbol(0x99990055, 5)));
//            activityMain.mGraphicsLayer.addGraphic(new Graphic(geom, new SimpleLineSymbol(0x99990055, 5)));
//            activityMain.mMapView.getCallout().hide();
//
//            // Get the list of directions from the result
//            List<RouteDirection> directions = result.getRoutingDirections();
//
//            // Find the directions spinner
////            Spinner dSpinner = (Spinner) findViewById(R.id.directionsSpinner);
//
//            // Iterate through all of the individual directions items and
//            // create a nicely fomatted string for each.
//            List<String> formattedDirections = new ArrayList<String>();
//            for (int i = 0; i < directions.size(); i++) {
//                RouteDirection direction = directions.get(i);
//                formattedDirections.add(String.format("%s\nGo %.2f %s For %.2f Minutes", direction.getText(),
//                        direction.getLength(), params.getDirectionsLengthUnit().name(), direction.getMinutes()));
//            }
//
//            // Add a summary String
//            formattedDirections.add(0, String.format("Total time: %.2f Mintues", result.getTotalMinutes()));
//
//            // Create a simple array adapter to visualize the directions in
//            // the Spinner
////            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
////                    android.R.layout.simple_spinner_item, formattedDirections);
////            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
////            dSpinner.setAdapter(adapter);
////
////            // Add a custom OnItemSelectedListener to the spinner to allow
////            // panning to each directions item.
////            dSpinner.setOnItemSelectedListener(new DirectionsItemListener(directions));
//
//        } catch (Exception e) {
//            //popToast("Solve Failed: " + e.getMessage(), true);
//            e.printStackTrace();
//        }
//        return null;
        return null;
    }
}
