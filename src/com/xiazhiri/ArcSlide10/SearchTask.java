package com.xiazhiri.ArcSlide10;

import android.os.AsyncTask;
import android.support.v4.app.FragmentTransaction;
import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;
import com.esri.core.tasks.geocode.LocatorReverseGeocodeResult;

import java.util.List;
import java.util.Map;

/**
 * 异步搜索
 */
public class SearchTask {
    GraphicsLayer graphicsLayer;
    LocatorGeocodeResult geocodeResult;

    ActivityMain activityMain;
    Locator locator;

    public SearchTask(ActivityMain activityMain, String locatorPath){
        this.activityMain = activityMain;
        try {
            locator = Locator.createLocalLocator(locatorPath);
        } catch (Exception e) {
            activityMain.popToast("加载定位器失败",true);
            e.printStackTrace();
        }
        graphicsLayer = new GraphicsLayer();
        activityMain.mMapView.addLayer(graphicsLayer);
    }

    //地理编码 搜索按钮
    public void Search(LocatorFindParameters locatorFindParameters){
        Searcher searcher = new Searcher();
        searcher.execute(locatorFindParameters);
    }

    //反地理编码 长按屏幕查询
    public void Search(Point point){
        SpatialReference mapRef = activityMain.mMapView.getSpatialReference();
        LocatorReverseGeocodeResult result = null;
        try {
            //result = mLocator.reverseGeocode(mapPoint, 5000, mapRef, mapRef);
            result = locator.reverseGeocode(point, 5000, mapRef, mapRef);
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder address = new StringBuilder();
        if (result != null && result.getAddressFields() != null) {
            Map<String, String> addressFields = result.getAddressFields();
            address.append(String.format("%s", addressFields.get("SingleKey")));
            setInfoAndMark(point,address.toString());
        }
    }

    public void setInfoAndMark(Point point, String name) {
        graphicsLayer.removeAll();
        Graphic graphic = new Graphic(point, new PictureMarkerSymbol(activityMain.getResources().getDrawable(R.drawable.icon_openmap_mark)),3);
        graphicsLayer.addGraphic(graphic);

        FragmentSearchInfo fragmentSearchInfo = new FragmentSearchInfo();
        fragmentSearchInfo.infoName = name;
        fragmentSearchInfo.point = point;
        FragmentTransaction fragmentTransaction = activityMain.getSupportFragmentManager().beginTransaction();
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

                setInfoAndMark(point,pointName);
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
}

