package com.xiazhiri.ArcSlide10;

import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.TextView;
import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.tasks.na.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 异步规划路径
 */
public class RoutingTask {

    ActivityMain activityMain;
    RouteTask routeTask;
    NAFeaturesAsFeature stops = new NAFeaturesAsFeature();
    int stopsNum = 0;
    int routeHandler = -1;
    final String Tag = "RouteTask";
    FragmentRoutingInfo fragmentRoutingInfo;
    GraphicsLayer graphicsLayer;


    public RoutingTask(ActivityMain activityMain,String databasePath,String networkName ){
        this.activityMain = activityMain;
        try {
            routeTask = RouteTask.createLocalRouteTask(databasePath,networkName);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(Tag, "创建RouteTask错误");
            return;
        }
        Log.v(Tag,"创建RouteTask完成");
        graphicsLayer = new GraphicsLayer();
        this.activityMain.mMapView.addLayer(graphicsLayer);
    }

    public void AddPoint(StopGraphic stopGraphic, String pointName)
    {
        stops.addFeature(stopGraphic);
        stopsNum++;
        if (stopsNum == 1) {
            fragmentRoutingInfo = new FragmentRoutingInfo();
            fragmentRoutingInfo.startPointName = "起点: " + pointName;
            FragmentTransaction fragmentTransaction = activityMain.getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_RoutingInfo, fragmentRoutingInfo).commit();
            Graphic graphic = new Graphic(stopGraphic.getGeometry(), new PictureMarkerSymbol(activityMain.getResources().getDrawable(R.drawable.icon_openmap_focuse_mark)),3);
            graphicsLayer.addGraphic(graphic);
        }
        else {
            ((TextView)fragmentRoutingInfo.getView().findViewById(R.id.RoutingInfoStopPoint)).setText("终点: " + pointName);

            Graphic graphic = new Graphic(stopGraphic.getGeometry(), new PictureMarkerSymbol(activityMain.getResources().getDrawable(R.drawable.icon_openmap_focuse_mark)),3);
            graphicsLayer.addGraphic(graphic);
        }
    }

    public void Reset()
    {
        stops.clearFeatures();
        stopsNum = 0;
    }

    public void FindRoute(){

        if (routeTask == null) {
            activityMain.popToast("RouteTask uninitialized.", true);
            return;
        }
        try {
            // Set the correct input spatial reference on the stops and the
            // desired output spatial reference on the RouteParameters object.
            SpatialReference mapRef = activityMain.mMapView.getSpatialReference();
            RouteParameters params = routeTask.retrieveDefaultRouteTaskParameters();
            params.setOutSpatialReference(mapRef);
            stops.setSpatialReference(mapRef);
            params.setStops(stops);
            params.setReturnDirections(true);

            RouteResult results = routeTask.solve(params);

            Route result = results.getRoutes().get(0);

            if (routeHandler != -1)
                 graphicsLayer.removeGraphic(routeHandler);

            Geometry geom = result.getRouteGraphic().getGeometry();
            routeHandler = graphicsLayer.addGraphic(new Graphic(geom, new SimpleLineSymbol(0x99990055, 5)));

            List<RouteDirection> directions = result.getRoutingDirections();

            // Find the directions spinner
            // dSpinner = (Spinner) findViewById(R.id.directionsSpinner);

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

            /*
            // Create a simple array adapter to visualize the directions in
            // the Spinner
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                    android.R.layout.simple_spinner_item, formattedDirections);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dSpinner.setAdapter(adapter);

            // Add a custom OnItemSelectedListener to the spinner to allow
            // panning to each directions item.
            dSpinner.setOnItemSelectedListener(new DirectionsItemListener(directions));
            */
        } catch (Exception e) {
            //popToast("Solve Failed: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

}
