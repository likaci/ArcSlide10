 package com.xiazhiri.ArcSlide10;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentTransaction;
import android.text.Layout;
import android.text.method.Touch;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.GraphicsLayer.RenderingMode;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.TiledLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Line;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;
import com.esri.core.tasks.geocode.LocatorReverseGeocodeResult;
import com.esri.core.tasks.na.NAFeaturesAsFeature;
import com.esri.core.tasks.na.Route;
import com.esri.core.tasks.na.RouteDirection;
import com.esri.core.tasks.na.RouteParameters;
import com.esri.core.tasks.na.RouteResult;
import com.esri.core.tasks.na.RouteTask;
import com.esri.core.tasks.na.StopGraphic;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class ActivityMain extends SherlockFragmentActivity{

	MapView mMapView;
	public LocationDisplayManager ldm;
	Callout locationCallout;
	TiledLayer tiledLayer;
	RouteTask mRouteTask = null;
	Locator mLocator = null;
	NAFeaturesAsFeature mStops = new NAFeaturesAsFeature();
	GraphicsLayer mGraphicsLayer = new GraphicsLayer(RenderingMode.DYNAMIC);
	View mCallout = null;
	LocatorGeocodeResult geocodeResult;

	String extern = Environment.getExternalStorageDirectory().getPath();
	String tpkPath     = "/Likaci/DY.tpk";
	String locatorPath = "/Likaci/locator/DY.loc";
	String networkPath = "/Likaci/data/line.geodatabase";
	String networkName = "Streets_ND";

	Measure measure = null;
    String touchMode;
    TouchListener touchListener;

	static ProgressDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mMapView = (MapView)findViewById(R.id.map);

		tiledLayer = new ArcGISLocalTiledLayer(extern + tpkPath);
		//mMapView.addLayer(new ArcGISTiledMapServiceLayer("http://cache1.arcgisonline.cn/ArcGIS/rest/services/ChinaOnlineStreetColor/MapServer"));
		mMapView.addLayer(tiledLayer);
		mMapView.addLayer(mGraphicsLayer);

		//FragmentMap fragmentMap = new FragmentMap();
		//fragmentMap.setArguments(getIntent().getExtras());
		//getSupportFragmentManager().beginTransaction().add(R.id.content_in_mainActivity,fragmentMap).commit();
		//fragmentMap.mapView.addLayer(new ArcGISTiledMapServiceLayer("http://cache1.arcgisonline.cn/ArcGIS/rest/services/ChinaOnlineStreetColor/MapServer"));


        SlidingMenu menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        menu.setShadowWidth(15);
        //menu.setShadowDrawable(R.drawable.shadow);
        menu.setBehindOffset(160);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.frame_menu);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        FragmentMenu fragmentMenu = new FragmentMenu();
        fragmentTransaction.replace(R.id.fragment_menu,fragmentMenu).commit();

		getSupportActionBar().setHomeButtonEnabled(true);

		//设置定位器
		ldm = mMapView.getLocationDisplayManager();
		//ldm.setLocationListener(locationListener);

		initializeRoutingAndGeocoding();

        touchListener = new TouchListener(ActivityMain.this,this,mMapView);
		mMapView.setOnTouchListener(touchListener);

	}

	private void initializeRoutingAndGeocoding() {
		new Thread(new Runnable() {
			public void run() {
				try {
					mLocator = Locator.createLocalLocator(extern + locatorPath);
					mRouteTask = RouteTask.createLocalRouteTask(extern + networkPath, networkName);
				} catch (Exception e) {
					popToast("加载离线数据失败" + e.getMessage(), true);
					e.printStackTrace();
				}
			}
		}).start();
	}


    /*
    OnClickListener onclick = new OnClickListener(){
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnLocation:
				ToggleButton btn = (ToggleButton)v;
				if (btn.isChecked())
					ldm.start();
				else
					ldm.stop();
				break;
			case R.id.btnTest:
				Polygon polygon = mMapView.getExtent();
				break;
			case R.id.btnTest2:
				Envelope envelope = new Envelope(12738443.640463805, 3852003.9702266017, 13652450.889367465, 4900256.033812986);
				mMapView.setExtent(envelope);
				SearchTask SearchTask = new SearchTask(mLocator, mMapView);
				LocatorFindParameters lfpFindParameters = new LocatorFindParameters("test");
				SearchTask.execute(lfpFindParameters);
				mMapView.setMaxScale(mMapView.getMaxScale()/2);
				mMapView.setScale(mMapView.getScale()/2);
				break;
			case R.id.btnSearch:
				String address = ((TextView)findViewById(R.id.txtSearch)).getText().toString();
				popToast(address, true);
				LocatorFindParameters findParams = new LocatorFindParameters(address);
				findParams.setMaxLocations(10);
				findParams.setOutSR(mMapView.getSpatialReference());
				List<LocatorGeocodeResult> results = null;
				try {
					results = mLocator.find(findParams);
				} catch (Exception e) {
					popToast("", true);
					e.printStackTrace();
				}

				if(results == null || results.size() == 0){
					popToast("", true);
				} else {
					LocatorGeocodeResult result = results.get(0);
					// Get the returned geometry, create a Graphic from it, and add to GraphicsLayer
					Point resultLocGeom = result.getLocation();
					SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol(Color.BLUE, 23, STYLE.CIRCLE);
					Graphic resultLocation = new Graphic(resultLocGeom, resultSymbol);
					mGraphicsLayer.addGraphic(resultLocation);

					// Create a text symbol for return address with a slight offset
					TextSymbol resultAddress = new TextSymbol(16, result.getAddress(), Color.BLACK);
					resultAddress.setOffsetX(-2);
					resultAddress.setOffsetY(1);

					// Create a graphic object for address text, and add to GraphicsLayer
					//Graphic resultText = new Graphic(resultLocGeom, resultAddress);
					//mGraphicsLayer.addGraphic(resultText);

					// Zoom to the Geocoding result
					mMapView.zoomTo(resultLocGeom, 10);
					showCallout(result.getAddress(),resultLocGeom);
				}
				break;
			case R.id.btnMeasure:
				ToggleButton tbtn = (ToggleButton)findViewById(R.id.btnMeasure);
				if(!tbtn.isChecked())
					measure.Reset();
				break;

			default:
				break;
			}
		}
	};
    */

	LocationListener locationListener = new LocationListener() {

		Line line = new Line();
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		public void onProviderEnabled(String provider) {

		}

		public void onProviderDisabled(String provider) {

		}

		public void onLocationChanged(Location location) {
			double locy = location.getLatitude();
			double locx = location.getLongitude();
			Point wgsPoint = new Point(locx, locy);
			Point mapPoint = (Point)GeometryEngine.project(wgsPoint,SpatialReference.create(4326),mMapView.getSpatialReference());
			line.setEnd(mapPoint);
		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//Toast.makeText(ActivityMain.this,item.getItemId() + item.getTitle().toString(),Toast.LENGTH_SHORT).show();
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        /*
        menu.add("Search")
                .setIcon(R.drawable.abs__ic_search)
                .setActionView(R.layout.collapsible_edittext)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                */
        getSupportMenuInflater().inflate(R.menu.activity_main, menu);

        SearchView searchView = new SearchView(getSupportActionBar().getThemedContext());
        searchView.setQueryHint("输入地名");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                LocatorFindParameters findParams = new LocatorFindParameters(query);
                findParams.setMaxLocations(10);
                findParams.setOutSR(mMapView.getSpatialReference());
                List<LocatorGeocodeResult> results = null;

                SearchTask searchTask = new SearchTask(mLocator,mMapView);
                try {
                    results = mLocator.find(findParams);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if(results == null || results.size() == 0){
                    popToast("", true);
                } else {
                    LocatorGeocodeResult result = results.get(0);
                    // Get the returned geometry, create a Graphic from it, and add to GraphicsLayer
                    Point resultLocGeom = result.getLocation();
                    SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol(Color.BLUE, 23, STYLE.CIRCLE);
                    Graphic resultLocation = new Graphic(resultLocGeom, resultSymbol);
                    mGraphicsLayer.addGraphic(resultLocation);

                    // Create a text symbol for return address with a slight offset
                    TextSymbol resultAddress = new TextSymbol(16, result.getAddress(), Color.BLACK);
                    resultAddress.setOffsetX(-2);
                    resultAddress.setOffsetY(1);

                    // Create a graphic object for address text, and add to GraphicsLayer
                    //Graphic resultText = new Graphic(resultLocGeom, resultAddress);
                    //mGraphicsLayer.addGraphic(resultText);

                    // Zoom to the Geocoding result
                    mMapView.zoomTo(resultLocGeom, 10);
                    showCallout(result.getAddress(),resultLocGeom);
                }

                ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        MenuItem menuItemSearch = menu.findItem(R.id.menu_search);
        menuItemSearch.setActionView(searchView);
        return super.onCreateOptionsMenu(menu);
	}

	//start 地图事件
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	@Override
	protected void onPause() {
		super.onPause();
		mMapView.pause();
	}
	@Override
	protected void onResume() {
		//ee
		super.onResume();
		mMapView.unpause();
	}
	//end

	public void showCallout(String text, Point location) {

		// If the callout has never been created, inflate it
		if (mCallout == null) {
			LayoutInflater inflater = (LayoutInflater) getApplication().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mCallout = inflater.inflate(R.layout.callout, null);
		}

		// Show the callout with the given text at the given location
		((TextView) mCallout.findViewById(R.id.calloutText)).setText(text);
        ((Button)mCallout.findViewById(R.id.btnHello)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ActivityMain.this,"hello",Toast.LENGTH_SHORT).show();
            }
        });
		mMapView.getCallout().show(location, mCallout);
		mMapView.getCallout().setMaxWidth(700);
	}

	public void popToast(final String message, final boolean show) {
		if (!show)
			return;
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(ActivityMain.this, message, Toast.LENGTH_SHORT).show();
			}
		});
	}

}