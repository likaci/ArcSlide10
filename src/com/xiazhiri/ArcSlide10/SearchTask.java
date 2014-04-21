package com.xiazhiri.ArcSlide10;

import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import com.esri.core.tasks.query.QueryParameters;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索
 */
public class SearchTask {

    ActivityMain activityMain;
    GraphicsLayer graphicsLayer;
    MapView mapView;
    ArrayList<Feature> features;
    ArrayList<View> views;
    ViewPager viewPager;
    SlidingUpPanelLayout slidingUpPanelLayout;

    public SearchTask(ActivityMain activityMain) {
        this.activityMain = activityMain;
        graphicsLayer = new GraphicsLayer();
        activityMain.mMapView.addLayer(graphicsLayer);
        this.mapView = activityMain.mMapView;
        this.viewPager = activityMain.viewPager;
        this.slidingUpPanelLayout = activityMain.slidingUpPanel;
    }

    //地理编码 搜索按钮
    public void Search(String name) {
        features.clear();
        for (Layer layer : mapView.getLayers()) {
            if (layer instanceof FeatureLayer) {
                ((FeatureLayer) layer).setSelectionColor(Color.CYAN);
                QueryParameters queryParameters = new QueryParameters();
                queryParameters.setWhere("NAME like '%" + name + "%'");
                ((FeatureLayer) layer).selectFeatures(queryParameters, ArcGISFeatureLayer.SELECTION_METHOD.NEW, null);
            }
        }
        if (features.isEmpty())
            return;
    }

    //反地理编码 长按屏幕查询
    public void Search(Point screenPoint) {
        features = new ArrayList<Feature>();
        Point mapPoint = mapView.toMapPoint(screenPoint);
        Map<String, Object> graphicAttribute = new HashMap<String, Object>();
        graphicAttribute.put("NAME", "地图选点");
        Graphic graphic = new Graphic(mapPoint, new SimpleMarkerSymbol(Color.RED, 2, SimpleMarkerSymbol.STYLE.CIRCLE), graphicAttribute);
        features.add(graphic);
        for (Layer layer : activityMain.mMapView.getLayers()) {
            if (layer instanceof FeatureLayer) {
                try {
                    FeatureLayer featureLayer = (FeatureLayer) layer;
                    Feature feature = featureLayer.getFeature(featureLayer.getFeatureIDs((float) screenPoint.getX(), (float) screenPoint.getY(), 5)[0]);
                    features.add(feature);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (features.isEmpty())
            return;
        views = createViews(features);
        showFeature(features.get(0));
        viewPager.setAdapter(new myPagerAdapter());
        viewPager.setOnPageChangeListener(new myOnPageChangeListener());
        slidingUpPanelLayout.setPanelHeight(70);
    }

    class myPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public Object instantiateItem(ViewGroup viewGroup, int position) {
            ((ViewPager) viewGroup).addView(views.get(position));
            return views.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            try {
                ((ViewPager) container).removeView(views.get(position));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    ArrayList<View> createViews(List<Feature> features) {
        ArrayList<View> views = new ArrayList<View>();
        for (Feature feature : features) {
            View view = createView(R.layout.search_info);
            setViewInfo(view, feature);
            views.add(view);
        }
        return views;
    }

    void setViewInfo(View view, Feature feature) {
        ((TextView) view.findViewById(R.id.SearchInfoName)).setText(feature.getAttributes().get("NAME").toString());
        Envelope envelope = new Envelope(); feature.getGeometry().queryEnvelope(envelope);
        ((TextView) (view.findViewById(R.id.SearchInfoCoord))).setText(String.format("%.2f,%.2f", envelope.getCenterX(), envelope.getCenterY()));

        TableLayout tableLayout =  (TableLayout)view.findViewById(R.id.tableProductiveLayout);
        for (String key : feature.getAttributes().keySet()) {
            TableRow tableRow = new TableRow(view.getContext());
            TextView keyTxt = new TextView(view.getContext());
            keyTxt.setText(key);
            TextView valueTxt= new TextView(view.getContext());
            try {
                valueTxt.setText(feature.getAttributes().get(key).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            TextView colonTxt= new TextView(view.getContext());
            colonTxt.setText(":");
            tableRow.addView(keyTxt);
            tableRow.addView(colonTxt);
            tableRow.addView(valueTxt);
            if (valueTxt.getText()!= "")
                tableLayout.addView(tableRow);
            WebView webView = (WebView)view.findViewById(R.id.WebView);
            webView.setWebViewClient(new WebViewClient());
            webView.loadUrl("http://baidu.com");
        }

    }

    View createView(int viewId) {
        LayoutInflater layoutInflater = LayoutInflater.from(this.activityMain.getBaseContext());
        View view = layoutInflater.inflate(viewId, null);
        return view;
    }

    class myOnPageChangeListener implements ViewPager.OnPageChangeListener{
        @Override
        public void onPageScrolled(int i, float v, int i2) {

        }
        @Override
        public void onPageSelected(int i) {
            showFeature(features.get(i));
        }

        @Override
        public void onPageScrollStateChanged(int i) {
        }
    }

    void showFeature(Feature feature) {
        Envelope envelope = new Envelope();
        feature.getGeometry().queryEnvelope(envelope);
        Point point = envelope.getCenter();
        Graphic graphic = new Graphic(envelope.getCenter(), new PictureMarkerSymbol(activityMain.getResources().getDrawable(R.drawable.icon_openmap_mark)),3);
        graphicsLayer.removeAll();
        graphicsLayer.addGraphic(graphic);
        mapView.centerAt(point, true);
        showFeatures(features);
    }

    void showFeatures(ArrayList<Feature> features) {
        for (Feature feature : features) {
            Envelope envelope = new Envelope();
            feature.getGeometry().queryEnvelope(envelope);
            Point point = envelope.getCenter();
            Graphic graphic = new Graphic(envelope.getCenter(), new PictureMarkerSymbol(activityMain.getResources().getDrawable(R.drawable.bubble_point_red_big)),3);
            graphicsLayer.addGraphic(graphic);
        }
    }
}

