package com.xiazhiri.ArcSlide10;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.map.Feature;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;
import com.esri.core.tasks.na.StopGraphic;
import com.esri.core.tasks.query.QueryParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索
 */
public class SearchTask {
    GraphicsLayer graphicsLayer;
    LocatorGeocodeResult geocodeResult;

    ActivityMain activityMain;
    Locator locator;
    //Set<Feature> features;
    List<Feature> features;
    FragmentSearchInfo fragmentSearchInfo;
    MapView mapView;
    public SearchTask(ActivityMain activityMain){
        features = new ArrayList<Feature>();
        this.activityMain = activityMain;
        graphicsLayer = new GraphicsLayer();
        activityMain.mMapView.addLayer(graphicsLayer);
        this.mapView = activityMain.mMapView;
        fragmentSearchInfo = new FragmentSearchInfo();
    }

    //地理编码 搜索按钮
    public void Search(String name){
        features.clear();
        for (Layer layer : mapView.getLayers()) {
            if (layer instanceof FeatureLayer) {
                ((FeatureLayer) layer).setSelectionColor(Color.CYAN);
                QueryParameters queryParameters = new QueryParameters();
                queryParameters.setWhere("NAME like '%" + name + "%'");
                ((FeatureLayer) layer).selectFeatures(queryParameters, ArcGISFeatureLayer.SELECTION_METHOD.NEW,null);
            }
        }
        if (features.isEmpty())
            return;
    }

    //反地理编码 长按屏幕查询
    public void Search(Point screenPoint){
        features.clear();
        Point mapPoint = mapView.toMapPoint(screenPoint);
        Map<String,Object> graphicAttribute = new HashMap<String, Object>();
        graphicAttribute.put("NAME","地图选点");
        Graphic graphic = new Graphic(mapPoint, new SimpleMarkerSymbol(Color.RED,2, SimpleMarkerSymbol.STYLE.CIRCLE), graphicAttribute);
        features.add(graphic);
        for (Layer layer : activityMain.mMapView.getLayers()) {
            if (layer instanceof FeatureLayer) {
                try {
                    FeatureLayer featureLayer = (FeatureLayer)layer;
                    Feature feature = featureLayer.getFeature(featureLayer.getFeatureIDs((float)screenPoint.getX(), (float)screenPoint.getY(), 5)[0]);
                    features.add(feature);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (features.isEmpty())
            return;
        setInfo();
    }

    public void setInfo() {
        fragmentSearchInfo = new FragmentSearchInfo();
        fragmentSearchInfo.features = features;
        FragmentTransaction fragmentTransaction = activityMain.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top);
        fragmentTransaction.replace(R.id.fragment_SearchInfo, fragmentSearchInfo).commit();
    }

    class Searcher extends AsyncTask<LocatorFindParameters, Void, List<LocatorGeocodeResult>> {
        protected void onPostExecute(List<LocatorGeocodeResult> result) {
            if (result == null || result.size() == 0) {
                //Toast.makeText(ActivityMain.this, "No result found.", Toast.LENGTH_LONG).show();
            } else {
                geocodeResult = result.get(0);
                activityMain.mMapView.zoomToResolution(geocodeResult.getLocation(), 2);

                Point point = new Point(geocodeResult.getLocation().getX(),geocodeResult.getLocation().getY());
                String pointName = geocodeResult.getAddress();
                //setInfoAndMark(point,pointName);
            }
        }
        @Override
        protected List<LocatorGeocodeResult> doInBackground(LocatorFindParameters... params) {
            List<LocatorGeocodeResult> results = null;
            try {
                results = locator.find(params[0]);
            } catch( Exception e ) {
                e.printStackTrace();
            }
            return results;
        }
    }

    class FragmentSearchInfo extends Fragment {

        ActivityMain activityMain;
        FrameLayout mFrameLayout;
        String infoName;
        Point point;
        Feature feature;
        List<Feature> features;
        final String TAG = "FragmentSearchInfo";
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            activityMain = (ActivityMain)getActivity();
            mFrameLayout = (FrameLayout)activityMain.findViewById(R.id.fragment_content);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View contextView = inflater.inflate(R.layout.fragment_search_info, container, false);
            //((TextView)contextView.findViewById(R.id.SearchInfoName)).setText(infoName);
            //((TextView)contextView.findViewById(R.id.SearchInfoCoord)).setText((int)point.getX() + "," + (int)point.getY());
            ((Button)contextView.findViewById(R.id.SearchInfoAddToRoute)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Envelope envelope = new Envelope();
                    feature.getGeometry().queryEnvelope(envelope);
                    activityMain.routingTask.AddPoint(new StopGraphic(envelope.getCenter()),feature.getAttributes().get("NAME").toString());
                }
            });

            ((Button)contextView.findViewById(R.id.SearchInfoPreItem)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showItemPre();
                }
            });

            ((Button)contextView.findViewById(R.id.SearchInfoNextItem)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showItemNext();
                }
            });

            ((Button)contextView.findViewById(R.id.ShowProductive)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentProductiveInfo fragmentProductiveInfo = new FragmentProductiveInfo();
                    fragmentProductiveInfo.feature = feature;
                    FragmentTransaction fragmentTransaction = activityMain.getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top);
                    fragmentTransaction.replace(R.id.fragment_ProductiveInfo,fragmentProductiveInfo).commit();
                }
            });

            return contextView;
        }

        @Override
        public void onResume() {
            super.onResume();
            showItem(features.get(0));
        }

        void showItem(Feature feature) {
            try {
                this.feature = feature;
                ((TextView) (getView().findViewById(R.id.SearchInfoName))).setText(feature.getAttributes().get("NAME").toString());
                Envelope envelope = new Envelope();
                feature.getGeometry().queryEnvelope(envelope);
                ((TextView) (getView().findViewById(R.id.SearchInfoCoord))).setText(String.format("%.2f,%.2f",
                        envelope.getCenterX(),envelope.getCenterY()));

                graphicsLayer.removeAll();
                Graphic graphic = new Graphic(envelope.getCenter(), new PictureMarkerSymbol(activityMain.getResources().getDrawable(R.drawable.icon_openmap_mark)),3);
                graphicsLayer.addGraphic(graphic);

                getView().findViewById(R.id.SearchInfoNextItem).setClickable(true);
                ((Button)getView().findViewById(R.id.SearchInfoNextItem)).setBackgroundResource(R.drawable.arrow_right_on);
                getView().findViewById(R.id.SearchInfoPreItem).setClickable(true);
                ((Button)getView().findViewById(R.id.SearchInfoPreItem)).setBackgroundResource(R.drawable.arrow_left_on);

                if (features.indexOf(feature) == (features.size()-1)){
                    getView().findViewById(R.id.SearchInfoNextItem).setClickable(false);
                    ((Button)getView().findViewById(R.id.SearchInfoNextItem)).setBackgroundResource(R.drawable.arrow_right_off);
                }
                if (features.indexOf(feature) == 0) {
                    getView().findViewById(R.id.SearchInfoPreItem).setClickable(false);
                    ((Button)getView().findViewById(R.id.SearchInfoPreItem)).setBackgroundResource(R.drawable.arrow_left_off);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "设置Item错误");
            }
        }

        void showItemPre() {
            try {
                showItem(features.get(features.indexOf(feature)-1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void showItemNext() {
            try {
                showItem(features.get(features.indexOf(feature)+1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

