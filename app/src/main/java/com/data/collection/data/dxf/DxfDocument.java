package com.data.collection.data.dxf;


import com.data.collection.util.LsLog;
import com.data.collection.util.Utils;
import com.google.gson.Gson;

import org.kabeja.dxf.Bounds;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFExtrusion;
import org.kabeja.dxf.DXFHeader;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFVariable;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.dxf.objects.DXFDictionary;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DxfDocument {

    private static final String TAG = "DxfDocument";
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
        Iterator varialbeIterator = dxfHeader.getVarialbeIterator();
        while (varialbeIterator!= null && varialbeIterator.hasNext()) {
            DXFVariable value = (DXFVariable)varialbeIterator.next();

            Iterator valueKeyIterator = value.getValueKeyIterator();
            StringBuffer sb = new StringBuffer();
            while (valueKeyIterator != null && valueKeyIterator.hasNext()) {
                String key = (String)valueKeyIterator.next();

                sb.append(key + " -> " + value.getValue(key) + ", ");
            }
            LsLog.w(TAG, "key = " + value.getName() + ", value = " + sb.toString());
        }
        //$EXTMIN   $EXTMAX
        return doc.getBounds();
    }

    public List<DXFLWPolyline> getENTITIES (){
        Iterator dxfBlockIterator = doc.getDXFBlockIterator();
        DXFDictionary rootDXFDictionary = doc.getRootDXFDictionary();
        LsLog.w(TAG, "rootDXFDictionary = ");
        List<DXFLWPolyline> entities = new ArrayList<>();

        Iterator dxfLayerIterator = doc.getDXFLayerIterator();
        while (dxfLayerIterator != null && dxfLayerIterator.hasNext()) {
            DXFLayer dxfLayer = (DXFLayer)dxfLayerIterator.next();
            List<DXFLWPolyline> dxfEntities = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE);
            if (dxfEntities != null && dxfEntities.size() > 0) {
                entities.addAll(dxfEntities);
                for (DXFLWPolyline line: dxfEntities) {
                    int vertexCount = line.getVertexCount();// 顶点数量
                    DXFVertex start = line.getVertex(0);
                    DXFVertex end = line.getVertex(vertexCount - 1);
                    int color = line.getColor();
                    byte[] colorRGB = line.getColorRGB();
                    String id = line.getID();
                    String type = line.getType();
                    StringBuffer sb = new StringBuffer();
                    int lineWeight = line.getLineWeight();
                    double contstantWidth = line.getContstantWidth();
                    sb.append("getENTITIES line : color =  ").append(color)
                            .append(", id = ").append(id)
                            .append(", start end = ").append(start.getPoint().toString())
                            .append(",").append(end.getPoint().toString())
                            .append(", type = " + type)
                            .append(", rgb = ").append(Utils.bytes2hex(colorRGB))
                            .append(", lineWeight = ").append(lineWeight);

                    LsLog.w(TAG, "dxfEntities = " + sb.toString());
                }
            }
        }
        return entities;

    }
}
