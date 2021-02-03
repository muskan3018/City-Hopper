package com.cityhopper.book.city;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cityhopper.book.MapActivity;
import com.cityhopper.book.R;
import com.cityhopper.book.helper.GreedyAlgorithm;
import com.cityhopper.book.helper.UtilConstants;
import com.cityhopper.book.helper.Utility;
import com.cityhopper.book.pojo.GooglePlaces;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

public class SortActivity extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private String UID = "";
    private DecimalFormat decimalFormat = new DecimalFormat("#00.0#");

    private PlacesAdapter mPlaceAdapter;

    private ListView list_sort;
    private Button btn_continue, btn_clear;

    private ArrayList<GooglePlaces> mPlacesSelected;
    private ArrayList<GooglePlaces> mOrderedPlaces;
    private ArrayList<Integer> mSelectedItem;
    private ArrayList<Boolean> mSelected;
    private int mCount = 0;
    private GreedyAlgorithm greedyAlgorithm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sort);

        sharedPref = getSharedPreferences(UtilConstants.SharedPref, Context.MODE_PRIVATE);
        UID = sharedPref.getString(UtilConstants.UID, "");

        getSupportActionBar().setTitle("Sort Places");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        list_sort = findViewById(R.id.list_sort);
        btn_clear = findViewById(R.id.btn_sort_cancel);
        btn_continue = findViewById(R.id.btn_sort_submit);

        mPlacesSelected = new ArrayList<GooglePlaces>(Utility.mSelectedPlaces);

        mOrderedPlaces = new ArrayList<GooglePlaces>();

        Collections.copy(mPlacesSelected, Utility.mSelectedPlaces);

        mSelectedItem = new ArrayList<>(Collections.nCopies(Utility.mSelectedPlaces.size(), 0));
        mSelected = new ArrayList<>(Collections.nCopies(Utility.mSelectedPlaces.size(), false));

        mPlaceAdapter = new PlacesAdapter(SortActivity.this, R.layout.places_item, mPlacesSelected, false);
        list_sort.setAdapter(mPlaceAdapter);



    }

    @Override
    protected void onStart() {
        super.onStart();

        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCount = 0;
                mOrderedPlaces = new ArrayList<GooglePlaces>();
                mSelectedItem = new ArrayList<>(Collections.nCopies(mPlacesSelected.size(), 0));
                mSelected = new ArrayList<>(Collections.nCopies(mPlacesSelected.size(), false));
                mPlaceAdapter.notifyDataSetChanged();
            }
        });

        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCount == mPlacesSelected.size()) {
                    Log.d("Count", mPlacesSelected.size()+"");
                    Utility.mUpdatedPlaces.clear();
                    Utility.mUpdatedPlaces.add(Utility.getmGooglePlaces());
                    Utility.mUpdatedPlaces.addAll(mOrderedPlaces);

//                    sortItemList();

//                    for(int i = 0; i<places.size();i++)
//                        Utility.mUpdatedPlaces.add(places.get(i));

//                    Log.d("Count", mPlacesSelected.size()+" - "+Utility.mUpdatedPlaces.size());
                    Intent intent = new Intent(SortActivity.this, MapActivity.class);
                    intent.putExtra(UtilConstants.SHOWBUTTON, true);
                    startActivity(intent);

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SortActivity.this);
                    builder.setCancelable(false);
                    builder.setTitle("Sort Your Places");
                    builder.setMessage("You need to Sort the Places in Order of Priority, " +
                            "Select the Places one by one to Form an Order ");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });
    }

    private synchronized void sortItemList() {

        for(int i = 0;i<mOrderedPlaces.size();i++){
            Log.d("SORT", "Before : "+mOrderedPlaces.get(i).getgPlaceName() +", i = "+i+" n = "+mPlacesSelected.get(i));
        }

        for(int i = 0;i<Utility.mUpdatedPlaces.size();i++){
            Log.d("SORT", "Before : "+Utility.mUpdatedPlaces.get(i).getgPlaceName() +", i = "+i);
        }

//        ArrayList<GooglePlaces> places = new ArrayList<GooglePlaces>(mPlacesSelected);
//
//        for (int i = 0; i < mPlacesSelected.size(); i++) {
//            if(mSelectedItem.get(i) - 1 != -1)
//                places.add(mSelectedItem.get(i) - 1, mPlacesSelected.get(i));
//        }
//        return places;

//        for(int i = 0;i<mPlacesSelected.size();i++){
//            Log.d("SORT", "After : "+places.get(i).getgPlaceName() +", i = "+i+" n = "+mSelectedItem.get(i));
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

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
                convertView = LayoutInflater.from(aContext).inflate(R.layout.sort_places_item, parent, false);
                myViewHolder.rel_overlay = convertView.findViewById(R.id.sort_overlay);
                myViewHolder.sort_count = convertView.findViewById(R.id.sort_item_count);
                myViewHolder.place_name = convertView.findViewById(R.id.sort_item_Name);
                myViewHolder.place_distance = convertView.findViewById(R.id.sort_item_Distance);
                myViewHolder.place_rating = convertView.findViewById(R.id.sort_item_RatingText);
                myViewHolder.place_time = convertView.findViewById(R.id.sort_item_estTime);
                myViewHolder.rating = convertView.findViewById(R.id.sort_item_Rating);
                myViewHolder.selection = convertView.findViewById(R.id.sort_item_Selection);
                myViewHolder.delete = convertView.findViewById(R.id.sort_item_delete);
                convertView.setTag(myViewHolder);
            } else {
                myViewHolder = (MyViewHolder) convertView.getTag();
            }

            if (showAddButton) {
                myViewHolder.selection.setVisibility(View.VISIBLE);
            } else {
                myViewHolder.place_time.setVisibility(View.VISIBLE);
//                myViewHolder.delete.setVisibility(View.VISIBLE);
            }

            if (mSelected.get(position)) {
                myViewHolder.sort_count.setText(mSelectedItem.get(position) + "");
                myViewHolder.rel_overlay.setVisibility(View.VISIBLE);
            } else {
                myViewHolder.sort_count.setText("");
                myViewHolder.rel_overlay.setVisibility(View.GONE);
            }

            myViewHolder.place_name.setText(aGooglePlaces.get(position).getgPlaceName());
            myViewHolder.place_rating.setText(aGooglePlaces.get(position).getgPlaceRating() + "");
            myViewHolder.place_distance.setText(decimalFormat.format(aGooglePlaces.get(position).getDistanceFromSource()) + " km");
            myViewHolder.rating.setRating(aGooglePlaces.get(position).getgPlaceRating());
            myViewHolder.place_time.setText(aGooglePlaces.get(position).getTimeToTravel() + " min");

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mSelected.get(position)) {
                        mSelected.set(position, true);
                        if (mCount < mSelectedItem.size())
                            mCount++;
                        GooglePlaces googlePlaces = mPlacesSelected.get(position);
                        googlePlaces.setSrno(mCount);
                        googlePlaces.setDayno(-1);
                        mOrderedPlaces.add(googlePlaces);
                        mSelectedItem.set(position, mCount);
                        notifyDataSetChanged();
                    }
                }
            });

            return convertView;
        }

        private class MyViewHolder {
            RelativeLayout rel_overlay;
            TextView place_name, place_distance, place_rating, place_time, sort_count;
            RatingBar rating;
            ImageView selection, delete;
        }

    }
}
