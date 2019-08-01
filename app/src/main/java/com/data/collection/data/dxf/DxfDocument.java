package com.data.collection.data.dxf;


import android.graphics.Color;
import android.os.AsyncTask;

import com.data.collection.listener.IListenerDxfReady;
import com.data.collection.util.ColorUtils;
import com.data.collection.util.LsLog;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PolylineBuilder;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

import org.kabeja.dxf.Bounds;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFHeader;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DxfDocument {

    private static final String TAG = "DxfDocument";
    private static final String EXTMIN = "$EXTMIN";
    private static final String EXTMAX = "$EXTMAX";

    DXFDocument doc;
    Parser parser;
    DXFHeader dxfHeader;
    public DxfDocument (String filename) {
        parser = ParserBuilder.createDefaultParser();
        try {
            FileInputStream fileInputStream = new FileInputStream(filename);
            parser.parse(filename);
            doc = parser.getDocument();
            dxfHeader = doc.getDXFHeader();
            Iterator varialbeIterator = dxfHeader.getVarialbeIterator();
            LsLog.w(TAG, "varialbeIterator = " + varialbeIterator) ;

        }  catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    public Bounds getBounds() {
        LsLog.w(TAG, "getBounds = " + doc.getBounds().toString());
        return doc.getBounds();
    }

    public List<DXFLWPolyline> getENTITIES (){
        LsLog.w(TAG, "getENTITIES = ");
        List<DXFLWPolyline> entities = new ArrayList<>();
        Iterator dxfLayerIterator = doc.getDXFLayerIterator();
        while (dxfLayerIterator != null && dxfLayerIterator.hasNext()) {
            DXFLayer dxfLayer = (DXFLayer)dxfLayerIterator.next();
            List<DXFLWPolyline> dxfEntities = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE);
            if (dxfEntities != null && dxfEntities.size() > 0) {
                entities.addAll(dxfEntities);
            }
        }
        return entities;
    }

    List<GraphicsOverlay> graphicsOverlays = new ArrayList<>();
    public void getGraphicsOverlay(IListenerDxfReady listenerDxfReady){
        List<DXFLWPolyline> entities = getENTITIES();
        getGraphicsOverlay(entities, listenerDxfReady);
    }

    public void getGraphicsOverlay(List<DXFLWPolyline> dxfEntities, IListenerDxfReady listenerDxfReady) {
        // Params, Progress, Result
        if (dxfEntities != null && dxfEntities.size() > 0) {
            new AsyncTask<List<DXFLWPolyline>, String, List<GraphicsOverlay>>() {
                @Override
                protected List<GraphicsOverlay> doInBackground(List<DXFLWPolyline>... lists) {
                    if (lists.length > 0) {
                        List<DXFLWPolyline> lines = lists[0];
                        for (DXFLWPolyline line: lines) {
                            graphicsOverlays.add(genaricLineGraphic(line));
                        }
                    }
                    return graphicsOverlays;
                }

                @Override
                protected void onPostExecute(List<GraphicsOverlay> graphicsOverlays) {
                    if (listenerDxfReady != null) listenerDxfReady.onReady(graphicsOverlays);
                }
            }.execute(dxfEntities);
        }
    }

    private GraphicsOverlay genaricLineGraphic( DXFLWPolyline line) {
        LsLog.w(TAG, "genaricLineGraphic ... ");
        // line graphic
        GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
        PolylineBuilder lineGeometry = new PolylineBuilder(SpatialReferences.getWebMercator());

        int color = Color.GREEN;
        byte[] colorRGB = line.getColorRGB();
        if (colorRGB != null) {
            int r = colorRGB[0];
            int g = colorRGB[1];
            int b = colorRGB[2];
            color = Color.argb(255, r,g,b);
        } else {
            int color1 = line.getColor();
            color = ColorUtils.IndexToRGB(color1);
        }

        int lineWeight = line.getLineWeight();
        lineWeight = lineWeight / 20;
        if (lineWeight == 0) lineWeight = 1;

        LsLog.w(TAG, "lineWeight = " + lineWeight);

        SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, color, lineWeight);
        // create graphic for polyline

        SimpleRenderer lineRenderer = new SimpleRenderer(lineSymbol);
        graphicsOverlay.setRenderer(lineRenderer);
        int vertexCount = line.getVertexCount();// 顶点数量

        for (int i = 0; i< vertexCount; i++) {
            DXFVertex vertex = line.getVertex(i);
            org.kabeja.dxf.helpers.Point point1 = vertex.getPoint();
            Point point =  new Point(point1.getX(),point1.getY(),SpatialReferences.getWebMercator());
            lineGeometry.addPoint(point);
        }
        Graphic lineGraphic = new Graphic(lineGeometry.toGeometry());
        graphicsOverlay.getGraphics().add(lineGraphic);
        return graphicsOverlay;
    }

}
