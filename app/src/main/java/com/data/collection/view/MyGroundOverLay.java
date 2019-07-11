package com.data.collection.view;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.GroundOverlay2;

public class MyGroundOverLay extends GroundOverlay2 {
    GeoPoint UL;
    GeoPoint RD;
    GeoPoint unmodifiedUL;
    GeoPoint unmodifiedRD;

    public GeoPoint getUnmodifiedUL() {
        return unmodifiedUL;
    }

    public void setUnmodifiedUL(GeoPoint unmodifiedUL) {
        this.unmodifiedUL = unmodifiedUL;
    }

    public GeoPoint getUnmodifiedRD() {
        return unmodifiedRD;
    }

    public void setUnmodifiedRD(GeoPoint unmodifiedRD) {
        this.unmodifiedRD = unmodifiedRD;
    }

    public void set84Position (GeoPoint UL, GeoPoint RD){
        unmodifiedUL = UL;
        unmodifiedRD = RD;
    }
    /**
     * @param UL upper left
     * @param RD lower right
     */
    @Override
    public void setPosition(GeoPoint UL, GeoPoint RD){
        super.setPosition(UL, RD);
        this.UL = UL;
        this.RD = RD;
    }

    public GeoPoint getUL() {
        return UL;
    }

    public void setUL(GeoPoint UL) {
        this.UL = UL;
    }

    public GeoPoint getRD() {
        return RD;
    }

    public void setRD(GeoPoint RD) {
        this.RD = RD;
    }
}
