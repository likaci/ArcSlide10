package com.xiazhiri.ArcSlide10;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.R.string;
import android.graphics.Color;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Line;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;
import com.esri.core.symbol.TextSymbol;

public class Measure {

    MapView mMapView;
    GraphicsLayer mGraphicsLayer;
    Polyline polyline;
    List<Point> points;
    int mtype;
    //1 line; 2 polygon; 3 round;

    public Measure(MapView mapView, GraphicsLayer graphicsLayer,int type) {
        mMapView = mapView;
        mGraphicsLayer = graphicsLayer;
        polyline = new Polyline();
        points = new ArrayList<Point>();
        mtype = type;
        this.Reset();
    }

    public void AddPoint(Point mapPoint) {
        points.add(mapPoint);
        Graphic graphic = new Graphic(mapPoint, new SimpleMarkerSymbol(Color.RED, 7, STYLE.CIRCLE));
        mGraphicsLayer.addGraphic(graphic);

        switch (mtype) {
            //start line
            case 1:
                if (points.indexOf(mapPoint) == 0)
                    polyline.startPath(mapPoint);
                else {
                    polyline.lineTo(mapPoint);
                    Graphic lineGraphic = new Graphic(polyline, new SimpleLineSymbol(Color.BLUE, 2, SimpleLineSymbol.STYLE.SOLID));
                    mGraphicsLayer.addGraphic(lineGraphic);

                    String lineLongthTxt;
                    double lineLongth = polyline.calculateLength2D();
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(1);
                    if (lineLongth > 1000)
                        lineLongthTxt = df.format(lineLongth / 1000) + "Km";
                    else
                        lineLongthTxt = (int) lineLongth + "m";
                    Graphic txtGraphic = new Graphic(mapPoint, new TextSymbol(12, lineLongthTxt, Color.RED));
                    mGraphicsLayer.addGraphic(txtGraphic);
                }
                break;
            case 2:
                if (points.size() >= 3) {
                    mGraphicsLayer.removeAll();
                    Polygon polygon = new Polygon();
                    polygon.startPath(points.get(0));
                    for (int i = 1; i < points.size(); i++) {
                        graphic = new Graphic(points.get(i), new SimpleMarkerSymbol(Color.RED, 7, STYLE.CIRCLE));
                        mGraphicsLayer.addGraphic(graphic);
                        polygon.lineTo(points.get(i));
                    }
                    polygon.lineTo(points.get(0));
                    Graphic polygonGraphic = new Graphic(polygon, (new SimpleFillSymbol(Color.BLUE)).setAlpha(30));
                    mGraphicsLayer.addGraphic(polygonGraphic);

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
