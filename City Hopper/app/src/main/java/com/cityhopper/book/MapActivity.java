package com.cityhopper.book;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.PolylineOptions;
import com.cityhopper.book.helper.UtilConstants;
import com.cityhopper.book.helper.Utility;
import com.cityhopper.book.pojo.GooglePlaces;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

import static com.cityhopper.book.city.SelectionActivity.REQUEST_CHECK_SETTINGS;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MarkerLoad";
    private SharedPreferences sharedPreferences;
    private String UID;

    public static final int PERMISSION_REQUEST_CODE = 1002;
    private GoogleMap gMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private ImageView currentLocation;
    private Location mLocation, mCurrentLocation;

    private ImageView btnNext, btnPrevious, btnNext_Days, btnPrevious_Days;
    private TextView txtGPlaceName, txtDays;
    private Dialog mDialog;
    private Button btn_SubmitPlan;
    private int markerPosition = 0;
    private int dayMarker = 0;

    private ArrayList<GooglePlaces> mLocalGooglePlaces;
    private ArrayList<String> dayNo;
    private ArrayList<ArrayList<GooglePlaces>> mDaysPlan;

    private Context infoContext = MapActivity.this;

    //Destination Plan data
    private static boolean isDayPlan;
    private String destination = "";
    private String noofdays = "";
    private String type = "";
    private boolean buttonShow = false;
    private boolean isNavigation;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        sharedPreferences = getSharedPreferences(UtilConstants.SharedPref, Context.MODE_PRIVATE);
        UID = sharedPreferences.getString(UtilConstants.UID, "");

        getSupportActionBar().setTitle("City Plan");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDialog = new Dialog(MapActivity.this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.loading_dialog);
        mDialog.setCancelable(false);

        Intent intent = getIntent();
        isDayPlan = intent.getBooleanExtra(UtilConstants.ISDAYSPLAN, false);
        buttonShow = intent.getBooleanExtra(UtilConstants.SHOWBUTTON, false);
        isNavigation = intent.getBooleanExtra(UtilConstants.ISNAVIGATION, false);

        if (isDayPlan) {
            destination = sharedPreferences.getString(UtilConstants.DESTINATION, "");
            noofdays = sharedPreferences.getInt(UtilConstants.DAYS, -1) + "";
            type = sharedPreferences.getString(UtilConstants.TYPE, "");
            findViewById(R.id.holder_buttons_days).setVisibility(View.VISIBLE);
        }

        findViewById(R.id.holder_items).setVisibility(View.VISIBLE);
        btn_SubmitPlan = findViewById(R.id.btn_Map);
        currentLocation = findViewById(R.id.btnCurrentLocation);

        //Normal Places Switch
        txtGPlaceName = findViewById(R.id.selectedItem);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);

        //Days switch
        btnNext_Days = findViewById(R.id.btnNext_days);
        btnPrevious_Days = findViewById(R.id.btnPrevious_days);
        txtDays = findViewById(R.id.selectedItem_days);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);
        mLocationRequest = createLocationRequest();

        currentLocation.setVisibility(View.GONE);

        if (buttonShow)
            btn_SubmitPlan.setVisibility(View.VISIBLE);

        btn_SubmitPlan.setText("Submit Plan");

    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }


    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult.getLastLocation() != null) {
                Log.d("Location", "Location Not Null");
                mCurrentLocation = locationResult.getLastLocation();
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);

            } else {
                Log.d("Location", "Location is Null");
            }

        }

        ;
    };

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    @Override
    protected void onStart() {
        super.onStart();

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (markerPosition == mLocalGooglePlaces.size() - 1) {
                    if (dayNo != null && dayNo.size() > 0) {
                        markerPosition = 0;
                    } else {
                        markerPosition = 0;
                    }
                    ShowMarker();
                } else {
                    markerPosition = markerPosition + 1;
                    ShowMarker();
                }
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (markerPosition == 0) {
                    markerPosition = mLocalGooglePlaces.size() - 1;
                    ShowMarker();
                } else {
                    markerPosition = markerPosition - 1;
                    ShowMarker();
                }
            }
        });

        btnNext_Days.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dayMarker == dayNo.size() - 1) {
                    dayMarker = 0;
                    LoadMarker();
                } else {
                    dayMarker++;
                    LoadMarker();
                }
            }
        });

        btnPrevious_Days.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dayMarker == 0) {
                    dayMarker = dayNo.size() - 1;
                    LoadMarker();
                } else {
                    dayMarker--;
                    LoadMarker();
                }
            }
        });

        btn_SubmitPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String dest,String type,String nodays,String uid,ArrayList<String> dayno
                // ,ArrayList<String> srno,ArrayList<String> pname,ArrayList<String> latlng
                // ,ArrayList<String> rating,ArrayList<String> placeid

                if (isDayPlan) {
                    prepareList();
                } else {

                    for (GooglePlaces googlePlaces : mLocalGooglePlaces) {
                        Log.d("PLACE", googlePlaces.getgPlaceId() + " \n " +
                                googlePlaces.getgPlaceName() + " \n " +
                                googlePlaces.getgPlaceLocation().latitude + " , " + googlePlaces.getgPlaceLocation().longitude + "\n" +
                                googlePlaces.getgPlaceRating() + " \n " +
                                googlePlaces.getDayno() + " \n " +
                                googlePlaces.getSrno());
                    }
                    AddTour addTour = new AddTour(MapActivity.this, mLocalGooglePlaces);
                    addTour.execute("City", "NA", "-1", UID);
                }


            }
        });
    }

    private void prepareList() {
        ArrayList<GooglePlaces> gPlaces = new ArrayList<GooglePlaces>();

        for (int i = 0; i < mDaysPlan.size(); i++) {
            ArrayList<GooglePlaces> plc;
            plc = mDaysPlan.get(i);
            GooglePlaces googlePlaces = plc.get(0);
            googlePlaces.setDayno(1);
            plc.set(0, googlePlaces);
            if (i != 0) {
                plc.remove(0);
            }
            gPlaces.addAll(plc);
        }

        for (GooglePlaces googlePlaces : gPlaces) {
            Log.d("PLACE", googlePlaces.getgPlaceId() + " \n " +
                    googlePlaces.getgPlaceName() + " \n " +
                    googlePlaces.getgPlaceLocation().latitude + " , " + googlePlaces.getgPlaceLocation().longitude + "\n" +
                    googlePlaces.getgPlaceRating() + " \n " +
                    googlePlaces.getDayno() + " \n " +
                    googlePlaces.getSrno());
        }

        AddTour addTour = new AddTour(MapActivity.this, gPlaces);
        addTour.execute(destination, type, noofdays, UID);

    }

    private void LoadMarker() {
        if (dayNo.size() > 0) {
            mLocalGooglePlaces = new ArrayList<GooglePlaces>();
            ArrayList<GooglePlaces> places = mDaysPlan.get(dayMarker);
            mLocalGooglePlaces.addAll(places);
            if (dayMarker == 0 || dayMarker == dayNo.size() - 1) {
                plotMyDayRoute(mLocalGooglePlaces);
            } else {
                mLocalGooglePlaces.remove(0);
                plotMyDayRoute(mLocalGooglePlaces);
            }

            Log.d(TAG, String.format("DayMaker - %d, LoadMarker - %d, SizeofList - %d"
                    , dayMarker, markerPosition, mLocalGooglePlaces.size()));
            txtDays.setText(dayNo.get(dayMarker).replace("<br>", ""));
        }
    }

    private int GetPosition(LatLng position) {
        int pos = -1;
        for (int i = 0; i < mLocalGooglePlaces.size(); i++) {
            if (mLocalGooglePlaces.get(i).getgPlaceLocation().equals(position)) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    private synchronized void ShowMarker() {
        if (mLocalGooglePlaces.size() > 0) {
            GooglePlaces googlePlaces = mLocalGooglePlaces.get(markerPosition);
            if (markerPosition == 0) {
                if (dayNo != null && dayNo.size() > 0) {
                    if (dayMarker == 0 || dayMarker == dayNo.size() - 1) {
                        txtGPlaceName.setText("My Location");
                    } else {
//                        Toast.makeText(MapActivity.this, markerPosition + "", Toast.LENGTH_SHORT).show();
                        txtGPlaceName.setText((markerPosition + 1) + ". " + googlePlaces.getgPlaceName());
                    }
                } else {
                    txtGPlaceName.setText("My Location");
                }

            } else {
                if (dayNo != null && dayNo.size() > 0) {
                    if (dayMarker == 0 || dayMarker == dayNo.size() - 1) {
//                        Toast.makeText(MapActivity.this, markerPosition + "", Toast.LENGTH_SHORT).show();
                        txtGPlaceName.setText((markerPosition) + ". " + googlePlaces.getgPlaceName());
                    } else {
                        txtGPlaceName.setText((markerPosition + 1) + ". " + googlePlaces.getgPlaceName());
                    }
                } else {
                    txtGPlaceName.setText((markerPosition) + ". " + googlePlaces.getgPlaceName());
                }

            }
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(googlePlaces.getgPlaceLocation(), 18));
        }

        if (isDayPlan) {
            if (mLocalGooglePlaces.size() == 1)
                Toast.makeText(MapActivity.this, "This Day is Spend in Travelling", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                GetMyLocation();
            } else {
                ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void GetMyLocation() {
        if (isDayPlan) {
            mDaysPlan = new ArrayList<ArrayList<GooglePlaces>>();
            mDaysPlan.addAll(Utility.getMainCollection());

            dayNo = new ArrayList<String>();
            dayNo.addAll(Utility.getDayNo());

            for (ArrayList<GooglePlaces> place : mDaysPlan) {
                for (GooglePlaces places : place) {
                    Log.d("THIS", places.getgPlaceName() + "\n" + places.getgPlaceId());
                }
            }

            LoadMarker();

        } else {
            mLocalGooglePlaces = new ArrayList<GooglePlaces>(Utility.mUpdatedPlaces);
            Collections.copy(mLocalGooglePlaces, Utility.mUpdatedPlaces);

            for(GooglePlaces googlePlaces : Utility.mUpdatedPlaces){
                Log.d("BEFORE", googlePlaces.getgPlaceId()+" - "
                        +googlePlaces.getgPlaceName());
            }

            plotMyRoute(mLocalGooglePlaces);
        }


    }

    private void plotMyRoute(ArrayList<GooglePlaces> googlePlaces) {

        gMap.clear();
        GooglePlaces src = googlePlaces.get(0);
        LatLng latLng = src.getgPlaceLocation();
        MarkerOptions mo = new MarkerOptions();
        mo.title("Start & End Position");
        mo.position(latLng);
        mo.draggable(false);
        mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        gMap.addMarker(mo);
        Location location = new Location("");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        mLocation = location;
        if (googlePlaces.size() > 0) {
            PolylineOptions polylineOptions;
            for (int i = 1; i < googlePlaces.size(); i++) {
                GooglePlaces gPlaces = googlePlaces.get(i);
                mo = new MarkerOptions();
                mo.title(i + ". " + gPlaces.getgPlaceName());
                mo.position(gPlaces.getgPlaceLocation());
                mo.draggable(false);
                mo.snippet(GetFormatedSnippet(gPlaces));
                mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                gMap.addMarker(mo);

                if (i == 1) {
                    polylineOptions = new PolylineOptions();
                    polylineOptions.color(0xFFA3B9CD);
                    polylineOptions.add(latLng, gPlaces.getgPlaceLocation());
                    polylineOptions.width(5);
                    gMap.addPolyline(polylineOptions);
                    Log.d("LINE", i + "");
                } else if (i == googlePlaces.size() - 1) {
                    GooglePlaces ppPlace = googlePlaces.get(i - 1);
                    GooglePlaces pPlace = googlePlaces.get(i);
                    polylineOptions = new PolylineOptions();
                    polylineOptions.color(0xFFA3B9CD);
                    polylineOptions.add(pPlace.getgPlaceLocation(), ppPlace.getgPlaceLocation());
                    polylineOptions.add(pPlace.getgPlaceLocation(), latLng);
                    polylineOptions.width(5);
                    gMap.addPolyline(polylineOptions);
                    Log.d("LINE", i + "");
                } else {
                    GooglePlaces pPlace = googlePlaces.get(i - 1);
                    polylineOptions = new PolylineOptions();
                    polylineOptions.color(Color.LTGRAY);
                    polylineOptions.add(pPlace.getgPlaceLocation(), gPlaces.getgPlaceLocation());
                    polylineOptions.width(5);
                    gMap.addPolyline(polylineOptions);
                    Log.d("LINE", i + " - " + (i - 1));
                }

            }
        } else {
            Toast.makeText(this, "Size is Zero", Toast.LENGTH_SHORT).show();
        }


        gMap.setInfoWindowAdapter(infoWindowAdapter);
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));

        ShowMarker();
    }

    private void plotMyDayRoute(ArrayList<GooglePlaces> googlePlaces) {
        gMap.clear();
        MarkerOptions mo = new MarkerOptions();
        LatLng latLng = null;
        if (dayMarker == 0 || dayMarker == dayNo.size() - 1) {
            GooglePlaces src = googlePlaces.get(0);
            latLng = src.getgPlaceLocation();
            mo.title("Start & End Position");
            mo.position(latLng);
            mo.draggable(false);
            mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            gMap.addMarker(mo);
            Location location = new Location("");
            location.setLatitude(latLng.latitude);
            location.setLongitude(latLng.longitude);
            mLocation = location;
            if (googlePlaces.size() > 0) {
                PolylineOptions polylineOptions;
                for (int i = 1; i < googlePlaces.size(); i++) {
                    GooglePlaces gPlaces = googlePlaces.get(i);
                    mo = new MarkerOptions();
                    mo.title(i + ". " + gPlaces.getgPlaceName());
                    mo.position(gPlaces.getgPlaceLocation());
                    mo.draggable(false);
                    mo.snippet(GetFormatedSnippet(gPlaces));
                    mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    gMap.addMarker(mo);
                    if (i == 1) {
                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.BLUE);
                        polylineOptions.add(latLng, gPlaces.getgPlaceLocation());
                        polylineOptions.width(5);
                        gMap.addPolyline(polylineOptions);
                        Log.d("LINE", i + "");
                    } else if (i == googlePlaces.size() - 1) {
                        GooglePlaces ppPlace = googlePlaces.get(i - 1);
                        GooglePlaces pPlace = googlePlaces.get(i);
                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.BLUE);
                        polylineOptions.add(pPlace.getgPlaceLocation(), ppPlace.getgPlaceLocation());
                        polylineOptions.add(pPlace.getgPlaceLocation(), latLng);
                        polylineOptions.width(5);
                        gMap.addPolyline(polylineOptions);
                        Log.d("LINE", i + "");
                    } else {
                        GooglePlaces pPlace = googlePlaces.get(i - 1);
                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.BLUE);
                        polylineOptions.add(pPlace.getgPlaceLocation(), gPlaces.getgPlaceLocation());
                        polylineOptions.width(5);
                        gMap.addPolyline(polylineOptions);
                        Log.d("LINE", i + " - " + (i - 1));
                    }
                }
            }
        } else {
            if (googlePlaces.size() > 0) {
                PolylineOptions polylineOptions;
                GooglePlaces src = googlePlaces.get(0);
                latLng = src.getgPlaceLocation();
                mo.title(1 + ". " + src.getgPlaceName());
                mo.position(latLng);
                mo.draggable(false);
                mo.snippet(GetFormatedSnippet(src));
                mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                gMap.addMarker(mo);
                Location location = new Location("");
                location.setLatitude(latLng.latitude);
                location.setLongitude(latLng.longitude);
                mLocation = location;
                for (int i = 1; i < googlePlaces.size(); i++) {
                    GooglePlaces gPlaces = googlePlaces.get(i);
                    mo = new MarkerOptions();
                    mo.title(i + 1 + ". " + gPlaces.getgPlaceName());
                    mo.position(gPlaces.getgPlaceLocation());
                    mo.draggable(false);
                    mo.snippet(GetFormatedSnippet(gPlaces));
                    mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    gMap.addMarker(mo);
                    if (i == 1) {
                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.BLUE);
                        polylineOptions.add(latLng, gPlaces.getgPlaceLocation());
                        polylineOptions.width(5);
                        gMap.addPolyline(polylineOptions);
                        Log.d("LINE", i + "");
                    } else if (i == googlePlaces.size() - 1) {
                        GooglePlaces ppPlace = googlePlaces.get(i - 1);
                        GooglePlaces pPlace = googlePlaces.get(i);
                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.BLUE);
                        polylineOptions.add(pPlace.getgPlaceLocation(), ppPlace.getgPlaceLocation());
                        polylineOptions.add(pPlace.getgPlaceLocation(), latLng);
                        polylineOptions.width(5);
                        gMap.addPolyline(polylineOptions);
                        Log.d("LINE", i + "");
                    } else {
                        GooglePlaces pPlace = googlePlaces.get(i - 1);
                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.BLUE);
                        polylineOptions.add(pPlace.getgPlaceLocation(), gPlaces.getgPlaceLocation());
                        polylineOptions.width(7);
                        gMap.addPolyline(polylineOptions);
                        Log.d("LINE", i + " - " + (i - 1));
                    }

                }
            }
        }

        gMap.setInfoWindowAdapter(infoWindowAdapter);
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));

        ShowMarker();
    }

    public GoogleMap.InfoWindowAdapter infoWindowAdapter = new GoogleMap.InfoWindowAdapter() {
        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            Context context = infoContext; //or getActivity(), YourActivity.this, etc.

            LinearLayout info = new LinearLayout(context);
            info.setOrientation(LinearLayout.VERTICAL);

            TextView title = new TextView(context);
            title.setTextColor(Color.BLACK);
            title.setGravity(Gravity.CENTER);
            title.setTypeface(null, Typeface.BOLD);
            title.setText(marker.getTitle());

            TextView snippet = new TextView(context);
            snippet.setTextColor(Color.GRAY);
            snippet.setText(marker.getSnippet());

            info.addView(title);
            info.addView(snippet);

            return info;
        }
    };

    private String GetFormatedSnippet(GooglePlaces gPlaces) {
        DecimalFormat decimalFormat = new DecimalFormat("#0.0#");
        String time = "Time : " + gPlaces.getTimeToTravel() + " min";
        time = time.concat("\nDistance : " + decimalFormat.format(gPlaces.getDistanceFromSource()) + " km");
        return time;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                markerPosition = GetPosition(marker.getPosition());
                ShowMarker();
                marker.showInfoWindow();
                return true;
            }
        });

        gMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
//                Toast.makeText(MapActivity.this, isNavigation+"", Toast.LENGTH_SHORT).show();
                if (isNavigation) {
                    if (mCurrentLocation != null) {
                        LatLng latLng = marker.getPosition();
                        String url = "http://maps.google.com/maps?saddr=" + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude() + "&daddr=" + latLng.latitude + "," + latLng.longitude;
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse(url));
                        startActivity(intent);
                    } else {
                        Toast.makeText(MapActivity.this, "Current Location is Null", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        GetMyLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utility.mUpdatedPlaces.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
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
                            status.startResolutionForResult(MapActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            showSettingDialog();
        } else {
            ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                startLocationUpdates();
            } else
                showSettingDialog();

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
