package com.cityhopper.book.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.Window;

import com.cityhopper.book.R;
import com.cityhopper.book.pojo.GooglePlaces;
import com.cityhopper.book.pojo.UserProfile;

import java.util.ArrayList;

public class Utility {

    private static String SearchQueryPart1 = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=####&location=###&radius=##&key=AIzaSyB_wJo8c8VQVH_lI7kOAGgjgaYUHX-_23U";
    private static String SearchQueryPart2 = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=####&location=###&radius=##&type=#&key=AIzaSyB_wJo8c8VQVH_lI7kOAGgjgaYUHX-_23U";
    private static String GPhotoRequest = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=###&photoreference=##&key=AIzaSyB_wJo8c8VQVH_lI7kOAGgjgaYUHX-_23U";
    private static String PDetailsRequest = "https://maps.googleapis.com/maps/api/place/details/json?placeid=##&fields=name,rating,reviews,opening_hours&key=AIzaSyB_wJo8c8VQVH_lI7kOAGgjgaYUHX-_23U";
    private static String textSearchURL = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=###&location=##&radius=#&key=AIzaSyB_wJo8c8VQVH_lI7kOAGgjgaYUHX-_23U";
    private static String textNextPage = "https://maps.googleapis.com/maps/api/place/textsearch/json?key=AIzaSyB_wJo8c8VQVH_lI7kOAGgjgaYUHX-_23U&&pagetoken=###";
    private static String PDetailsRequestALL = "https://maps.googleapis.com/maps/api/place/details/json?placeid=##&fields=name,rating,formatted_address,icon,type,formatted_phone_number,opening_hours,website&key=AIzaSyB_wJo8c8VQVH_lI7kOAGgjgaYUHX-_23U";

    private static UserProfile userProfile;
    private static GooglePlaces mGooglePlaces;

    //This is Used Global Selected Place List;
    public static ArrayList<GooglePlaces> mSelectedPlaces = new ArrayList<GooglePlaces>();
    //This List Hold the removed Item from the List;
    public static ArrayList<GooglePlaces> mRemovedPlaces = new ArrayList<GooglePlaces>();

    public static ArrayList<GooglePlaces> mUpdatedPlaces = new ArrayList<GooglePlaces>();


    public static GooglePlaces getmGooglePlaces() {
        return mGooglePlaces;
    }

    public static void setmGooglePlaces(GooglePlaces mGooglePlaces) {
        Utility.mGooglePlaces = mGooglePlaces;
    }

    public static UserProfile getUserProfile() {
        return userProfile;
    }

    public static void setUserProfile(UserProfile userProfile) {
        Utility.userProfile = userProfile;
    }

    public static boolean checkConnection(String errorString) {
        boolean error = false;
        if (errorString.contains("unable to resolve host") || errorString.contains("failed to connect") || errorString.contains("network is unreachable")
                || errorString.contains("software caused connection abort") || errorString.contains("connection timed out") || errorString.contains("No address associated with hostname")) {
            error = true;
        }
        return error;
    }

    public static Pair<String, String> GetErrorMessage(String errorString) {
        Pair<String, String> pair = new Pair<>("Something went Wrong","Can't find anything for you");

        if (errorString.contains("Unable to resolve host")) {

            pair = new Pair<>("Unable to Connect!", "Check your Internet Connection,Unable to connect the Server");

        } else if (errorString.contains("Failed to connect")) {

            pair = new Pair<>("Connection timed out", "Check your Internet Connection");

        } else if (errorString.contains("Network is unreachable")) {

            pair = new Pair<>("Network unreachable", "Could not connect to Internet, Check your mobile/wifi Connection");

        } else if (errorString.contains("Software caused connection abort")) {

            pair = new Pair<>("Connection Aborted", "Connection was aborted by server, without any response");

        } else if (errorString.contains("Connection timed out")) {

            pair = new Pair<>("Connection timed out", "Could not connect server, check internet connection");

        } else if (errorString.contains("No address associated with hostname")) {

            pair = new Pair<>("Unable to Connect!", "Check your Internet Connection,Unable to connect the Server");

        }
        return pair;
    }

