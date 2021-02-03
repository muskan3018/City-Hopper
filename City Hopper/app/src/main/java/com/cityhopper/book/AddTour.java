package com.cityhopper.book;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;
import android.view.Window;
import android.widget.Toast;

import com.cityhopper.book.helper.JSONParse;
import com.cityhopper.book.helper.RestAPI;
import com.cityhopper.book.helper.Utility;
import com.cityhopper.book.pojo.GooglePlaces;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class AddTour extends AsyncTask<String, JSONObject, String> {
    private static String TAG = "ADDTOUR";
    private WeakReference<Context> mContext;
    private ArrayList<GooglePlaces> mGooglePlaces;
    private ArrayList<String> mPlaceIds, mPlaceName, mLatLnt, mRating, mDayNo, mSrNo;
    private Dialog mDialog;

    public AddTour(@NonNull Context context, @NonNull ArrayList<GooglePlaces> googlePlaces) {
        this.mContext = new WeakReference<Context>(context);
        this.mGooglePlaces = googlePlaces;
        prepareDialog();
        prepareAllList();
    }

    private void prepareDialog() {
        mDialog = new Dialog(mContext.get());
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.loading_dialog);
        mDialog.setCancelable(false);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog.show();
    }

    @Override
    protected String doInBackground(String... strings) {
        String response = "";
        try {
            RestAPI restAPI = new RestAPI();
            JSONParse jsonParse = new JSONParse();
            //String dest,String type,String nodays,String uid,l dayno,l srno,l pname,l latlng,l rating,l placeid
            JSONObject jsonObject = restAPI.AddSmartPlan(strings[0], strings[1], strings[2], strings[3], mDayNo, mSrNo, mPlaceName, mLatLnt, mRating, mPlaceIds);
            response = jsonParse.Parse(jsonObject);
        } catch (Exception e) {
            response = e.getMessage();
            e.printStackTrace();
        }
        return response;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        mDialog.dismiss();
        Log.d(TAG, s);
        if (Utility.checkConnection(s)) {
            Pair<String, String> pair = Utility.GetErrorMessage(s);
            Utility.ShowAlertDialog(mContext.get(), pair.first, pair.second, false);
        } else {
            try {
                JSONObject json = new JSONObject(s);
                String status = json.getString("status");
                if (status.compareTo("true") == 0) {
                    Toast.makeText(mContext.get(), "Plan Successfully Added", Toast.LENGTH_SHORT).show();
                    Activity activity = (Activity) mContext.get();
                    Intent intent = new Intent(activity, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    activity.startActivity(intent);
                    activity.finish();
                } else {
                    String error = json.getString("Data");
                    Log.d(TAG, error);
                }
            } catch (JSONException e) {
                Log.d(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void prepareAllList() {
        //l dayno,l srno,l pname,l latlng,l rating,l placeid
        mDayNo = new ArrayList<String>();
        mSrNo = new ArrayList<String>();
        mPlaceName = new ArrayList<String>();
        mLatLnt = new ArrayList<String>();
        mRating = new ArrayList<String>();
        mPlaceIds = new ArrayList<String>();
        int i = 0;
        for (GooglePlaces googlePlaces : mGooglePlaces) {
            Log.d(TAG + "_" + i, googlePlaces.getgPlaceId() + " - " + googlePlaces.getgPlaceName()
                    + " - " + googlePlaces.getgPlaceLocation().latitude + "," + googlePlaces.getgPlaceLocation().longitude);
            mDayNo.add(googlePlaces.getDayno() + "");
            mSrNo.add(googlePlaces.getSrno() + "");
            mPlaceName.add(googlePlaces.getgPlaceName());
            mLatLnt.add(googlePlaces.getgPlaceLocation().latitude + "," + googlePlaces.getgPlaceLocation().longitude);
            mRating.add(String.valueOf(googlePlaces.getgPlaceRating()));
            mPlaceIds.add(googlePlaces.getgPlaceId());
            i++;
        }
    }
}
