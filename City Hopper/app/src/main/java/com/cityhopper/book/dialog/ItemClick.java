package com.cityhopper.book.dialog;

import android.view.View;

import com.cityhopper.book.pojo.GooglePlaces;

import java.util.ArrayList;

public interface ItemClick {
    void onDialogSubmit(View v, ArrayList<GooglePlaces> mPlaces, int hoursPerDay);
}
