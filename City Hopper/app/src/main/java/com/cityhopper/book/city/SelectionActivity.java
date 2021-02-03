package com.cityhopper.book.city;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.cityhopper.book.MapActivity;
import com.cityhopper.book.R;
import com.cityhopper.book.helper.GreedyAlgorithm;
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
import java.util.List;

public class SelectionActivity extends AppCompatActivity implements Dialog.OnShowListener {
    public static final int DEFAULT_DURATION = 0;
    public static final int DEFUALT_SWIPE_VALUE = -70;
    public static final int REQUEST_CHECK_SETTINGS = 1002;

    private SharedPreferences sharedPref;
    private Dialog mapd;
    private ImageView imageMapButton;
    private GoogleMap gMap;
    private double lat = 0.0, lng = 0.0;
    public static final int PERMISSION_REQUEST_CODE = 1002;

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLocation;

    private ListView list_Places;
    private View mFooterView;
    private LinearLayout btnShowDialog;
    private Button btn_SSubmit;

    private String[] places;
    private ArrayList<String> sSelction;
    private int[] drawable = new int[]{R.drawable.food, R.drawable.cafe, R.drawable.museum, R.drawable.beach, R.drawable.park, R.drawable.mall,
            R.drawable.park, R.drawable.museum, R.drawable.nightclub};
    private RelativeLayout[] layouts;
    private SelectionDialog selectionDialog;
    private int selected_position = -1;


    private ArrayList<GooglePlaces> mGooglePlaces, mFoodPlaces;
    private int mSelectedPosition = -1;
    private int isLoopCompete = -1;

    //Number of Hours user want to Travel
    private int numberOfHours = 0;
    private DecimalFormat decimalFormat = new DecimalFormat("#00.0#");
    private int minutes = 0;
    private PlacesAdapter mPlaceAdapter;

    private AlertDialog alertDialog;
    private LocationRequest mLocationRequest;
    String weather="",temp_String="",humidity="",wind_string="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        sharedPref = getSharedPreferences(UtilConstants.SharedPref, Context.MODE_PRIVATE);
        getSupportActionBar().setTitle("NearBy");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        numberOfHours = intent.getIntExtra(UtilConstants.HOURS, 0);
        minutes = intent.getIntExtra(UtilConstants.MINS, 0);


        Utility.mSelectedPlaces.clear();
        Utility.mRemovedPlaces.clear();

//        Toast.makeText(this, Utility.mSelectedPlaces.size()+"", Toast.LENGTH_SHORT).show();

        mFooterView = LayoutInflater.from(SelectionActivity.this).inflate(R.layout.footer_layout, null);
        list_Places = findViewById(R.id.list_result);
        btnShowDialog = findViewById(R.id.dialog_s_layout);
        btn_SSubmit = findViewById(R.id.btn_selection);

//        cuisine = getResources().getStringArray(R.array.Cuisine);
        places = getResources().getStringArray(R.array.Places);

        //Combining this above list
        sSelction = new ArrayList<String>();
        sSelction.add("Food");
        sSelction.addAll(Arrays.asList(places));

        selectionDialog = new SelectionDialog(SelectionActivity.this);
        selectionDialog.setOnShowListener(this);

        MapDailog();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(SelectionActivity.this);
        mLocationRequest = createLocationRequest();

        mPlaceAdapter = new PlacesAdapter(SelectionActivity.this, R.layout.places_item, Utility.mSelectedPlaces, false);
        list_Places.setAdapter(mPlaceAdapter);

        mapd.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nearby,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();


        btnShowDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectionDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                if (!selectionDialog.isShowing())
                    selectionDialog.show();
            }
        });

        btn_SSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAlgoDialog();
