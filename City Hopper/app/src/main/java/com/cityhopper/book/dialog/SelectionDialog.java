package com.cityhopper.book.dialog;

import android.animation.Animator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.cityhopper.book.R;
import com.cityhopper.book.city.SelectionActivity;
import com.cityhopper.book.helper.MySingleton;
import com.cityhopper.book.helper.UtilConstants;
import com.cityhopper.book.helper.Utility;
import com.cityhopper.book.pojo.GooglePlaces;
import com.cityhopper.book.pojo.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


/**
 * {@link SelectionActivity.SelectionDialog}
 * This Custom Dialog is Used to Create the Selection View
 */

public class SelectionDialog extends Dialog {
    private SharedPreferences sharePref;
    private Context mContext;
    private RelativeLayout rView1, rView2, rView3, rView4, rView5, rView6, rView7, rView8, rView9;
    private LinearLayout grid_Selection;
    private Dialog mDialog;

    private ListView mPlacesList;
    private TextView footerView;
    private Button btnSubmit, btnCancel;
    private String[] selectedPlaces;

    private String[] places;
    private ArrayList<String> sSelction;
    private int[] drawable = new int[]{R.drawable.food, R.drawable.cafe, R.drawable.museum, R.drawable.beach, R.drawable.park, R.drawable.mall,
            R.drawable.park, R.drawable.museum, R.drawable.nightclub};
    private RelativeLayout[] layouts;
    private int selected_position;
    private Location mLocation;
    private int numberOfHours;
    private int isLoopCompete;
    private ArrayList<GooglePlaces> mMainList, mFoodPlaces, mGooglePlaces;
    private AlertDialog alertDialog;
    private int minutes = 0;
    private DecimalFormat decimalFormat = new DecimalFormat("#00.0#");
    private int mSelectedPosition = 0, dayCount;


    private ItemClick itemClick;
    private int loopCount = 0;
    private GooglePlaces msGooglePlaces;

    public SelectionDialog(@NonNull Context context, GooglePlaces googlePlaces, int noHours, int days, Location location) {
        super(context);
        this.mContext = context;
        this.numberOfHours = noHours;
        minutes = numberOfHours * 60;
        this.dayCount = days;
        this.msGooglePlaces = googlePlaces;
        this.mLocation = location;
    }

    public SelectionDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.selection_dialog);
        getWindow().setBackgroundDrawable(new ColorDrawable(mContext.getResources().getColor(android.R.color.transparent)));
        setCancelable(false);

        sharePref = mContext.getSharedPreferences(UtilConstants.SharedPref, Context.MODE_PRIVATE);

        places = mContext.getResources().getStringArray(R.array.Places);

        //Combining this above list
        sSelction = new ArrayList<String>();
        sSelction.add("Food");
        sSelction.addAll(Arrays.asList(places));

        grid_Selection = findViewById(R.id.grid_Selection);
        mPlacesList = findViewById(R.id.sDialogList);

        footerView = findViewById(R.id.footerView);

        rView1 = findViewById(R.id.lView1);
        rView2 = findViewById(R.id.lView2);
        rView3 = findViewById(R.id.lView3);
        rView4 = findViewById(R.id.lView4);

        rView5 = findViewById(R.id.lView5);
        rView6 = findViewById(R.id.lView6);
        rView7 = findViewById(R.id.lView7);
        rView8 = findViewById(R.id.lView8);
        rView9 = findViewById(R.id.lView9);

        btnSubmit = findViewById(R.id.sDialogSubmit);
        btnCancel = findViewById(R.id.sDialogCancel);

        TextView textView = findViewById(R.id.selectionTitle);
        textView.setTextSize(21.0f);
        textView.setText(Html.fromHtml("Plan For Day <b>" + dayCount + "<b>"));

        mDialog = new Dialog(mContext);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.loading_dialog);
        mDialog.setCancelable(false);

        Gson json = new Gson();
        UserProfile userProfile = json.fromJson(sharePref.getString(UtilConstants.USER_PROFILE, ""), UserProfile.class);
        if (userProfile != null) {
            selectedPlaces = userProfile.getPlaces().split(",");
        }

        prepareSelectionView();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
