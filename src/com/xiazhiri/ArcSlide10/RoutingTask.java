package com.xiazhiri.ArcSlide10;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
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
    final String TAG = "RouteTask";
    FragmentRoutingInfo fragmentRoutingInfo;
    GraphicsLayer graphicsLayer;


    public RoutingTask(ActivityMain activityMain,String databasePath,String networkName ){
        this.activityMain = activityMain;
        try {
            routeTask = RouteTask.createLocalRouteTask(databasePath,networkName);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "创建RouteTask错误");
            return;
        }
        Log.v(TAG,"创建RouteTask完成");
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

            List<String> formattedDirections = new ArrayList<String>();
            for (int i = 0; i < directions.size(); i++) {
                RouteDirection direction = directions.get(i);
                formattedDirections.add(String.format("%s\nGo %.2f %s For %.2f 分钟", direction.getText(),
                        direction.getLength(), params.getDirectionsLengthUnit().name(), direction.getMinutes()));
                Geometry geometry = direction.getGeometry();
                if (geometry instanceof Polyline) {
                    Polyline polyline = (Polyline)geometry;
                    Point pointStart = polyline.getPoint(0);
                    Point pointEnd = polyline.getPoint(polyline.getPointCount()-1);
                    graphicsLayer.addGraphic(new Graphic(pointStart, new SimpleMarkerSymbol(Color.RED,30,SimpleMarkerSymbol.STYLE.CROSS)));
                    graphicsLayer.addGraphic(new Graphic(pointEnd, new SimpleMarkerSymbol(Color.RED,30,SimpleMarkerSymbol.STYLE.CROSS)));
                }
            }

            formattedDirections.add(0, String.format("Total time: %.2f Mintues", result.getTotalMinutes()));

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(activityMain.getBaseContext(),
                    android.R.layout.simple_spinner_item, formattedDirections);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner dSpinner = (Spinner)fragmentRoutingInfo.getView().findViewById(R.id.RoutingInfoSpinner);
            dSpinner.setAdapter(adapter);

            //dSpinner.setOnItemSelectedListener(new DirectionsItemListener(directions));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class FragmentRoutingInfo extends Fragment implements View.OnTouchListener {

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

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return false;
        }
    }
}
