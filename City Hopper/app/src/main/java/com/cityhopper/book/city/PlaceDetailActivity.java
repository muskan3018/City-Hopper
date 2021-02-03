package com.cityhopper.book.city;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.cityhopper.book.R;
import com.cityhopper.book.helper.MySingleton;
import com.cityhopper.book.helper.Utility;
import com.cityhopper.book.pojo.GooglePlaces;

import org.json.JSONArray;
import org.json.JSONObject;

public class PlaceDetailActivity extends AppCompatActivity  implements OnMapReadyCallback {

    Boolean Infoisopen=true;
    private Dialog mDialog;
    RelativeLayout InfoWindow;
    private GoogleMap gMap;
    GooglePlaces gp=null;
    String name="",icon="",contact="",address="",rating="",isopen="",timings="",website="";
    TextInputLayout NameT,RatingT,AddressT,ContactT,IsopenT,TimingsT,WebsiteT;
    TextInputEditText NameTxt,RatingTxt,AddressTxt,ContactTxt,IsopenTxt,TimingsTxt,WebsiteTxt;
    ImageView close,open;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placedetail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        init();

        gp=getIntent().getParcelableExtra("place");
        getSupportActionBar().setTitle(gp.getgPlaceName());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        String query= Utility.GenerateDetailsQueryAll(gp.getgPlaceId());
        Log.d("RESPONSE",query);
        FoodResponse(query);
    }

    public void init()
    {
        close=findViewById(R.id.close);
        open=findViewById(R.id.open);
        NameT=findViewById(R.id.name);
        RatingT=findViewById(R.id.rating);
        AddressT=findViewById(R.id.address);
        ContactT=findViewById(R.id.contact);
        IsopenT=findViewById(R.id.isopen);
        TimingsT=findViewById(R.id.timings);
        WebsiteT=findViewById(R.id.website);

        NameTxt=findViewById(R.id.nametxt);
        RatingTxt=findViewById(R.id.ratingtxt);
        AddressTxt=findViewById(R.id.addresstxt);
        ContactTxt=findViewById(R.id.contacttxt);
        IsopenTxt=findViewById(R.id.isopentxt);
        TimingsTxt=findViewById(R.id.timingstxt);
        WebsiteTxt=findViewById(R.id.websitetxt);
        InfoWindow=findViewById(R.id.ray);

        mDialog = new Dialog(PlaceDetailActivity.this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.loading_dialog);
        mDialog.setCancelable(false);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Infoisopen=false;
                InfoWindow.setVisibility(View.GONE);
                open.setVisibility(View.VISIBLE);
            }
        });

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Infoisopen=true;
                InfoWindow.setVisibility(View.VISIBLE);
                open.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(Infoisopen)
        {
            close.performClick();
        }else
        {
            finish();
        }
    }

    private void FoodResponse(@NonNull String query) {
        mDialog.show();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, query, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("RESPONSE", response.toString());
                        ParseJSON(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mDialog.dismiss();
                        Log.d("RESPONSE", error.getMessage());
                    }
                });
        MySingleton.getInstance(PlaceDetailActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    public void ParseJSON(JSONObject response)
    {
        try
        {
            String status=response.getString("status");
            Log.d("RESPONSE",status);
            if(status.equals("OK"))
            {
                Log.d("RESPONSE","inside");
                JSONObject result=response.getJSONObject("result");

                try
                {
                    name=result.getString("name");
                    NameTxt.setText(name);
                }
                catch (Exception e){
                    NameT.setVisibility(View.GONE);
                }


                try
                {
                    address=result.getString("formatted_address");
                    if(address!=null && address.length()>0)
                    {
                        AddressTxt.setText(address);
                    }
                    else
                    {
                        AddressT.setVisibility(View.GONE);
                    }
                }
                catch (Exception e){AddressT.setVisibility(View.GONE);}


                try
                {
                    contact=result.getString("formatted_phone_number");
                    if(contact!=null && contact.length()>0)
                    {
                        ContactTxt.setText(contact);
                    }
                    else
                    {
                        ContactT.setVisibility(View.GONE);
                    }
                }
                catch (Exception e){ ContactT.setVisibility(View.GONE);}


                try
                {
                    rating=result.getDouble("rating")+"";
                    if(rating!=null && rating.length()>0)
                    {
                        RatingTxt.setText("Overall Ratings : "+rating);
                    }
                    else
                    {
                        RatingT.setVisibility(View.GONE);
                    }

                }
                catch (Exception e){RatingT.setVisibility(View.GONE);}

                try
                {

                    try
                    {
                        JSONObject open=result.getJSONObject("opening_hours");
                        isopen=open.getBoolean("open_now")+"";
                        if(isopen!=null && isopen.length()>0)
                        {
                            String o=isopen.equals("true")?"Opened":"Closed";
                            IsopenTxt.setText("The Place is "+o);
                        }
                        else
                        {
                            IsopenT.setVisibility(View.GONE);
                        }

                    }
                    catch (Exception e){IsopenT.setVisibility(View.GONE);}


                    try
                    {
                        JSONObject open=result.getJSONObject("opening_hours");
                        JSONArray time=open.getJSONArray("weekday_text");
                        if(time!=null)
                        {
                            String ftime="";
                            for(int i=0;i<time.length();i++)
                            {
                                ftime+=time.getString(i)+"\n";
                            }
                            if(ftime.length()>0)
                            {
                                ftime.substring(0,ftime.length()-2);
                                timings=ftime;
                                TimingsTxt.setText(ftime);
                            }
                        }
                        else
                        {
                            TimingsT.setVisibility(View.GONE);
                        }
                    }
                    catch (Exception e){TimingsT.setVisibility(View.GONE);}
                }
                catch (Exception e){
                    IsopenT.setVisibility(View.GONE);
                    TimingsT.setVisibility(View.GONE);
                }

                try
                {
                    website=response.getString("website");
                    if(website!=null && website.length()>0)
                    {
                        WebsiteTxt.setText(website);
                    }
                    else
                    {
                        WebsiteT.setVisibility(View.GONE);
                    }
                }
                catch (Exception e){WebsiteT.setVisibility(View.GONE);}

            }
            else
            {
                Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        mDialog.dismiss();
        InfoWindow.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        MarkerOptions mo = new MarkerOptions();
        mo.title(gp.getgPlaceName());
        mo.position(gp.getgPlaceLocation());
        mo.draggable(false);
        mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        gMap.addMarker(mo);
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gp.getgPlaceLocation(), 18));
    }
}