//
            }
        });
    }

    private void showAlgoDialog() {

//        UserProfile userProfile = Utility.getUserProfile();
//                Log.d("USER", userProfile.getName() + "\n" + userProfile.getContact() + "\n" + userProfile.getAddress() + "\n" + userProfile.getVegetarian() + "\n" + userProfile.getDrinker()
//                        + "\n" + userProfile.getCuisine() + "\n" + userProfile.getPlaces());

        LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        final GooglePlaces googlePlaces = new GooglePlaces();
        googlePlaces.setgPlaceId("myLocation");
        googlePlaces.setgPlaceName("MyLocation");
        googlePlaces.setgPlaceLocation(latLng);
        googlePlaces.setTimeToTravel(0);
        googlePlaces.setgPlaceRating(0);
        googlePlaces.setgPlaceAddress("NA");
        googlePlaces.setDistanceFromSource(0);
        googlePlaces.setgPlaceReview("No Review");
        googlePlaces.setgPlaceOpen(false);
        googlePlaces.setDayno(-1);

        Utility.setmGooglePlaces(googlePlaces);

        if (Utility.mSelectedPlaces.size() >= 1) {
            for(GooglePlaces place : Utility.mSelectedPlaces){
                Log.d("SORT", "Before : "+String.format("Name = %s, Distance = %s", place.getgPlaceName(), place.getDistanceFromSource()));
            }

            GooglePlaces[] pla = new GooglePlaces[Utility.mSelectedPlaces.size()];
            pla = Utility.mSelectedPlaces.toArray(pla);

            GreedyAlgorithm greedyAlgorithm = new GreedyAlgorithm(pla);

            final ArrayList<GooglePlaces> placesAglo = new ArrayList<GooglePlaces>(greedyAlgorithm.getmLocalGooglePlaces());
            Collections.copy(placesAglo, greedyAlgorithm.getmLocalGooglePlaces());

            for(GooglePlaces place : placesAglo){
                Log.d("SORT", "After : "+String.format("Name = %s, Distance = %s", place.getgPlaceName(), place.getDistanceFromSource()));
            }

            AlertDialog alertDialog = new AlertDialog.Builder(SelectionActivity.this)
                    .setTitle("Sort Places")
                    .setMessage("How you want to Sort the Places ?")
                    .setPositiveButton("Auto-Sort", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                            Utility.mUpdatedPlaces.clear();
//                            Utility.mUpdatedPlaces.add(Utility.getmGooglePlaces());
                            Utility.mUpdatedPlaces.addAll(placesAglo);

                            for(GooglePlaces pl : Utility.mUpdatedPlaces){
                                Log.d("SELECTION", "Places: "+pl.getgPlaceName() +" Serial No. "+pl.getSrno());
                            }

                            Intent intent = new Intent(SelectionActivity.this, MapActivity.class);
                            intent.putExtra(UtilConstants.SHOWBUTTON, true);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Manual", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            Utility.mSelectedPlaces.add(0, googlePlaces);
//                Toast.makeText(SelectionActivity.this, Utility.mSelectedPlaces.size() + "", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SelectionActivity.this, SortActivity.class);
                            startActivity(intent);
                        }
                    })
                    .create();

            alertDialog.show();
//
        } else {
            Utility.ShowAlertDialog(SelectionActivity.this, "Minimum Places", "Choose,  At least one place to proceed ", false);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSettingDialog();
            } else {
                ActivityCompat.requestPermissions(SelectionActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    public void MapDailog() {

        mapd = new Dialog(SelectionActivity.this);
        mapd.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mapd.setContentView(R.layout.map_activity);
        mapd.findViewById(R.id.dialog_title).setVisibility(View.VISIBLE);

        Button Submit = (Button) mapd.findViewById(R.id.btn_Map);
        final android.support.v7.widget.SearchView place_search = (android.support.v7.widget.SearchView) mapd.findViewById(R.id.searchLocation);
        place_search.setVisibility(View.VISIBLE);
        Submit.setVisibility(View.VISIBLE);
        Button cancel = (Button) mapd.findViewById(R.id.btn_Map_Cancel);
        cancel.setVisibility(View.VISIBLE);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mapd.isShowing())
                    mapd.dismiss();
                finish();
            }
        });


        imageMapButton = (ImageView) mapd.findViewById(R.id.btnCurrentLocation);
        imageMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(SelectionActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(SelectionActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    showSettingDialog();
                } else {
                    ActivityCompat.requestPermissions(SelectionActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
                }
            }
        });

        final SupportMapFragment map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        map.onCreate(mapd.onSaveInstanceState());
        map.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                gMap = googleMap;


