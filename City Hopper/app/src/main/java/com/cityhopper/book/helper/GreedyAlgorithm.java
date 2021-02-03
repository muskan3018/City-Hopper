package com.cityhopper.book.helper;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.cityhopper.book.pojo.GooglePlaces;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GreedyAlgorithm {

    private ArrayList<String> Mnames, TempPath, TempNames, fPlaceId;
    private ArrayList<LatLng> MLatlng, TempLatlng;

    private LatLng curLatLng;
    private ArrayList<Float> TempDist;
    private ArrayList<ArrayList<String>> FinalList;

    private ArrayList<GooglePlaces> mLocalGooglePlaces;
    private int totalTime;
    private double totalDistance;

    public GreedyAlgorithm(GooglePlaces[] mPlaces) {
        List<GooglePlaces> places = Arrays.asList(mPlaces);
        this.mLocalGooglePlaces = new ArrayList<GooglePlaces>();
        mLocalGooglePlaces.addAll(places);

        for(GooglePlaces googlePlaces : mLocalGooglePlaces){
            Log.d("SORTING_B", googlePlaces.getgPlaceId()
                    +" - "+googlePlaces.getgPlaceName()+" - "+googlePlaces.getDistanceFromSource());
        }

        mLocalGooglePlaces.add(0, Utility.getmGooglePlaces());

        FinalList = new ArrayList<ArrayList<String>>();
        MLatlng = new ArrayList<LatLng>();

        fPlaceId = new ArrayList<String>();
        fPlaceId.add(mLocalGooglePlaces.get(0).getgPlaceId());

        MakeRoute(mLocalGooglePlaces.get(0).getgPlaceId(), mLocalGooglePlaces.get(0).getgPlaceLocation());
    }

    private void MakeRoute(String name, LatLng latlng) {
        curLatLng = latlng;
        TempDist = new ArrayList<Float>();
        TempPath = new ArrayList<String>();

        Makearray(name);

        checkLocationAvailable();

        SortMainList();

    }

    private void Makearray(String name) {
        TempNames = new ArrayList<String>();
        TempLatlng = new ArrayList<LatLng>();

        for (int i = 0; i < mLocalGooglePlaces.size(); i++) {
            if (mLocalGooglePlaces.get(i).getgPlaceId().compareTo(name) != 0) {
                TempNames.add(mLocalGooglePlaces.get(i).getgPlaceId());
                TempLatlng.add(mLocalGooglePlaces.get(i).getgPlaceLocation());
            }
        }
    }

    private void checkLocationAvailable() {
        Log.d("TSPALGO", "Size = "+TempNames.size());
        if (TempNames.size() == 1) {
            TempDist.add(Float.parseFloat("0"));
            TempPath.add(TempNames.get(0));
            FinalList.add(TempPath);
        } else {
            getNextLocation();
        }
    }

    private void getNextLocation() {
        float dist = 0;
        int fpos = 0;
        for (int i = 0; i < TempNames.size(); i++) {
            float b = getDist(curLatLng, TempLatlng.get(i));
            Log.d("TSPACO",TempNames.get(i)+"-"+b);
            if (i == 0) {
                dist = b;
            }

            if (b < dist) {
                dist = b;
                fpos = i;
            }
        }

        TempDist.add(dist);
        TempPath.add(TempNames.get(fpos));
        fPlaceId.add(TempNames.get(fpos));

        curLatLng = TempLatlng.get(fpos);
        TempNames.remove(fpos);
        TempLatlng.remove(fpos);

        checkLocationAvailable();
    }

    private float getDist(LatLng a, LatLng b) {

        Location l1 = new Location("");
        l1.setLatitude(a.latitude);
        l1.setLongitude(a.longitude);

        Location l2 = new Location("");
        l2.setLatitude(b.latitude);
        l2.setLongitude(b.longitude);

        return l1.distanceTo(l2);
    }

    private void SortMainList() {
        for (int i = 0; i < fPlaceId.size(); i++) {
            Log.d("GREEDY", "Swap : "+i +" - "+ GetPosition(fPlaceId.get(i)));
            Collections.swap(mLocalGooglePlaces, GetPosition(fPlaceId.get(i)), i);
        }

        for(int i = 0; i < mLocalGooglePlaces.size(); i++){
            GooglePlaces places  =  mLocalGooglePlaces.get(i);
            places.setSrno(i);
        }


//        for (int j = 0; j < mLocalGooglePlaces.size(); j++) {
//            double dist = 0.0;
//            GooglePlaces googlePlaces = mLocalGooglePlaces.get(j);
//            if(j > 0)
//                dist = GetDistance(mLocalGooglePlaces.get(j).getgPlaceLocation(), mLocalGooglePlaces.get(j-1).getgPlaceLocation());
//            googlePlaces.setDistanceFromSource(dist);
//            googlePlaces.setTimeToTravel(SelectionActivity.getTimeToTravel(dist));
//            Log.d("GREEDY", "Distance : "+dist+" - "+SelectionActivity.getTimeToTravel(dist));
//            mLocalGooglePlaces.set(j, googlePlaces);
//        }
//
//
//        for (int j = 0; j < mLocalGooglePlaces.size(); j++) {
//            totalTime = totalTime + mLocalGooglePlaces.get(j).getTimeToTravel();
//            totalDistance = totalDistance + mLocalGooglePlaces.get(j).getDistanceFromSource();
//        }

    }

    private int GetPosition(String mGooglePlaceId) {
        int position = -1;
        for (int i = 0; i < mLocalGooglePlaces.size(); i++) {
            if (mLocalGooglePlaces.get(i).getgPlaceId().compareTo(mGooglePlaceId) == 0) {
                position = i;
                break;
            }
        }
        return position;
    }

    public static double GetDistance(LatLng latLng, LatLng latLng1) {
        double miles = 1000;
        Location sLocation = new Location("");
        sLocation.setLongitude(latLng.longitude);
        sLocation.setLatitude(latLng.latitude);


        Location eLocation = new Location("");
        eLocation.setLatitude(latLng1.latitude);
        eLocation.setLongitude(latLng1.longitude);
        Log.d("Distance", ((double) sLocation.distanceTo(eLocation)) + "");
        return ((double) sLocation.distanceTo(eLocation)) / miles;
    }

    public ArrayList<GooglePlaces> getmLocalGooglePlaces() {
        return mLocalGooglePlaces;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public String getTotalDistance() {
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        return decimalFormat.format(totalDistance);
    }
}
