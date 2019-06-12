package com.data.zwnavi;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.struct.BNLocationData;

import java.util.List;

/**
 * 系统GPS回调
 * App 中初始化，然后调用startLocation 开始定位。 在应用中就能够使用；
 * 结束程序前，先调用stopLocation
 * @author yangchao on 2018/9/28.
 */
public class LocationController implements LocationListener {

    private LocationManager mLocationManager;

    private static LocationController sInstance;

    private BNLocationData mLocData;

    private Location mLocation;

    private boolean isLocating;

    private LocationController() {
    }

    public static synchronized LocationController getInstance() {
        if (sInstance == null) {
            sInstance = new LocationController();
        }

        return sInstance;
    }

    public boolean isLocating() {
        return isLocating;
    }

    public Location getLocation(){
        return mLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            return;
        }
        mLocation = location;
        Log.e("onLocationChanged: ", location.toString());
        mLocData = new BNLocationData.Builder()
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .accuracy(location.getAccuracy())
                .speed(location.getSpeed())
                .direction(location.getBearing())
                .altitude((int) location.getAltitude())
                .time(location.getTime())
                .build();
        BaiduNaviManagerFactory.getMapManager().setMyLocationData(mLocData);
    }

    public void startLocation(Context ctx) {
        if (isLocating) {
            return;
        }
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) ctx.getSystemService(Context
                    .LOCATION_SERVICE);
        }
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ctx,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("LocationDemo", "initLocationClient: permission failed");
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000, 1000, this);
        mLocation = getLastKnownLocation(ctx);
        isLocating = true;
    }


    private Location getLastKnownLocation(Context ctx) {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) ctx.getApplicationContext().getSystemService(Context
                    .LOCATION_SERVICE);
        }

        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;

        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(ctx,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(ctx,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return bestLocation;
            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }




    public void stopLocation() {
        mLocationManager.removeUpdates(this);
        isLocating = false;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