//                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 8));

                gMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng ll) {
                        gMap.clear();
                        lat = ll.latitude;
                        lng = ll.longitude;

                        mLocation = new Location("");
                        mLocation.setLatitude(ll.latitude);
                        mLocation.setLongitude(ll.longitude);

                        MarkerOptions mo = new MarkerOptions();
                        mo.position(new LatLng(lat, lng));
                        mo.draggable(true);
                        Marker m = gMap.addMarker(mo);
                        m.showInfoWindow();
                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15));
                    }
                });


                gMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDragStart(Marker marker) {
                    }

                    @Override
                    public void onMarkerDrag(Marker marker) {
                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker) {
                        LatLng ll = marker.getPosition();
                        lat = ll.latitude;
                        lng = ll.longitude;

                        mLocation = new Location("");
                        mLocation.setLatitude(ll.latitude);
                        mLocation.setLongitude(ll.longitude);

                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15));
                    }
                });

                place_search.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        try {
                            Geocoder g = new Geocoder(SelectionActivity.this);
                            List<Address> list = g.getFromLocationName(s, 1);
                            Address add = list.get(0);

                            mLocation = new Location("");
                            mLocation.setLatitude(add.getLatitude());
                            mLocation.setLongitude(add.getLongitude());

                            gMap.clear();
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                            gMap.addMarker(markerOptions);
                            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 15));

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(SelectionActivity.this, "No Match Found, Try Again", Toast.LENGTH_SHORT).show();
                        }
                        place_search.onActionViewCollapsed();
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return false;
                    }
                });

                imageMapButton.performClick();
            }
        });


        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLocation == null) {
                    Snackbar.make(view, "You have not chosen any location, Choose a Location", Snackbar.LENGTH_SHORT).show();
                } else {
                    String ll = mLocation.getLatitude() + "";
                    String ll2 = mLocation.getLongitude() + "";
                    if (ll.length() > 3 && ll2.length() > 3) {
                        mapd.hide();
                        calculate_weather();
                        if (selectionDialog != null) {
                            if (!selectionDialog.isShowing())
                                selectionDialog.show();
                        }
                    }
                }
            }
        });
    }

    private void showSettingDialog() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(1000);//5 sec Time interval for location update
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient to show dialog always when GPS is off

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mFusedLocationClient.asGoogleApiClient(), builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
//                        new logintask().execute(Email.getText().toString(), Pass.getText().toString());
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(SelectionActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                startLocationUpdates();
            } else
                imageMapButton.performClick();

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("MissingPermission")
    private void GetMyLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
