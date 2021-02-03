package com.cityhopper.book.dialog;

import android.util.Log;

import com.cityhopper.book.pojo.GooglePlaces;

import java.util.ArrayList;

public class Helper {
    public static ArrayList<GooglePlaces> mHelperList;

    public static ArrayList<GooglePlaces> getmHelperList() {
        return mHelperList;
    }

    public static void setmHelperList(ArrayList<GooglePlaces> mHelperList) {
        Helper.mHelperList = mHelperList;
    }

    public static boolean checkIfItemExists(String placid){
        boolean exists = false;
        for(GooglePlaces googlePlaces : mHelperList){
            Log.d("PLACE_ID", placid +" - "+ googlePlaces.getgPlaceId());
            if(googlePlaces.getgPlaceId().compareTo(placid) == 0){
                exists = true;
                break;
            }
        }
        return exists;
    }
}