    public static void ShowAlertDialog(final Context context, String title, String message, final boolean isFinish) {
        AlertDialog.Builder aDialogBuilder = new AlertDialog.Builder(context);
        aDialogBuilder.setTitle(title);
        aDialogBuilder.setMessage(message);
        aDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (isFinish)
                    ((Activity) context).finish();
            }
        });
        AlertDialog alertDialog = aDialogBuilder.create();
        alertDialog.show();
    }

    public static String GenerateQuery(@NonNull String Query, @NonNull Location location, @NonNull float radius) {
        String query = SearchQueryPart1.replace("####", Query);
        query = query.replace("###", location.getLatitude() + "," + location.getLongitude());
        query = query.replace("##", radius + "");
        return query;
    }

    public static String GenerateTypeQuery(@NonNull String Query, @NonNull Location location, @NonNull float radius, @NonNull String bar) {
        String query = SearchQueryPart2.replace("####", Query);
        query = query.replace("###", location.getLatitude() + "," + location.getLongitude());
        query = query.replace("##", radius + "");
        query = query.replace("#", bar);
        return query;
    }

    public static String GeneratePhotoQuery(@NonNull String maxHeightWidth, @NonNull String GPhotoReference) {
        String query = GPhotoRequest.replace("###", maxHeightWidth);
        query = query.replace("##", GPhotoReference);
        return query;
    }

    public static String GenerateDetailsQuery(@NonNull String place_Id) {
        String query = PDetailsRequest.replace("##", place_Id);
        return query;
    }

    public static String GenerateDetailsQueryAll(@NonNull String place_Id) {
        String query = PDetailsRequestALL.replace("##", place_Id);
        return query;
    }

    public static String GenerateTextSearchQuery(@NonNull String Query, @NonNull Location location, @NonNull float radius) {
        String query = textSearchURL.replace("###", Query);
        query = query.replace("##", location.getLatitude() + "," + location.getLongitude());
        query = query.replace("#", radius + "");
        return query;
    }

    public static String GetNextPageUrl(@NonNull String nextPageToken){
        return textNextPage.replace("###", nextPageToken);
    }

    public static boolean CheckPlaceID(@NonNull String place_id, @NonNull ArrayList<GooglePlaces> placesArrayList) {
        boolean check = true;
        for(GooglePlaces googlePlaces : placesArrayList){
            if(googlePlaces.getgPlaceId().compareTo(place_id) == 0){
                check = false;
                break;
            }
        }
        return check;
    }

    public static AlertDialog.Builder getAlertBuilder(@NonNull Context context, @NonNull String title, @NonNull String message, boolean cancelable){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(cancelable);
        builder.setTitle(title);
        builder.setMessage(message);
        return builder;
    }

    public static void blurBitmapWithRenderscript(
            RenderScript rs, Bitmap bitmap2) {
        // this will blur the bitmapOriginal with a radius of 25
        // and save it in bitmapOriginal
        // use this constructor for best performance, because it uses
        // USAGE_SHARED mode which reuses memory
        final Allocation input =
                Allocation.createFromBitmap(rs, bitmap2);
        final Allocation output = Allocation.createTyped(rs,
                input.getType());
        final ScriptIntrinsicBlur script =
                ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
//        final ScriptIntrinsicBlur script =
//                ScriptIntrinsicBlur.create(rs, Element.U16_4(rs));
        // must be >0 and <= 25
        script.setRadius(25f);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(bitmap2);
    }


    public static Dialog GetLoadingDialog (@NonNull Context cContext){
        Dialog dialog = new Dialog(cContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.loading_dialog);
        dialog.setCancelable(false);
        return dialog;
    }

    private static Location mLocation;

    public static Location getmLocation() {
        return mLocation;
    }

    public static void setmLocation(Location mLocation) {
        Utility.mLocation = mLocation;
    }


    public static ArrayList<ArrayList<GooglePlaces>> mainCollection;
    public static ArrayList<String> dayNo;

    public static ArrayList<ArrayList<GooglePlaces>> getMainCollection() {
        return mainCollection;
    }

    public static void setMainCollection(ArrayList<ArrayList<GooglePlaces>> collection) {
        mainCollection = new ArrayList<ArrayList<GooglePlaces>>();
        mainCollection = collection;
    }

    public static ArrayList<String> getDayNo() {
        return dayNo;
    }

    public static void setDayNo(ArrayList<String> dayno) {
        dayNo = new ArrayList<String>();
        dayNo = dayno;
    }

    private static String NextPageToken="";

    public static String getNextPageToken() {
        return NextPageToken;
    }

    public static void setNextPageToken(String nextPageToken) {
        NextPageToken = nextPageToken;
    }
}
