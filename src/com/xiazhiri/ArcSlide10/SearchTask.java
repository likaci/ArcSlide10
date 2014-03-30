package com.xiazhiri.ArcSlide10;

import java.util.List;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;

import android.graphics.Color;
import android.os.AsyncTask;

public class SearchTask extends AsyncTask<LocatorFindParameters, Void, List<LocatorGeocodeResult>> {

	Locator mLocator;
	MapView mMapView;
	GraphicsLayer mGraphicsLayer;
	LocatorGeocodeResult geocodeResult;
	Callout locationCallout;

	public SearchTask(Locator locator,MapView mapView) {
		mLocator = locator;
		mMapView = mapView;
	}

	protected void onPostExecute(List<LocatorGeocodeResult> result) {
			if (result == null || result.size() == 0) {
				//Toast.makeText(ActivityMain.this, "No result found.", Toast.LENGTH_LONG).show();
			} else {
				geocodeResult = result.get(0);
				//dialog = ProgressDialog.show(mMapView.getContext(), "Geocoder", "Searching for address ...");
				Geometry resultLocGeom = geocodeResult.getLocation();
				SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol( Color.BLUE, 20, SimpleMarkerSymbol.STYLE.CIRCLE);
				Graphic resultLocation = new Graphic(resultLocGeom, resultSymbol);
				mGraphicsLayer.addGraphic(resultLocation);

				// // create callout for return address
				locationCallout = mMapView.getCallout();
				String place = geocodeResult.getAddress();
				//locationCallout.setContent(loadView(place));
				// locationCallout.show();

				// create text symbol for return address
				TextSymbol resultAddress = new TextSymbol(12, geocodeResult.getAddress(), Color.BLACK);
				// create offset for text
				resultAddress.setOffsetX(10);
				resultAddress.setOffsetY(50);
				// create a graphic object for address text
				Graphic resultText = new Graphic(resultLocGeom, resultAddress);
				// add address text graphic to location graphics layer
				//locationLayer.addGraphic(resultText);
				// zoom to geocode result

				mMapView.zoomToResolution(geocodeResult.getLocation(), 2);
				// create a runnable to be added to message queue
				//handler.post(new MyRunnable());
			}
	}

	@Override
	protected List<LocatorGeocodeResult> doInBackground( LocatorFindParameters... params) {
			List<LocatorGeocodeResult> results = null;
			try {
				results = mLocator.find(params[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return results;
	}
}

