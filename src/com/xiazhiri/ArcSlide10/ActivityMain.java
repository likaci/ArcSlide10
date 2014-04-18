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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.esri.android.map.*;
import com.esri.android.map.GraphicsLayer.RenderingMode;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.core.geodatabase.Geodatabase;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.geometry.Point;
import com.esri.core.tasks.query.QueryParameters;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityMain extends SherlockFragmentActivity{

    MapView mMapView;
    public LocationDisplayManager ldm;
    TiledLayer tiledLayer;
    RoutingTask routingTask;
    GraphicsLayer mGraphicsLayer = new GraphicsLayer(RenderingMode.DYNAMIC);
    View mCallout = null;

    String extern = Environment.getExternalStorageDirectory().getPath();
    String tpkPath     = "/Likaci/BN3857.tpk";
    String locatorPath = "/Likaci/locator/DY.loc";
    String networkPath = "/Likaci/data/bn3857.geodatabase";
    String networkName = "Road_ND";

    Measure measure = null;
    TouchListener touchListener;
    SearchTask searchTask;

    FragmentMenuNormal fragmentMenuNormal = new FragmentMenuNormal();
    FragmentMenuPro fragmentMenuPro = new FragmentMenuPro();
    FragmentTransaction fragmentTransaction;
    boolean featureLayerLoaded;
    SlidingUpPanelLayout slidingUpPanel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMapView = (MapView)findViewById(R.id.map);
        tiledLayer = new ArcGISLocalTiledLayer(extern + tpkPath);
        mMapView.addLayer(tiledLayer);
        mMapView.setMaxScale(5000);
        //mMapView.addLayer(new ArcGISTiledMapServiceLayer("http://cache1.arcgisonline.cn/ArcGIS/rest/services/ChinaOnlineStreetColor/MapServer"));
        //mMapView.addLayer(new ArcGISTiledMapServiceLayer("http://services.arcgisonline.com/ArcGIS/rest/services/ESRI_StreetMap_World_2D/MapServer"));
        mMapView.setMapBackground(0xEFF4F2,0xEFF4F2,0,0);

        //region 设置SlidingMenu 不替换
        SlidingMenu menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        menu.setShadowWidth(15);
        menu.setShadowDrawable(R.drawable.shadow_slide_menu);
        //menu_normal.setBehindOffset(160);
        menu.setBehindWidth(300);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        //endregion

        //region 替换SlidingMenu
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //fragmentTransaction.replace(R.id.fragment_menu, fragmentMenuNormal).commit();
        fragmentTransaction.add(R.id.fragment_menu, fragmentMenuPro);
        fragmentTransaction.add(R.id.fragment_menu, fragmentMenuNormal).commit();
        fragmentTransaction.hide(fragmentMenuPro);
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right,R.anim.slide_in_right);
        View menuContainer = LayoutInflater.from(getBaseContext()).inflate(R.layout.menu_container,null);
        ((Switch)menuContainer.findViewById(R.id.switchMenuProNormal)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!b) {
                    //普通
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                    fragmentTransaction.hide(fragmentMenuPro);
                    fragmentTransaction.show(fragmentMenuNormal).commit();
                } else {
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                    fragmentTransaction.hide(fragmentMenuNormal);
                    fragmentTransaction.show(fragmentMenuPro).commit();
                }
            }
        });
        menu.setMenu(menuContainer);
        //endregion

        getSupportActionBar().setHomeButtonEnabled(true);

        //设置定位器
        ldm = mMapView.getLocationDisplayManager();
        //ldm.setLocationListener(locationListener);

        initializeRoutingAndGeocoding();

        touchListener = new TouchListener(ActivityMain.this,this,mMapView);
        mMapView.setOnTouchListener(touchListener);

        //region 加载矢量底图
        (new Runnable() {
            @Override
            public void run() {
                try {
                    Geodatabase geodatabase = new Geodatabase(extern + networkPath);
                    for (GeodatabaseFeatureTable gdbFeatureTable : geodatabase.getGeodatabaseTables()) {
                        if (gdbFeatureTable.hasGeometry()) {
                            final FeatureLayer layer = new FeatureLayer(gdbFeatureTable);
                            layer.setEnableLabels(true);
                            layer.setSelectionColor(Color.YELLOW);
                            mMapView.addLayer(layer);
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).run();
        //endregion

        mMapView.addLayer(mGraphicsLayer);

        slidingUpPanel = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        slidingUpPanel.setEnableDragViewTouchEvents(true);

        LayoutInflater layoutInflater = getLayoutInflater().from(this);
        View view1 = layoutInflater.inflate(R.layout.page1, null);
        View view2 = layoutInflater.inflate(R.layout.page1, null);
        View view3 = layoutInflater.inflate(R.layout.page1,null);
        final List<View> viewList = new ArrayList<View>();
        viewList.add(view1);
        viewList.add(view2);
        viewList.add(view3);
        ViewPager viewPager = (ViewPager)findViewById(R.id.viewPager);
        PagerAdapter pagerAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return viewList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object o) {
                return view == o;
            }

            @Override
            public Object instantiateItem(ViewGroup viewGroup, int position) {
                ((ViewPager)viewGroup).addView(viewList.get(position));
                return viewList.get(position);
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                ((ViewPager)container).removeView(viewList.get(position));
            }

        };
        viewPager.setAdapter(pagerAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_main, menu);
        SearchView searchView = new SearchView(getSupportActionBar().getThemedContext());
        searchView.setQueryHint("输入地名");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                /*
                LocatorFindParameters findParams = new LocatorFindParameters(query);
                findParams.setMaxLocations(10);
                findParams.setOutSR(mMapView.getSpatialReference());
                searchTask.Search(findParams);
                ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                */

                for (Layer layer : mMapView.getLayers()) {
                    if (layer instanceof FeatureLayer) {
                        ((FeatureLayer) layer).setSelectionColor(Color.CYAN);
                        QueryParameters queryParameters = new QueryParameters();
                        queryParameters.setWhere("NAME like '%" + query + "%'");
                        ((FeatureLayer) layer).selectFeatures(queryParameters, ArcGISFeatureLayer.SELECTION_METHOD.NEW,null);
                    }
                }


                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        MenuItem menuItemSearch = menu.findItem(R.id.menu_search);
        menuItemSearch.setActionView(searchView);

        MenuItem menuItem = menu.add("Add");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                (new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Geodatabase geodatabase = new Geodatabase(extern + networkPath);
                            for (GeodatabaseFeatureTable gdbFeatureTable : geodatabase.getGeodatabaseTables()) {
                                if (gdbFeatureTable.hasGeometry()) {
                                    final FeatureLayer layer = new FeatureLayer(gdbFeatureTable);
                                    mMapView.addLayer(layer);
                                    LinearLayout layerControlContentor =  (LinearLayout)fragmentMenuNormal.getView().findViewById(R.id.layerControlContentor);
                                    CheckBox checkBox = new CheckBox(layerControlContentor.getContext());
                                    checkBox.setText(gdbFeatureTable.getTableName());
                                    checkBox.setChecked(true);
                                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                        @Override
                                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                            layer.setVisible(b);
                                        }
                                    });
                                    layerControlContentor.addView(checkBox);
                                }
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }).run();
                return false;
            }
        });


        menuItem = menu.add("拍照(系统)");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                //File picture = new File(Environment.getExternalStorageDirectory().getPath() + "/" + timeStamp + ".jpg");
                File picture = new File(Environment.getExternalStorageDirectory().getPath() + "/" +  "Hello.jpg");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(picture));
                //ActivityMain.this.startActivity(intent);
                startActivityForResult(intent,1);
                return false;
            }
        });


        menuItem = menu.add("ShowPanel");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                slidingUpPanel.showPane();
                slidingUpPanel.setPanelHeight(100);
                slidingUpPanel.setAnchorPoint((float)0.5);
                //slidingUpPanel.expandPane();
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
            Log.e("CameraE","SavePicError");
            return;
        }
        uploadFile("http://10.11.204.71/index.php", picture);
    }

    private void uploadFile(String serverUrl,File pic){
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";
        String srcPath = pic.getAbsolutePath();
        try
        {
            URL url = new URL(serverUrl);
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
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream( httpURLConnection.getOutputStream());
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
            String result = br.readLine();

            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
            dos.close();
            is.close();
        } catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void PostText(String RequestURL, File file) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", "username");
        params.put("age", "password");
        String encode = "utf-8";

        byte[] data = getRequestData(params, encode).toString().getBytes();
        try {
            URL url = new URL(RequestURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(3000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setUseCaches(false);
            //设置请求体的类型是文本类型
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //设置请求体的长度
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            //获得输出流，向服务器写入数据
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(data);

            int response = httpURLConnection.getResponseCode();            //获得服务器的响应码
            if(response == HttpURLConnection.HTTP_OK) {
                InputStream inptStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inptStream));

                StringBuilder stringBuilder = new StringBuilder();
                String line = null;
                while ((line = bufferedReader.readLine()) != null){
                    stringBuilder.append(line);
                }

                TextView textView = new TextView(this);
                textView.setText(Html.fromHtml(stringBuilder.toString()));
                Dialog dialog = new Dialog(this);
                dialog.setContentView(textView);
                dialog.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
      /*
      TextView textView = new TextView(this);
      textView.setText(result);
      Dialog dialog = new Dialog(this);
      dialog.setContentView(textView);
      dialog.show();
      */
    }

    public static StringBuffer getRequestData(Map<String, String> params, String encode) {
        StringBuffer stringBuffer = new StringBuffer();        //存储封装好的请求体信息
        try {
            for(Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), encode))
                        .append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);    //删除最后的一个"&"
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
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
        (new Runnable() {
            @Override
            public void run() {
                LinearLayout layerControlContentor =  (LinearLayout)fragmentMenuNormal.getView().findViewById(R.id.layerControlContentor);
                layerControlContentor.removeAllViews();
                for (final Layer layer : mMapView.getLayers()) {
                    try {
                        if (layer.getName().length()<1)
                            return;
                        CheckBox checkBox = new CheckBox(layerControlContentor.getContext());
                        checkBox.setText(layer.getName());
                        checkBox.setChecked(layer.isVisible());
                        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                layer.setVisible(b);
                            }
                        });
                        layerControlContentor.addView(checkBox);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).run();

        super.onResume();
        mMapView.unpause();
    }
    //endregion
    public void showCallout(String text, Point location) {

        if (mCallout == null) {
            LayoutInflater inflater = (LayoutInflater) getApplication().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mCallout = inflater.inflate(R.layout.callout, null);
        }

        // Show the callout with the given text at the given location
        ((TextView) mCallout.findViewById(R.id.calloutText)).setText(text);
        ((Button)mCallout.findViewById(R.id.btnAddStop)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //routingTask.AddPoint(new StopGraphic(mMapView.getCallout().getCoordinates()));
            }
        });
        mMapView.getCallout().show(location, mCallout);
        mMapView.getCallout().setMaxWidth(700);
        mMapView.getCallout().setMaxHeight(900);
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
                    searchTask = new SearchTask(ActivityMain.this);
                    //mLocator = Locator.createLocalLocator(extern + locatorPath);
                    //mRouteTask = RouteTask.createLocalRouteTask(extern + networkPath, networkName);
                    routingTask = new RoutingTask(ActivityMain.this,extern + networkPath,networkName);
                } catch (Exception e) {
                    popToast("加载离线数据失败" + e.getMessage(), true);
                    e.printStackTrace();
                }
            }
        }).start();
    }

}