//                    finish();
            }
        });

        mMainList = new ArrayList<GooglePlaces>();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mMainList.size() > 0) {
                    GooglePlaces googlePlaces;
                    if(msGooglePlaces.getgPlaceLocation() != null) {
                        googlePlaces = msGooglePlaces;
                        if (checkReturnTimeAvailable(googlePlaces, false, 0)) {
                            Log.d("PLACES", " Click " + mMainList.size());
                            if (isShowing())
                                cancel();
                            Helper.setmHelperList(new ArrayList<GooglePlaces>(0));
                            itemClick.onDialogSubmit(v, mMainList, minutes / 60);
                        } else {
                            Toast.makeText(mContext, "Time Not Available", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        if (isShowing())
                            cancel();
                        Helper.setmHelperList(new ArrayList<GooglePlaces>(0));
                        itemClick.onDialogSubmit(v, mMainList, minutes / 60);
                    }

                } else {
                    Toast.makeText(mContext, "Oops, Seems you have not added any place to your Plan", Toast.LENGTH_SHORT).show();
                }
            }
        });

        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mDialog.isShowing())
                    mDialog.show();
                MakeFooterRequest();
            }
        });


    }

    private void MakeFooterRequest() {
        if(Utility.getNextPageToken().compareTo("NA") == 0){
            String messge = "";
            if(getUserSelectedItem(selected_position).contains("%20"))
                messge = getUserSelectedItem(selected_position).replace("%20", " ");
            else
                messge = getUserSelectedItem(selected_position);
            if (mDialog.isShowing())
                mDialog.dismiss();
            Toast.makeText(mContext, "No more "+messge+" found", Toast.LENGTH_SHORT).show();
        }else {
            String query = Utility.GetNextPageUrl(Utility.getNextPageToken());
            Log.d("QUERY", "MakeFooterRequest: "+query);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, query, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("FOOTER_RESP", "Success");
                            if (mDialog.isShowing())
                                mDialog.dismiss();
                            parseResult(response, true);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (mDialog.isShowing())
                                mDialog.dismiss();
                            Log.d("FOOTER_RESP", "Error Occurred");
                            Log.d("FOOTER_RESP", error.getMessage());
                        }
                    });
            MySingleton.getInstance(mContext).addToRequestQueue(jsonObjectRequest);
        }
    }


    private void prepareSelectionView() {
        layouts = new RelativeLayout[]{rView1, rView2, rView3, rView4, rView5, rView6, rView7, rView8, rView9};
        if (selectedPlaces.length > 0) {
            for (int i = 0; i < layouts.length; i++) {
                if (i == 0) {
                    View view = layouts[i];

                    ImageView imageView = view.findViewById(R.id.img_View);
                    TextView textView = view.findViewById(R.id.txt_View);

                    imageView.setImageDrawable(mContext.getResources().getDrawable(drawable[i]));
                    textView.setText(sSelction.get(i));

                } else {
                    View view = layouts[i];
                    if (!isPlaceSelected(sSelction.get(i))) {
                        view.setVisibility(View.GONE);
                    }
                    ImageView imageView = view.findViewById(R.id.img_View);
                    TextView textView = view.findViewById(R.id.txt_View);

                    imageView.setImageDrawable(mContext.getResources().getDrawable(drawable[i]));
                    textView.setText(sSelction.get(i));
                }
            }
        } else {
            for (int i = 0; i < layouts.length; i++) {
                View view = layouts[i];
                ImageView imageView = view.findViewById(R.id.img_View);
                TextView textView = view.findViewById(R.id.txt_View);

                imageView.setImageDrawable(mContext.getResources().getDrawable(drawable[i]));
                textView.setText(sSelction.get(i));
            }
        }

        SetOnLayoutClickListner();
    }

    private boolean isPlaceSelected(String place) {
        boolean isSelected = false;
        for (String iPlaces : selectedPlaces) {
            if (iPlaces.compareTo(place) == 0) {
                isSelected = true;
                break;
            }
        }
        return isSelected;
    }

    private void SetOnLayoutClickListner() {
        for (int i = 0; i < layouts.length; i++) {
            View view = layouts[i];
            final int finalI = i;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selected_position = finalI;
                    playAnimation();
                }
            });
        }

    }

    private void playAnimation() {
        for (int i = 0; i < layouts.length; i++) {
            View view = layouts[i];

            final LottieAnimationView animationView = view.findViewById(R.id.animation_view);

            animationView.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    animationView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            if (i == selected_position) {
                view.setBackgroundColor(mContext.getResources().getColor(R.color.colorLightGrayLine
                ));
                animationView.playAnimation();
                MakeRequest(selected_position);
            } else {
                view.setBackgroundColor(mContext.getResources().getColor(R.color.colorWhite));
                animationView.setVisibility(View.GONE);
            }
        }
    }

    private void MakeRequest(int Selection) {
        if (mLocation != null) {
            //Decrease Radius By Factor of Time;
            float radius = GetRadius(numberOfHours);
            if ((int) radius == 0)
                radius = 0.5f;
            if (Selection == 0) {
                loopCount = 0;
                mFoodPlaces = new ArrayList<>();

                if(footerView.getVisibility() == View.VISIBLE)
                    footerView.setVisibility(View.GONE);

                if(!mDialog.isShowing())
                    mDialog.show();
                FoodRequest();
            } else {

                if(footerView.getVisibility() == View.GONE)
                    footerView.setVisibility(View.VISIBLE);

                String oQuery = getUserSelectedItem(Selection);
                if (oQuery != null) {
                    String query;
                    if (mMainList.size() > 0) {
                        GooglePlaces gplace = mMainList.get(mMainList.size() - 1);
                        Location location = new Location("");
                        location.setLatitude(gplace.getgPlaceLocation().latitude);
                        location.setLongitude(gplace.getgPlaceLocation().longitude);

//                        Log.d("Location", location.getLatitude() + " - " + location.getLongitude());
                        query = Utility.GenerateQuery(oQuery, location, radius);
                    } else {
//                        Log.d("Location", mLocation.getLatitude() + " - " + mLocation.getLongitude());
                        query = Utility.GenerateQuery(oQuery, mLocation, radius);
                    }

                    GetReponse(query);
                    Log.d("QUERY", query);
                } else {
                    Log.d("QUERY", "Query is Null");
                }
            }
        } else {
            Log.d("QUERY", "mLocation Null");
        }
    }

    private void FoodRequest(){
        float radius = GetRadius(numberOfHours);
        String object = sharePref.getString(UtilConstants.USER_PROFILE, "");
        Gson json = new Gson();
        UserProfile userProfile = json.fromJson(object, UserProfile.class);
        String[] fLikes = userProfile.getCuisine().split(",");
        String query = "";
        if (msGooglePlaces.getgPlaceLocation() != null) {
            Log.d("LocationC", mLocation.getLatitude() + " - " + mLocation.getLongitude());
            Location location = new Location("");
            location.setLatitude(msGooglePlaces.getgPlaceLocation().latitude);
            location.setLongitude(msGooglePlaces.getgPlaceLocation().longitude);
            query = Utility.GenerateQuery(fLikes[loopCount].toLowerCase() + "+food", location, radius);

        } else {
            Log.d("LocationD", mLocation.getLatitude() + " - " + mLocation.getLongitude());
            query = Utility.GenerateQuery(fLikes[loopCount].toLowerCase() + "+food", mLocation, radius);
        }
        Log.d("QUERY", query);
        FoodResponse(query, fLikes.length);
    }

    private void FoodResponse(@NonNull String query, @NonNull final int loopSize){
        mGooglePlaces = new ArrayList<GooglePlaces>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, query, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("FOOD_VOLLEY", "Success");
                        ParseFoodResponse(response, loopSize);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        FoodRequest();
                        Log.d("FOOD_VOLLEY", "Error Occurred");
                        Log.d("FOOD_VOLLEY", error.getMessage());
                    }
                });
        MySingleton.getInstance(mContext).addToRequestQueue(jsonObjectRequest);
    }

    private void ParseFoodResponse(@NonNull JSONObject response, int size){
        try{
            if(response.getString("status").compareTo("OK") == 0){
                JSONArray jsonArray = response.getJSONArray("results");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsObject = jsonArray.getJSONObject(i);
                    if (Helper.getmHelperList().size() > 0) {
                        Log.d("PLACE_ID", Helper.checkIfItemExists(jsObject.getString("place_id")) + "");
                        if (!Helper.checkIfItemExists(jsObject.getString("place_id"))) {
                            AddGooglePlaces(jsObject);
                        }
                    } else {
                        AddGooglePlaces(jsObject);
                    }
                }

                sort(mGooglePlaces);

                mFoodPlaces.addAll(mGooglePlaces);

                if(loopCount == size-1){
                    if(mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
//                    Toast.makeText(mContext, loopCount+" OK_Adapter", Toast.LENGTH_SHORT).show();
                    PlacesAdapter placesAdapter = new PlacesAdapter(mContext, R.layout.places_item, mFoodPlaces, true);
                    mPlacesList.setAdapter(placesAdapter);
                }else {
                    loopCount ++;
                    FoodRequest();
                }

            }else {
                if(loopCount == size-1){
                    if(mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
//                    Toast.makeText(mContext, "ERROR_Adapter", Toast.LENGTH_SHORT).show();
                    PlacesAdapter placesAdapter = new PlacesAdapter(mContext, R.layout.places_item, mFoodPlaces, true);
                    mPlacesList.setAdapter(placesAdapter);
                }else {
                    loopCount++;
                    FoodRequest();
                }
            }
        }catch (Exception exp){
            exp.printStackTrace();
            Log.d("ERROR_FOOD", exp.getMessage());
        }
    }

    private void GetReponse(String strings) {
        mGooglePlaces = new ArrayList<GooglePlaces>();

        if (!mDialog.isShowing())
            mDialog.show();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, strings, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (mDialog.isShowing())
                            mDialog.dismiss();
                        Log.d("JSON", "Success");
                        parseResult(response, false);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (mDialog.isShowing())
                            mDialog.dismiss();
                        mPlacesList.setAdapter(null);
                        Log.d("JSON", "Error - " + error.getMessage());
                        ShowReload(error.getMessage());
                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(mContext).addToRequestQueue(jsonObjectRequest);
    }

    private void ShowReload(String message) {
        if (Utility.checkConnection(message.toLowerCase())) {
            Pair<String, String> pair = Utility.GetErrorMessage(message.toLowerCase());
            AlertDialog.Builder aDialogBuilder = new AlertDialog.Builder(mContext);
            aDialogBuilder.setCancelable(false);
            aDialogBuilder.setTitle(pair.first);
            aDialogBuilder.setMessage(pair.second);
            aDialogBuilder.setPositiveButton("Reload", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    MakeRequest(selected_position);
                }
            });
            alertDialog = aDialogBuilder.create();
            if (!alertDialog.isShowing())
                alertDialog.show();
        }
    }

    private String parseResult(JSONObject response, boolean isFooterItem) {
        String process = "";
        Log.d("JSON", "ParsingResult");
        int position = 0;
        if(isFooterItem){
            position = mGooglePlaces.size()-1;
            Log.d("POSITION", "parseResult: "+position);
        }
        try {
            final JSONArray jsonArray = response.getJSONArray("results");

            if(response.has("next_page_token"))
                Utility.setNextPageToken(response.getString("next_page_token"));
            else
                Utility.setNextPageToken("NA");

            if (response.getString("status").compareTo("OK") == 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsObject = jsonArray.getJSONObject(i);
                    if (Helper.getmHelperList().size() > 0) {
                        Log.d("PLACE_ID", Helper.checkIfItemExists(jsObject.getString("place_id")) + "");
                        if (!Helper.checkIfItemExists(jsObject.getString("place_id"))) {
                            AddGooglePlaces(jsObject);
                        }
                    } else {
                        AddGooglePlaces(jsObject);
                    }
                }

                if(!isFooterItem)
                    sort(mGooglePlaces);

                if(isFooterItem){
                    PlacesAdapter placesAdapter = (PlacesAdapter) mPlacesList.getAdapter();
                    placesAdapter.notifyDataSetChanged();
                    mPlacesList.smoothScrollToPosition(position+1);

                }else {
                    mPlacesList.setAdapter(null);
                    PlacesAdapter placesAdapter = new PlacesAdapter(mContext, R.layout.places_item, mGooglePlaces, true);
                    mPlacesList.setAdapter(placesAdapter);
                }


                process = "Success";
            } else {
//                    Toast.makeText(mContext, isLoopComplete+"", Toast.LENGTH_SHORT).show();
                mPlacesList.setAdapter(null);
                PlacesAdapter placesAdapter = new PlacesAdapter(mContext, R.layout.places_item, mGooglePlaces, true);
                mPlacesList.setAdapter(placesAdapter);
            }

        } catch (Exception e) {
            Log.d("JSON", "ErrorParsing " + e.getMessage());
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
            process = e.getMessage();
            e.printStackTrace();
        }

        return process;
    }

    private void sort(@NonNull ArrayList<GooglePlaces> mGooglePlaces) {
        int n = mGooglePlaces.size();

        // One by one move boundary of unsorted subarray
        for (int i = 0; i < n - 1; i++) {
            // Find the minimum element in unsorted array
            int min_idx = i;
            for (int j = i + 1; j < n; j++)
                if (mGooglePlaces.get(j).getgPlaceRating() > mGooglePlaces.get(min_idx).getgPlaceRating())
                    min_idx = j;

            // Swap the found minimum element with the first
            // element
            Collections.swap(mGooglePlaces, i, min_idx);
        }
    }

    private void CalculateTime() {
        /**
         * Subtract {@link SelectionActivity#minutes} Based on This Conditions
         * 4 -5 (10 Minutes Est.)
         * 6 -8 (20 Minutes Est.)
         * 8 - 10 (30 Minutes Est.)
         *   > 10 (35 Minutes Est.)
         *
         * if{@link SelectionActivity#mSelectedPosition} == 1
         *   {@link SelectionActivity#minutes} = {@link SelectionActivity#minutes} - (Time Required To Get To This Point + Time Required To Return)
         * else({@link SelectionActivity#GetDistance(LatLng)} > 4 || < 5){
         *      4 -5 (10 Minutes Est.)
         *     {@link SelectionActivity#minutes} = {@link SelectionActivity#minutes} - time required
         * }
         *
         */
    }

    private void ShowWaitTimeAlert(final int position, final PlacesAdapter placesAdapter) {
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(mContext).inflate(R.layout.alert_view, null);
        dialog.setContentView(view);
        dialog.findViewById(R.id.button_holder).setVisibility(View.VISIBLE);
        TextView textView = dialog.findViewById(R.id.alert_title);
        textView.setText("Tour Time");
        TextView textView1 = dialog.findViewById(R.id.alert_message);
        textView1.setText("Enter,  how much time you want to spend here");
        final EditText editText = dialog.findViewById(R.id.edit_alert);
        TextView btnPositive = dialog.findViewById(R.id.btn_positive);
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().length() == 0) {
                    Snackbar.make(v, "Please, Enter Time", Snackbar.LENGTH_SHORT).show();
                    editText.setText("");
                    editText.requestFocus();
                } else if(editText.getText().toString().compareTo("0") == 0 || editText.getText().toString().compareTo("00") == 0){
                    Snackbar.make(v, "Please, Enter Time Valid Time", Snackbar.LENGTH_SHORT).show();
                    editText.setText("");
                    editText.requestFocus();
                }else {
                    dialog.dismiss();
                    AddItemToMainList(position, Integer.parseInt(editText.getText().toString()));
                    placesAdapter.notifyDataSetChanged();
                }
            }
        });

        TextView btnNegative = dialog.findViewById(R.id.btn_negative);
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialog.isShowing())
                    dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void AddGooglePlaces(JSONObject jsObject) throws JsonIOException, JSONException {
        JSONObject location = jsObject.getJSONObject("geometry").getJSONObject("location");
        GooglePlaces googlePlaces = new GooglePlaces();
        googlePlaces.setgPlaceId(jsObject.getString("place_id"));
//        googlePlaces.setgPlaceIcon(jsObject.getString("icon"));
        googlePlaces.setgPlaceName(jsObject.getString("name"));
        googlePlaces.setgPlaceAddress(jsObject.getString("formatted_address"));
        googlePlaces.setgPlaceRating(Float.parseFloat(jsObject.getString("rating")));

        LatLng latLng = new LatLng(Double.parseDouble(location.getString("lat")), Double.parseDouble(location.getString("lng")));
        Log.d("Location", latLng.latitude + " - " + latLng.longitude);
        googlePlaces.setgPlaceLocation(latLng);
        if (mMainList.size() > 0) {
            GooglePlaces gplace = mMainList.get(mMainList.size() - 1);
            Location glocation = new Location("");
            glocation.setLatitude(gplace.getgPlaceLocation().latitude);
            glocation.setLongitude(gplace.getgPlaceLocation().longitude);
            googlePlaces.setDistanceFromSource(GetDistance(latLng, glocation));
        } else {
            googlePlaces.setDistanceFromSource(GetDistance(latLng, mLocation));
        }
        googlePlaces.setTimeToTravel(0);
        googlePlaces.setDayno(dayCount);
        googlePlaces.setSrno(-1);

        if(GetDistance(latLng, mLocation) < 50)
            mGooglePlaces.add(googlePlaces);
    }

    private float GetRadius(int numberOfHours) {
        if (minutes == numberOfHours * 60) {
            return Float.parseFloat(new DecimalFormat("#0.0#").format((numberOfHours * 1000)));
        } else {
            return Float.parseFloat(new DecimalFormat("#0.0#").format((minutes / 60) * 1000));
        }
    }

    /**
     * {@link SelectionActivity.PlacesAdapter}
     * This Custom Adapter For the ListView to show the Google Places Data for User Selection
     */

    private class PlacesAdapter extends ArrayAdapter<GooglePlaces> {
        private Context aContext;
        private ArrayList<GooglePlaces> aGooglePlaces;
        private boolean showAddButton;

        public PlacesAdapter(@NonNull Context context, int resource, @NonNull ArrayList<GooglePlaces> places, boolean showAdd) {
            super(context, resource, places);
            aContext = context;
            aGooglePlaces = places;
            showAddButton = showAdd;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            MyViewHolder myViewHolder;
            if (convertView == null) {
                myViewHolder = new MyViewHolder();
                convertView = LayoutInflater.from(aContext).inflate(R.layout.places_item, parent, false);
                myViewHolder.place_name = convertView.findViewById(R.id.item_Name);
                myViewHolder.place_distance = convertView.findViewById(R.id.item_Distance);
                myViewHolder.place_rating = convertView.findViewById(R.id.item_RatingText);
                myViewHolder.place_time = convertView.findViewById(R.id.item_estTime);
                myViewHolder.rating = convertView.findViewById(R.id.item_Rating);
                myViewHolder.selection = convertView.findViewById(R.id.item_Selection);
                myViewHolder.delete = convertView.findViewById(R.id.item_delete);
                convertView.setTag(myViewHolder);
            } else {
                myViewHolder = (MyViewHolder) convertView.getTag();
            }

            if (showAddButton) {
                myViewHolder.selection.setVisibility(View.VISIBLE);
            } else {
                myViewHolder.place_time.setVisibility(View.VISIBLE);
                myViewHolder.delete.setVisibility(View.VISIBLE);
            }

            myViewHolder.place_name.setText(aGooglePlaces.get(position).getgPlaceName());
            myViewHolder.place_rating.setText(aGooglePlaces.get(position).getgPlaceRating() + "");
            myViewHolder.place_distance.setText(decimalFormat.format(aGooglePlaces.get(position).getDistanceFromSource()) + " km");
            myViewHolder.rating.setRating(aGooglePlaces.get(position).getgPlaceRating());
            myViewHolder.place_time.setText(aGooglePlaces.get(position).getTimeToTravel() + " min");

            myViewHolder.selection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectedPosition = position;
                    ShowWaitTimeAlert(mSelectedPosition, PlacesAdapter.this);
                }
            });

            myViewHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeFromList(position);
                }
            });

            return convertView;
        }

        private class MyViewHolder {
            TextView place_name, place_distance, place_rating, place_time;
            RatingBar rating;
            ImageView selection, delete;
        }
    }

    private void removeFromList(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Remove this Place");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                minutes = minutes + (mMainList.get(position).getTimeToTravel() + mMainList.get(position).getTimeSpent());
                mMainList.remove(position);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void AddItemToMainList(final int selection, int time) {
        mSelectedPosition = -1;

        Log.d("TIME_O", numberOfHours + " - " + selected_position);
        if (selected_position == 0) {
            GooglePlaces googlePlaces = mFoodPlaces.get(selection);

            if (checkReturnTimeAvailable(googlePlaces, true, time)) {
                googlePlaces.setTimeToTravel(getTimeToTravel(googlePlaces.getDistanceFromSource()));
                googlePlaces.setTimeSpent(time);
                minutes = minutes - (googlePlaces.getTimeToTravel() + time);
                Log.d("TimeD", "MinR - " + getTimeToTravel(googlePlaces.getDistanceFromSource()) + " - MinLeft - " + minutes + " TimeSpent - " + time);
                mFoodPlaces.set(selection, googlePlaces);

                mMainList.add(mFoodPlaces.get(selection));
                Utility.mRemovedPlaces.add(mFoodPlaces.get(selection));
                mFoodPlaces.remove(selection);

                Toast.makeText(mContext, "Place Added", Toast.LENGTH_SHORT).show();

//                Log.d("LIST_P", "S - " + mMainList.size() + " R - " + Utility.mRemovedPlaces.size() + "  O- " + mGooglePlaces.size());
            } else {
                Toast.makeText(mContext, "Can't add this place, as No time Left to Get to this point and Return Back", Toast.LENGTH_SHORT).show();
                Log.d("TimeD", "MinR - " + getTimeToTravel(googlePlaces.getDistanceFromSource()) + " - MinLeft - " + minutes + " TimeSpent - " + time);
            }

        } else {
            GooglePlaces googlePlaces = mGooglePlaces.get(selection);

            if (checkReturnTimeAvailable(googlePlaces, true, time)) {
                googlePlaces.setTimeToTravel(getTimeToTravel(googlePlaces.getDistanceFromSource()));
                googlePlaces.setTimeSpent(time);
                minutes = minutes - (googlePlaces.getTimeToTravel() + time);
                Log.d("TimeD", "MinR - " + getTimeToTravel(googlePlaces.getDistanceFromSource()) + " - MinLeft - " + minutes + " TimeSpent - " + time);
                mGooglePlaces.set(selection, googlePlaces);
//
                mMainList.add(mGooglePlaces.get(selection));
                Utility.mRemovedPlaces.add(mGooglePlaces.get(selection));
                mGooglePlaces.remove(selection);

                Toast.makeText(mContext, "Place Added", Toast.LENGTH_SHORT).show();

//                Log.d("LIST_P", "S - " + mMainList.size() + " R - " + Utility.mRemovedPlaces.size() + "  O- " + mGooglePlaces.size());
            } else {
                Toast.makeText(mContext, "Can't add this place, as No time Left to Get to this point and Return Back", Toast.LENGTH_SHORT).show();
                Log.d("TimeD", "MinR - " + getTimeToTravel(googlePlaces.getDistanceFromSource()) + " - MinLeft - " + minutes + " TimeSpent - " + time);
            }
        }

    }

    private boolean checkReturnTimeAvailable(GooglePlaces googlePlaces, boolean isTimeSpentCheck, int timeSpent) {
        double distance = 0.0;
        if (mMainList.size() > 0) {
            GooglePlaces gplace = mMainList.get(mMainList.size() - 1);
            Location glocation = new Location("");
            glocation.setLatitude(gplace.getgPlaceLocation().latitude);
            glocation.setLongitude(gplace.getgPlaceLocation().longitude);
            distance = GetDistance(googlePlaces.getgPlaceLocation(), glocation);
        } else {
            distance = GetDistance(googlePlaces.getgPlaceLocation(), mLocation);
        }
        double min = getTimeToTravel(distance);
        Log.d("TimeD", "MinTotal - " + min + " - MinLeft - " + minutes + " TimeSpent - " + timeSpent);
        if (isTimeSpentCheck)
            return minutes >= ((min * 2) + timeSpent);
        else
            return minutes >= min;
    }

    private int getTimeSpentHere(int selection) {
        if (selection == 1) {
            return GetTimeSpent(SELECTION.CAFE);
        } else if (selection == 2) {
            return GetTimeSpent(SELECTION.MUSEUM);
        } else if (selection == 3) {
            return GetTimeSpent(SELECTION.BEACHES);
        } else if (selection == 4) {
            return GetTimeSpent(SELECTION.PARKS);
        } else if (selection == 5) {
            return GetTimeSpent(SELECTION.SHOPPING);
        } else if (selection == 6) {
            return GetTimeSpent(SELECTION.AMUZE_PARK);
        } else if (selection == 7) {
            return GetTimeSpent(SELECTION.GALLERY);
        } else if (selection == 8) {
            return GetTimeSpent(SELECTION.NIGHT_CLUB);
        } else {
            return GetTimeSpent(SELECTION.FOOD);
        }
    }

    public static int getTimeToTravel(double km) {
        if(km == 0 || km == 1){
            return 2;
        }else
            return (int) km * 4;
    }

    enum SELECTION {
        FOOD, CAFE, MUSEUM, BEACHES, PARKS, SHOPPING, AMUZE_PARK, GALLERY, NIGHT_CLUB;
    }

    private String getUserSelectedItem(int selection) {

        String response = "";

        if (selection == 1) {
            response = GetString(SELECTION.CAFE);
        } else if (selection == 2) {
            response = GetString(SELECTION.MUSEUM);
        } else if (selection == 3) {
            response = GetString(SELECTION.BEACHES);
        } else if (selection == 4) {
            response = GetString(SELECTION.PARKS);
        } else if (selection == 5) {
            response = GetString(SELECTION.SHOPPING);
        } else if (selection == 6) {
            response = GetString(SELECTION.AMUZE_PARK);
        } else if (selection == 7) {
            response = GetString(SELECTION.GALLERY);
        } else if (selection == 8) {
            response = GetString(SELECTION.NIGHT_CLUB);
        }

        return response;
    }

    private String GetString(SELECTION selection) {
        String select = "";
        switch (selection) {
            case CAFE:
                select = "cafe";
                break;
            case MUSEUM:
                select = "museum";
                break;
            case BEACHES:
                select = "beach";
                break;
            case PARKS:
                select = "parks";
                break;
            case SHOPPING:
                select = "shopping_malls";
                break;
            case AMUZE_PARK:
                select = "amusement_parks";
                break;
            case GALLERY:
                select = "gallery";
                break;
            case NIGHT_CLUB:
                select = "night%20club";
                break;
        }
        return select;
    }

    private int GetTimeSpent(SELECTION selection) {
        int select = 0;
        switch (selection) {
            case FOOD:
                select = 60;
                break;
            case CAFE:
                select = 30;
                break;
            case MUSEUM:
                select = 45;
                break;
            case BEACHES:
                select = 45;
                break;
            case PARKS:
                select = 60;
                break;
            case SHOPPING:
                select = 90;
                break;
            case AMUZE_PARK:
                select = 60;
                break;
            case GALLERY:
                select = 30;
                break;
            case NIGHT_CLUB:
                select = 90;
                break;
        }
        return select;
    }

    private String getQuery(String choice, boolean Veg) {
        String veg = (Veg ? "vegetarian" : "nonvegetarian");
        String choices = choice + "+" + veg;
        Log.d("QUERY", choices + " - O " + choice);
        return choices;
    }

    private double GetDistance(LatLng latLng, Location location) {
        double km = 1000;
        Location nLocation = new Location("");
        nLocation.setLatitude(latLng.latitude);
        nLocation.setLongitude(latLng.longitude);
        Log.d("Distance", ((double) location.distanceTo(nLocation)) + "");
        return ((double) location.distanceTo(nLocation)) / km;
    }

    public void setItemClick(ItemClick itemClick) {
        this.itemClick = itemClick;
    }

}

