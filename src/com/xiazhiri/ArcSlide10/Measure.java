package com.xiazhiri.ArcSlide10;

import android.graphics.Color;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.TextSymbol;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Measure {

    ActivityMain activityMain;
    MapView mMapView;
    GraphicsLayer mGraphicsLayer;
    Polyline polyline;
    List<Point> points;
    int mtype;
    //1 line; 2 polygon; 3 round;

    public Measure(ActivityMain activityMain, GraphicsLayer graphicsLayer,int type) {
        mMapView = activityMain.mMapView;
        this.activityMain = activityMain;
        mGraphicsLayer = graphicsLayer;
        polyline = new Polyline();
        points = new ArrayList<Point>();
        mtype = type;
        this.Reset();
    }

    public void AddPoint(Point mapPoint) {
        points.add(mapPoint);
        Graphic graphic = new Graphic(mapPoint, new PictureMarkerSymbol(activityMain.getResources().getDrawable(R.drawable.bubble_point_blue_big)),3);
        mGraphicsLayer.addGraphic(graphic);

        switch (mtype) {
            //start line
            case 1:
                if (points.indexOf(mapPoint) == 0)
                    polyline.startPath(mapPoint);
                else {
                    polyline.lineTo(mapPoint);
                    Graphic lineGraphic = new Graphic(polyline, new SimpleLineSymbol(Color.rgb(45,134,255), 2, SimpleLineSymbol.STYLE.SOLID),2);
                    mGraphicsLayer.addGraphic(lineGraphic);
                    //距离
                    String lineLongthTxt;
                    double lineLongth = polyline.calculateLength2D();
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(1);
                    lineLongthTxt = (lineLongth > 1000 ? df.format(lineLongth / 1000) + "Km" : (int)lineLongth + "m");
                    Graphic txtGraphic = new Graphic(mapPoint, new TextSymbol(12, lineLongthTxt, Color.RED));
                    mGraphicsLayer.addGraphic(txtGraphic);
                }
                break;

            case 2:
                if (points.size() >= 3) {
                    mGraphicsLayer.removeAll();
                    Polygon polygon = new Polygon();
                    polygon.startPath(points.get(0));

                    //起点
                    graphic = new Graphic(points.get(0), new PictureMarkerSymbol(activityMain.getResources().getDrawable(R.drawable.bubble_point_blue_big)));
                    mGraphicsLayer.addGraphic(graphic);
                    //中间点
                    for (int i = 1; i < points.size()-1; i++) {
                        graphic = new Graphic(points.get(i), new PictureMarkerSymbol(activityMain.getResources().getDrawable(R.drawable.bubble_point_blue_big)));
                        mGraphicsLayer.addGraphic(graphic);
                        polygon.lineTo(points.get(i));
                    }
                    //终点
                    polygon.lineTo(points.get(points.size()-1));
                    graphic = new Graphic(points.get(points.size()-1), new PictureMarkerSymbol(activityMain.getResources().getDrawable(R.drawable.bubble_point_red_big)));
                    mGraphicsLayer.addGraphic(graphic);
                    //终点到起点闭合
                    polygon.lineTo(points.get(0));
                    //线
                    Graphic polygonGraphic = new Graphic(polygon, (new SimpleFillSymbol(Color.BLUE)).setAlpha(30));
                    mGraphicsLayer.addGraphic(polygonGraphic);

                    //面积
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(1);
                    String polygonAreaTxt = df.format(polygon.calculateArea2D());
                    Graphic txtGraphic = new Graphic(mapPoint, new TextSymbol(12, polygonAreaTxt, Color.RED));
                    mGraphicsLayer.addGraphic(txtGraphic);
                }
                break;
            default:
                break;
        }
    }

    public void Reset(){
        polyline = new Polyline();
        points = new ArrayList<Point>();
        mGraphicsLayer.removeAll();
    }
}
