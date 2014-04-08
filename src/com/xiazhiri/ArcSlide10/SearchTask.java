package com.xiazhiri.ArcSlide10;

import android.graphics.Color;
import android.os.AsyncTask;
import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
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

    //地理编码
    public void Search(LocatorFindParameters locatorFindParameters){
        Searcher searcher = new Searcher();
        searcher.execute(locatorFindParameters);
    }

    //反地理编码
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
            activityMain.showCallout(address.toString(),point);
        }
    }

    class Searcher extends AsyncTask<LocatorFindParameters, Void, List<LocatorGeocodeResult>> {
        protected void onPostExecute(List<LocatorGeocodeResult> result) {
            if (result == null || result.size() == 0) {
                //Toast.makeText(ActivityMain.this, "No result found.", Toast.LENGTH_LONG).show();
            } else {
                geocodeResult = result.get(0);
                //dialog = ProgressDialog.show(mMapView.getContext(), "Geocoder", "Searching for address ...");
                Geometry resultLocGeom = geocodeResult.getLocation();
                SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol(Color.BLUE, 20, SimpleMarkerSymbol.STYLE.CIRCLE);
                Graphic resultLocation = new Graphic(resultLocGeom, resultSymbol);
                graphicsLayer.addGraphic(resultLocation);

                activityMain.mMapView.zoomToResolution(geocodeResult.getLocation(), 2);

                String place = geocodeResult.getAddress();
                activityMain.showCallout(place, geocodeResult.getLocation());

                /*
                TextSymbol resultAddress = new TextSymbol(12, geocodeResult.getAddress(), Color.BLACK);
                resultAddress.setOffsetX(10);
                resultAddress.setOffsetY(50);
                Graphic resultText = new Graphic(resultLocGeom, resultAddress);
                graphicsLayer.addGraphic(resultText);
                */
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

