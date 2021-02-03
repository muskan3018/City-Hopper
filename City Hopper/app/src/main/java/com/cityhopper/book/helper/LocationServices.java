package com.cityhopper.book.helper;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;



public class LocationServices extends LocationCallback{

    private static final long UPDATE_INTERVAL = 10 * 1000;
    private static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 3;

    private LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;
    private Context context;


    @SuppressLint("MissingPermission")
    public LocationServices(Context con) {
        context = con;

        Utility.setmLocation(new Location(""));

        mFusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context);

        createLocationRequest();

    }


    @SuppressLint("MissingPermission")
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);

        mFusedLocationClient.requestLocationUpdates(mLocationRequest,getPendingIntent());
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(context, LocationBroadcast.class);
        intent.setAction(LocationBroadcast.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void stopLocationRequest(){
        mFusedLocationClient.removeLocationUpdates(getPendingIntent());
    }
}
