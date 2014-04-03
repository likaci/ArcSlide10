 package com.xiazhiri.ArcSlide10;

 import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
 import android.text.Html;
 import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.GraphicsLayer.RenderingMode;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.TiledLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;
import com.esri.core.tasks.na.RouteTask;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ActivityMain extends SherlockFragmentActivity{

	MapView mMapView;
	public LocationDisplayManager ldm;
	TiledLayer tiledLayer;
	RouteTask mRouteTask = null;
	Locator mLocator = null;
	GraphicsLayer mGraphicsLayer = new GraphicsLayer(RenderingMode.DYNAMIC);
	View mCallout = null;

	String extern = Environment.getExternalStorageDirectory().getPath();
	String tpkPath     = "/Likaci/DY.tpk";
	String locatorPath = "/Likaci/locator/DY.loc";
	String networkPath = "/Likaci/data/line.geodatabase";
	String networkName = "Streets_ND";

	Measure measure = null;
    TouchListener touchListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mMapView = (MapView)findViewById(R.id.map);
		tiledLayer = new ArcGISLocalTiledLayer(extern + tpkPath);
		//mMapView.addLayer(new ArcGISTiledMapServiceLayer("http://cache1.arcgisonline.cn/ArcGIS/rest/services/ChinaOnlineStreetColor/MapServer"));
		mMapView.addLayer(tiledLayer);
		mMapView.addLayer(mGraphicsLayer);

        /*
        FragmentContent fragmentContent = new FragmentContent();
		fragmentContent.setArguments(getIntent().getExtras());
		getSupportFragmentManager().beginTransaction().add(R.id.fragment_content,fragmentContent).commit();
		*/

        //region init menu
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
        //endregion

		getSupportActionBar().setHomeButtonEnabled(true);

		//设置定位器
		ldm = mMapView.getLocationDisplayManager();
		//ldm.setLocationListener(locationListener);

		initializeRoutingAndGeocoding();

        touchListener = new TouchListener(ActivityMain.this,this,mMapView);
		mMapView.setOnTouchListener(touchListener);
	}

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
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


        MenuItem menuItem = menu.add("替换");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                FragmentContent fragmentContent = new FragmentContent();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_content,fragmentContent);
                fragmentTransaction.commit();
                return false;
            }
        });

        menuItem = menu.add("切换");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.hide(getSupportFragmentManager().getFragments().get(1));
                fragmentTransaction.commit();
                return false;
            }
        });


        menuItem = menu.add("拍照(内嵌)");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent();
                intent.setClass(ActivityMain.this, ActivityCamera.class);
                ActivityMain.this.startActivity(intent);
                return false;
            }
        });


        menuItem = menu.add("拍照(系统)");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                //File picture = new File(Environment.getExternalStorageDirectory().getPath() + "/" + timeStamp + ".jpg");
                File picture = new File(Environment.getExternalStorageDirectory().getPath() + "/" +  "Hello.jpg");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(picture));
                //ActivityMain.this.startActivity(intent);
                startActivityForResult(intent,1);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
	}


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        File picture = new File(Environment.getExternalStorageDirectory().getPath() + "/" +  "Hello.jpg");
        try {
        FileInputStream in = new FileInputStream(picture);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 10;
        String imagePath = picture.getAbsolutePath();
        Toast.makeText(ActivityMain.this,imagePath,Toast.LENGTH_LONG).show();
        Bitmap bmp = BitmapFactory.decodeStream(in, null, options);
        //picture.setImageBitmap(bmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        uploadFile("http://192.168.42.147/index.php");


    }

 private void uploadFile(String uploadUrl)
  {
      String end = "\r\n";
      String twoHyphens = "--";
      String boundary = "******";
      String srcPath = Environment.getExternalStorageDirectory().getPath() + "/" +  "Hello.jpg";
      try
      {
          URL url = new URL(uploadUrl);
          HttpURLConnection httpURLConnection = (HttpURLConnection) url
                  .openConnection();
          // 设置每次传输的流大小，可以有效防止手机因为内存不足崩溃
          // 此方法用于在预先不知道内容长度时启用没有进行内部缓冲的 HTTP 请求正文的流。
          httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
          // 允许输入输出流
          httpURLConnection.setDoInput(true);
          httpURLConnection.setDoOutput(true);
          httpURLConnection.setUseCaches(false);
          // 使用POST方法
          httpURLConnection.setRequestMethod("POST");
          httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
          httpURLConnection.setRequestProperty("Charset", "UTF-8");
          httpURLConnection.setRequestProperty("Content-Type",
                  "multipart/form-data;boundary=" + boundary);

          DataOutputStream dos = new DataOutputStream(
                  httpURLConnection.getOutputStream());
          dos.writeBytes(twoHyphens + boundary + end);
          dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\""
                  + srcPath.substring(srcPath.lastIndexOf("/") + 1)
                  + "\""
                  + end);
          dos.writeBytes(end);

          FileInputStream fis = new FileInputStream(srcPath);
          byte[] buffer = new byte[8192]; // 8k
          int count = 0;
          // 读取文件
          while ((count = fis.read(buffer)) != -1)
          {
              dos.write(buffer, 0, count);
          }
          fis.close();

          dos.writeBytes(end);
          dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
          dos.flush();

          InputStream is = httpURLConnection.getInputStream();
          InputStreamReader isr = new InputStreamReader(is, "utf-8");
          BufferedReader br = new BufferedReader(isr);
          String result = "";
          while (br.readLine() != null)
          {
              result= result + "<br/>" +br.readLine();
          }

          //Toast.makeText(ActivityMain.this, result, Toast.LENGTH_LONG).show();
          Dialog dialog = new Dialog(this);
          TextView textView = new TextView(ActivityMain.this);
          textView.setText(Html.fromHtml(result));
          dialog.setContentView(textView);
          dialog.show();
          dos.close();
          is.close();

      } catch (Exception e)
      {
          e.printStackTrace();
          setTitle(e.getMessage());
      }
  }


    //region 地图事件
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
    //endregion

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

}