//                        Toast.makeText(SelectionActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        if (location != null) {
                            mLocation = location;
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                            goToLocation(location);
//                            Log.d("LOCATION", location.getLatitude() + " - " + location.getLongitude());
                        } else {
                            GetMyLocation();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(SelectionActivity.this, "L Exp", Toast.LENGTH_SHORT).show();
                        Log.d("LOCATION", e.getMessage());
                    }
                });
    }

    private void goToLocation(Location location) {

        gMap.clear();

        String title = getLocality(location);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        final MarkerOptions mo = new MarkerOptions();
        if (title.compareTo("NA") != 0)
            mo.title(title);
        mo.position(latLng);
        mo.draggable(false);
        mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        gMap.addMarker(mo);
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult.getLastLocation() != null) {
                Log.d("Location", "Location Not Null");
                mLocation = locationResult.getLastLocation();
                lat = mLocation.getLatitude();
                lng = mLocation.getLongitude();
                goToLocation(mLocation);
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);

            } else {
                Log.d("Location", "Location is Null");
            }

        }

        ;
    };

    private String getLocality(@NonNull Location location) {
        String locality;
        try {
            Geocoder g = new Geocoder(SelectionActivity.this);
            List<Address> list = g.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            Address add = list.get(0);
            locality = "";

            String loc1 = add.getLocality() + ", " + add.getCountryName();
            String loc = add.getThoroughfare() + ", " + add.getSubLocality();
//            Log.d("LOCATION", loc1 + "\n" + loc);

            if (add.getLocality() != null) {
                locality = add.getLocality();
            } else
                locality = "NA";

        } catch (Exception e) {
            locality = "NA";
//            Toast.makeText(SelectionActivity.this, "No Match Found" + "\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return locality;

    }

    private void refreshTextView() {
//        String select = "";
//        for (String selection : sCuisines) {
//            if (selection.compareTo("NA") != 0)
//                select = select.concat(selection + ",");
//        }
//        select = select.substring(0, select.length() - 1);

//        grid_Selected.setText("");
//        grid_Selected.setText(select);
    }

    @Override
    public void onShow(DialogInterface dialog) {
        if (btnShowDialog.getVisibility() == View.VISIBLE)
            btnShowDialog.setVisibility(View.GONE);
    }

    /**
     * {@link SelectionActivity.SelectionDialog}
     * This Custom Dialog is Used to Create the Selection View
     */

    private class SelectionDialog extends Dialog {
        private Context mContext;
        private RelativeLayout rView1, rView2, rView3, rView4, rView5, rView6, rView7, rView8, rView9;
        private LinearLayout grid_Selection;
        private Dialog mDialog;
        private ListView mPlacesList;
        private Button btnSubmit, btnCancel;
        private TextView footerView;
        private String[] selectedPlaces;

        public SelectionDialog(@NonNull Context context) {
            super(context);
            mContext = context;
        }

        public SelectionDialog(@NonNull Context context, int themeResId) {
            super(context, themeResId);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.selection_dialog);
            getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
            setCancelable(false);

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

            mDialog = new Dialog(mContext);
            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mDialog.setContentView(R.layout.loading_dialog);
            mDialog.setCancelable(false);

            Gson json = new Gson();
            UserProfile userProfile = json.fromJson(sharedPref.getString(UtilConstants.USER_PROFILE, ""), UserProfile.class);
            if (userProfile != null) {
                selectedPlaces = userProfile.getPlaces().split(",");
            }

            prepareSelectionView();

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (btnShowDialog.getVisibility() == View.GONE)
                        btnShowDialog.setVisibility(View.VISIBLE);
                    dismiss();
//                    finish();
                }
            });

            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utility.mSelectedPlaces.size() > 0) {
                        GooglePlaces googlePlaces = Utility.mSelectedPlaces.get(Utility.mSelectedPlaces.size() - 1);
                        if (checkReturnTimeAvailable(googlePlaces, false, 0)) {
                            if (isShowing())
                                dismiss();
                            if (btnShowDialog.getVisibility() == View.GONE)
                                btnShowDialog.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(mContext, "Time Not Available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(mContext, "Oops, Seems you have not added any place to your Plan", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            footerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mDialog.isShowing())
                        mDialog.show();
                    MakeFooterRequest();
                }
            });
        }

        private void MakeFooterRequest() {
            if (Utility.getNextPageToken().compareTo("NA") == 0) {

                String messge = "";
                if (getUserSelectedItem(selected_position).contains("%20"))
                    messge = getUserSelectedItem(selected_position).replace("%20", " ");
                else
                    messge = getUserSelectedItem(selected_position);

                if (mDialog.isShowing())
                    mDialog.dismiss();
                Toast.makeText(mContext, "No more " + messge + " found", Toast.LENGTH_SHORT).show();
            } else {
                String query = Utility.GetNextPageUrl(Utility.getNextPageToken());
                Log.d("QUERY", "MakeFooterRequest: " + query);
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

                        imageView.setImageDrawable(getResources().getDrawable(drawable[i]));
                        textView.setText(sSelction.get(i));

                    } else {
                        View view = layouts[i];
                        if (!isPlaceSelected(sSelction.get(i))) {
                            view.setVisibility(View.GONE);
                        }
                        ImageView imageView = view.findViewById(R.id.img_View);
                        TextView textView = view.findViewById(R.id.txt_View);

                        imageView.setImageDrawable(getResources().getDrawable(drawable[i]));
                        textView.setText(sSelction.get(i));
                    }
                }
            } else {
                for (int i = 0; i < layouts.length; i++) {
                    View view = layouts[i];
                    ImageView imageView = view.findViewById(R.id.img_View);
                    TextView textView = view.findViewById(R.id.txt_View);

                    imageView.setImageDrawable(getResources().getDrawable(drawable[i]));
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
                    view.setBackgroundColor(getResources().getColor(R.color.colorLightGrayLine));
                    animationView.playAnimation();
                    MakeRequest(selected_position);
                } else {
                    view.setBackgroundColor(getResources().getColor(R.color.colorWhite));
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
                    isLoopCompete = 0;
                    mFoodPlaces = new ArrayList<>();
                    if (footerView.getVisibility() == View.VISIBLE)
                        footerView.setVisibility(View.GONE);
                    if (!mDialog.isShowing())
                        mDialog.show();
                    FoodRequest();
                } else {
                    if (footerView.getVisibility() == View.GONE)
                        footerView.setVisibility(View.VISIBLE);
                    String oQuery = getUserSelectedItem(Selection);
                    if (oQuery != null) {
                        String query;
                        if (Utility.mSelectedPlaces.size() > 0) {
                            GooglePlaces gplace = Utility.mSelectedPlaces.get(Utility.mSelectedPlaces.size() - 1);
                            Location location = new Location("");
                            location.setLatitude(gplace.getgPlaceLocation().latitude);
                            location.setLongitude(gplace.getgPlaceLocation().longitude);

//                            Log.d("Location", location.getLatitude() + " - " + location.getLongitude());
                            query = Utility.GenerateQuery(oQuery, location, radius);
                        } else {
//                            Log.d("Location", mLocation.getLatitude() + " - " + mLocation.getLongitude());
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

        private void FoodRequest() {
            float radius = GetRadius(numberOfHours);
            String object = sharedPref.getString(UtilConstants.USER_PROFILE, "");
            Gson json = new Gson();
            UserProfile userProfile = json.fromJson(object, UserProfile.class);
            String[] fLikes = userProfile.getCuisine().split(",");
            String query = "";
            if (Utility.mSelectedPlaces.size() > 0) {
                Log.d("LocationC", mLocation.getLatitude() + " - " + mLocation.getLongitude());
                Location location = new Location("");
                location.setLatitude(Utility.mSelectedPlaces.get(Utility.mSelectedPlaces.size() - 1).getgPlaceLocation().latitude);
                location.setLongitude(Utility.mSelectedPlaces.get(Utility.mSelectedPlaces.size() - 1).getgPlaceLocation().longitude);
                query = Utility.GenerateQuery(fLikes[isLoopCompete].toLowerCase() + "+food", location, radius);

            } else {
                Log.d("LocationD", mLocation.getLatitude() + " - " + mLocation.getLongitude());
                query = Utility.GenerateQuery(fLikes[isLoopCompete].toLowerCase() + "+food", mLocation, radius);
            }
            Log.d("QUERY", query);
            FoodResponse(query, fLikes.length);
        }

        private void FoodResponse(@NonNull String query, @NonNull final int loopSize) {
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

        private void ParseFoodResponse(@NonNull JSONObject response, int size) {
            try {
                if (response.getString("status").compareTo("OK") == 0) {
                    JSONArray jsonArray = response.getJSONArray("results");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsObject = jsonArray.getJSONObject(i);
                        if (Utility.mSelectedPlaces.size() > 0) {
                            Log.d("Result", Utility.CheckPlaceID(jsObject.getString("place_id"), Utility.mSelectedPlaces) + "");
                            if (Utility.CheckPlaceID(jsObject.getString("place_id"), Utility.mSelectedPlaces)) {
                                AddGooglePlaces(jsObject);
                            }
                        } else {
                            AddGooglePlaces(jsObject);
                        }
                    }

                    sort(mGooglePlaces);

                    mFoodPlaces.addAll(mGooglePlaces);

                    if (isLoopCompete == size - 1) {
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
//                    Toast.makeText(mContext, loopCount+" OK_Adapter", Toast.LENGTH_SHORT).show();
                        PlacesAdapter placesAdapter = new PlacesAdapter(mContext, R.layout.places_item, mFoodPlaces, true);
                        mPlacesList.setAdapter(placesAdapter);
                    } else {
                        isLoopCompete++;
                        FoodRequest();
                    }

                } else {
                    if (isLoopCompete == size - 1) {
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
//                    Toast.makeText(mContext, "ERROR_Adapter", Toast.LENGTH_SHORT).show();
                        PlacesAdapter placesAdapter = new PlacesAdapter(mContext, R.layout.places_item, mFoodPlaces, true);
                        mPlacesList.setAdapter(placesAdapter);
                    } else {
                        isLoopCompete++;
                        FoodRequest();
                    }
                }
            } catch (Exception exp) {
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
                AlertDialog.Builder aDialogBuilder = new AlertDialog.Builder(SelectionActivity.this);
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
            if (isFooterItem) {
                position = mGooglePlaces.size() - 1;
                Log.d("POSITION", "parseResult: " + position);
            }
            try {

                if (response.has("next_page_token"))
                    Utility.setNextPageToken(response.getString("next_page_token"));
                else
                    Utility.setNextPageToken("NA");

                JSONArray jsonArray = response.getJSONArray("results");

                if (response.getString("status").compareTo("OK") == 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsObject = jsonArray.getJSONObject(i);
                        if (Utility.mSelectedPlaces.size() > 0) {
                            Log.d("Result", Utility.CheckPlaceID(jsObject.getString("place_id"), Utility.mSelectedPlaces) + "");
                            if (Utility.CheckPlaceID(jsObject.getString("place_id"), Utility.mSelectedPlaces)) {
                                AddGooglePlaces(jsObject);
                            }
                        } else {
                            AddGooglePlaces(jsObject);
                        }
                    }

                    if (!isFooterItem)
                        sort(mGooglePlaces);

                    if (isFooterItem) {
                        PlacesAdapter placesAdapter = (PlacesAdapter) mPlacesList.getAdapter();
                        placesAdapter.notifyDataSetChanged();
                        mPlacesList.smoothScrollToPosition(position + 1);

                    } else {
                        mPlacesList.setAdapter(null);
                        PlacesAdapter placesAdapter = new PlacesAdapter(mContext, R.layout.places_item, mGooglePlaces, true);
                        mPlacesList.setAdapter(placesAdapter);
                    }

                    process = "Success";
                } else {

                    mPlacesList.setAdapter(null);
                    Toast.makeText(mContext, "Oops, Could not find anything near you", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Log.d("JSON", "ErrorParsing " + e.getMessage());
                Toast.makeText(SelectionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    }

    private void ShowWaitTimeAlert(final int position, final PlacesAdapter placesAdapter) {

        final Dialog dialog = new Dialog(SelectionActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(SelectionActivity.this).inflate(R.layout.alert_view, null);
        dialog.setContentView(view);
        dialog.findViewById(R.id.button_holder).setVisibility(View.VISIBLE);
        TextView textView = dialog.findViewById(R.id.alert_title);
        textView.setText("Tour Time");
        TextView textView1 = dialog.findViewById(R.id.alert_message);
        textView1.setText("Enter, how much time you want to spend here");
        final EditText editText = dialog.findViewById(R.id.edit_alert);
        TextView btnPositive = dialog.findViewById(R.id.btn_positive);
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().length() == 0) {
                    Snackbar.make(v, "Please, Enter Time", Snackbar.LENGTH_SHORT).show();
                    editText.setText("");
                    editText.requestFocus();
                } else if (Integer.parseInt(editText.getText().toString()) == 0) {
                    Snackbar.make(v, "Please, Enter Time Valid Time", Snackbar.LENGTH_SHORT).show();
                    editText.setText("");
                    editText.requestFocus();
                } else {
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
                if (dialog.isShowing())
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
        if (Utility.mSelectedPlaces.size() > 0) {
            GooglePlaces gplace = Utility.mSelectedPlaces.get(Utility.mSelectedPlaces.size() - 1);
            Location glocation = new Location("");
            glocation.setLatitude(gplace.getgPlaceLocation().latitude);
            glocation.setLongitude(gplace.getgPlaceLocation().longitude);
            googlePlaces.setDistanceFromSource(GetDistance(latLng, glocation));
        } else {
            googlePlaces.setDistanceFromSource(GetDistance(latLng, mLocation));
        }
        googlePlaces.setTimeToTravel(0);

        googlePlaces.setDayno(-1);
        googlePlaces.setSrno(-1);

        if (GetDistance(latLng, mLocation) < 5) {
            Log.d("BOOLEAN", "AddGooglePlaces: " + ifItemIsAdded(jsObject.getString("place_id")));
            if (ifItemIsAdded(jsObject.getString("place_id")))
                mGooglePlaces.add(googlePlaces);
        }
    }

    private boolean ifItemIsAdded(@NonNull String placeId) {
        boolean check = true;
        if (selected_position == 0) {
            for (int i = 0; i < mFoodPlaces.size(); i++) {
                if (mFoodPlaces.get(i).getgPlaceId().compareTo(placeId) == 0) {
                    Log.d("CHECK", mFoodPlaces.get(i).getgPlaceId() + " - " + mFoodPlaces.get(i).getgPlaceName() + " - " + placeId);
                    check = false;
                    break;
                }
            }
        } else {
            for (int i = 0; i < mGooglePlaces.size(); i++) {
                if (mGooglePlaces.get(i).getgPlaceId().compareTo(placeId) == 0) {
                    Log.d("GOOGLE", mFoodPlaces.get(i).getgPlaceId() + " - " + mFoodPlaces.get(i).getgPlaceName() + " - " + placeId);
                    check = false;
                    break;
                }
            }
        }
        return check;
    }

    private float GetRadius(int numberOfHours) {
//        if (minutes == numberOfHours * 60) {
//            return Float.parseFloat(new DecimalFormat("#0.0#").format((numberOfHours * 1000)));
//        } else {
//            return Float.parseFloat(new DecimalFormat("#0.0#").format((minutes / 60) * 1000));
//        }
        return 5000;
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
                myViewHolder.card = convertView.findViewById(R.id.card);
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
            Log.d("ADAPTER", "getView: " + aGooglePlaces.get(position).getgPlaceRating() + " - " + position);
            myViewHolder.place_rating.setText(aGooglePlaces.get(position).getgPlaceRating() + "");
            myViewHolder.place_distance.setText(decimalFormat.format(aGooglePlaces.get(position).getDistanceFromSource()) + " km");
            myViewHolder.rating.setRating(aGooglePlaces.get(position).getgPlaceRating());
            myViewHolder.place_time.setText(aGooglePlaces.get(position).getTimeToTravel() + " min");

            if (myViewHolder.place_rating.getVisibility() == View.GONE)
                myViewHolder.place_rating.setVisibility(View.VISIBLE);

            if (myViewHolder.place_rating.getText().toString().length() <= 0)
                Toast.makeText(aContext, aGooglePlaces.get(position).getgPlaceName() + "", Toast.LENGTH_SHORT).show();

            if (myViewHolder.rating.getVisibility() == View.GONE)
                myViewHolder.rating.setVisibility(View.VISIBLE);

            myViewHolder.selection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectedPosition = position;
                    ShowWaitTimeAlert(mSelectedPosition, PlacesAdapter.this);
                }
            });

            myViewHolder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i=new Intent(SelectionActivity.this,PlaceDetailActivity.class);
                    i.putExtra("place",aGooglePlaces.get(position));
                    startActivity(i);
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
            CardView card;
        }
    }

    private void removeFromList(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SelectionActivity.this);
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
                minutes = minutes + (Utility.mSelectedPlaces.get(position).getTimeToTravel() + Utility.mSelectedPlaces.get(position).getTimeSpent());
                Utility.mSelectedPlaces.remove(position);
                mPlaceAdapter.notifyDataSetChanged();
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

                Utility.mSelectedPlaces.add(mFoodPlaces.get(selection));
                Utility.mRemovedPlaces.add(mFoodPlaces.get(selection));
                mFoodPlaces.remove(selection);

                Toast.makeText(SelectionActivity.this, "Place Added", Toast.LENGTH_SHORT).show();

//                Log.d("LIST_P", "S - " + Utility.mSelectedPlaces.size() + " R - " + Utility.mRemovedPlaces.size() + "  O- " + mGooglePlaces.size());
            } else {
                Toast.makeText(this, "Can't add this place, as No time Left to Get to this point and Return Back", Toast.LENGTH_SHORT).show();
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
                Utility.mSelectedPlaces.add(mGooglePlaces.get(selection));
                Utility.mRemovedPlaces.add(mGooglePlaces.get(selection));
                mGooglePlaces.remove(selection);

                Toast.makeText(SelectionActivity.this, "Place Added", Toast.LENGTH_SHORT).show();

//                Log.d("LIST_P", "S - " + Utility.mSelectedPlaces.size() + " R - " + Utility.mRemovedPlaces.size() + "  O- " + mGooglePlaces.size());
            } else {
                Toast.makeText(this, "Can't add this place, as No time Left to Get to this point and Return Back", Toast.LENGTH_SHORT).show();
                Log.d("TimeD", "MinR - " + getTimeToTravel(googlePlaces.getDistanceFromSource()) + " - MinLeft - " + minutes + " TimeSpent - " + time);
            }
        }

        mPlaceAdapter.notifyDataSetChanged();
    }

    private boolean checkReturnTimeAvailable(GooglePlaces googlePlaces, boolean isTimeSpentCheck, int timeSpent) {
        double distance = 0.0;
        if (Utility.mSelectedPlaces.size() > 0) {
            GooglePlaces gplace = Utility.mSelectedPlaces.get(Utility.mSelectedPlaces.size() - 1);
            Location glocation = new Location("");
            glocation.setLatitude(gplace.getgPlaceLocation().latitude);
            glocation.setLongitude(gplace.getgPlaceLocation().longitude);
            distance = GetDistance(googlePlaces.getgPlaceLocation(), glocation);
        } else {
            distance = GetDistance(googlePlaces.getgPlaceLocation(), mLocation);
        }
        double min = getTimeToTravel(distance);
        Log.d("TimeD", "MinR - " + min + " - MinLeft - " + minutes + " TimeSpent - " + getTimeSpentHere(selected_position));
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
        if (km == 0 || km == 1) {
            return 2;
        } else
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

    public static double GetDistance(LatLng latLng, Location location) {
        double km = 1000;
        Location nLocation = new Location("");
        nLocation.setLatitude(latLng.latitude);
        nLocation.setLongitude(latLng.longitude);
        Log.d("Distance", ((double) location.distanceTo(nLocation)) + "");
        return ((double) location.distanceTo(nLocation)) / km;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        else if(item.getItemId()==R.id.weather)
        {
            WeatherDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utility.mSelectedPlaces.clear();
    }

    public void WeatherDialog()
    {
        final Dialog d=new Dialog(SelectionActivity.this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.weatherdialog);
        TextView Pstatus, Ptemp, Phumidity, Pwind;
        Pstatus = (TextView)d.findViewById(R.id.pd_status);
        Ptemp = (TextView) d.findViewById(R.id.pd_temp);
        Phumidity = (TextView) d.findViewById(R.id.pd_humidity);
        Pwind = (TextView) d.findViewById(R.id.pd_wind);
        ImageView close=d.findViewById(R.id.close);

        String a = "<b>Status: </b>" + weather;
        Pstatus.setText(Html.fromHtml(a));

        String a1 = "<b>Temperature: </b>" + temp_String +"°";
        Ptemp.setText(Html.fromHtml(a1));

        String a2 = "<b>Humidity: </b>" + humidity;
        Phumidity.setText(Html.fromHtml(a2));

        String a3 = "<b>Wind: </b>" + wind_string;
        Pwind.setText(Html.fromHtml(a3));

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.cancel();
            }
        });

        if(weather.length()>0) {
            d.show();
        }
        else
        {
            calculate_weather();
            Toast.makeText(this, "Somthing went wrong, try again", Toast.LENGTH_SHORT).show();
        }
    }

    public void calculate_weather() {
        String url="http://api.openweathermap.org/data/2.5/weather?";
        String furl = url +"lat="+mLocation.getLatitude() + "&lon=" + mLocation.getLongitude() +"&"+getString(R.string.appid);
        Log.d("WEATHERRESPONSE",furl);

        JsonObjectRequest jor = new JsonObjectRequest(furl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject main = response;
                    JSONArray weatherarr=main.getJSONArray("weather");
                    JSONObject joj=weatherarr.getJSONObject(0);
                    weather = joj.getString("description");

                    JSONObject Jmain=main.getJSONObject("main");
                    double t=Double.parseDouble(Jmain.getString("temp"));
                    t=t- 273.15;
                    temp_String = new DecimalFormat("##.00").format(t);
                    humidity = Jmain.getString("humidity");

                    JSONObject Jwind=main.getJSONObject("wind");
                    wind_string = Jwind.getString("speed");

                } catch (Exception e) {
//                    Toast.makeText(SelectionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(SelectionActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(SelectionActivity.this);
        requestQueue.add(jor);
    }
}
