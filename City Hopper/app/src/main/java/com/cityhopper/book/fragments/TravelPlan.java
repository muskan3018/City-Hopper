package com.cityhopper.book.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.cityhopper.book.MapActivity;
import com.cityhopper.book.R;
import com.cityhopper.book.city.SelectionActivity;
import com.cityhopper.book.helper.JSONParse;
import com.cityhopper.book.helper.RestAPI;
import com.cityhopper.book.helper.UtilConstants;
import com.cityhopper.book.helper.Utility;
import com.cityhopper.book.pojo.GooglePlaces;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TravelPlan extends Fragment {
    private SharedPreferences sharedPreferences;
    private String UID;
    private ListView list_Places;

    private Dialog mDialog;
    private ArrayList<String> planId, dest, destType, noOfDays, planData;

    public TravelPlan() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences(UtilConstants.SharedPref, Context.MODE_PRIVATE);
        UID = sharedPreferences.getString(UtilConstants.UID, "");
        mDialog = Utility.GetLoadingDialog(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_selection, container, false);
        list_Places = view.findViewById(R.id.list_result);
        view.findViewById(R.id.dialog_s_layout).setVisibility(View.GONE);
        view.findViewById(R.id.btn_selection).setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        list_Places.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (Integer.parseInt(noOfDays.get(position)) == -1) {
                    prepareNData(position);
                } else {
                    prepareList(position);
                }
            }
        });
    }

    private void prepareList(int position) {
        try {
            String prevDay = "";
            JSONArray jsonArray = new JSONArray(planData.get(position));
            ArrayList<ArrayList<GooglePlaces>> lisOfPlaces = new ArrayList<ArrayList<GooglePlaces>>();
            ArrayList<String> dayNo = new ArrayList<String>();
            ArrayList<GooglePlaces> places = new ArrayList<GooglePlaces>();

            for (int i = 0; i < jsonArray.length(); i++) {
                if (i == 0) {
                    prevDay = jsonArray.getJSONObject(i).getString("data0");
                    places.add(GetGooglePlace(jsonArray.getJSONObject(i)));
                } else if (i == jsonArray.length() - 1) {
                    String day = jsonArray.getJSONObject(i).getString("data0");
                    if (Integer.parseInt(day) == Integer.parseInt(prevDay)) {
                        places.add(GetGooglePlace(jsonArray.getJSONObject(i)));
                        lisOfPlaces.add(places);
                    } else {
                        places = new ArrayList<GooglePlaces>();
                        places.add(lisOfPlaces.get(0).get(0));
                        places.add(GetGooglePlace(jsonArray.getJSONObject(i)));
                        lisOfPlaces.add(places);
                    }
                } else {
                    String day = jsonArray.getJSONObject(i).getString("data0");
                    if (Integer.parseInt(day) == Integer.parseInt(prevDay)) {
                        places.add(GetGooglePlace(jsonArray.getJSONObject(i)));
                    } else {
                        prevDay = day;
                        lisOfPlaces.add(places);
                        places = new ArrayList<GooglePlaces>();
                        places.add(lisOfPlaces.get(0).get(0));
                        places.add(GetGooglePlace(jsonArray.getJSONObject(i)));
                    }
                }
            }

            if (lisOfPlaces.size() != Integer.parseInt(noOfDays.get(position))) {
                places = new ArrayList<GooglePlaces>();
                places.add(lisOfPlaces.get(0).get(0));
                lisOfPlaces.add(places);
            }

            Utility.setDayNo(new ArrayList<String>());
            Utility.setMainCollection(new ArrayList<ArrayList<GooglePlaces>>());

            for (int i = 0; i < lisOfPlaces.size(); i++) {
                dayNo.add("Day " + (i + 1));

                for (int j = 0; j < lisOfPlaces.get(i).size(); j++) {
                    if (j == 1) {
                        Location location = new Location("");
                        location.setLatitude(lisOfPlaces.get(i).get(0).getgPlaceLocation().latitude);
                        location.setLongitude(lisOfPlaces.get(i).get(0).getgPlaceLocation().longitude);
                        GooglePlaces googlePlaces = lisOfPlaces.get(i).get(j);
                        googlePlaces.setDistanceFromSource(SelectionActivity.GetDistance(googlePlaces.getgPlaceLocation()
                                , location));
                        googlePlaces.setTimeToTravel(SelectionActivity.getTimeToTravel(googlePlaces.getDistanceFromSource()));
                        lisOfPlaces.get(i).set(j, googlePlaces);
                    } else if (j == lisOfPlaces.get(i).size() - 1) {
                        Location location = new Location("");
                        location.setLatitude(lisOfPlaces.get(i).get(0).getgPlaceLocation().latitude);
                        location.setLongitude(lisOfPlaces.get(i).get(0).getgPlaceLocation().longitude);
                        GooglePlaces googlePlaces = lisOfPlaces.get(i).get(j);
                        googlePlaces.setDistanceFromSource(SelectionActivity.GetDistance(googlePlaces.getgPlaceLocation()
                                , location));
                        googlePlaces.setTimeToTravel(SelectionActivity.getTimeToTravel(googlePlaces.getDistanceFromSource()));
                        lisOfPlaces.get(i).set(j, googlePlaces);
                    } else {
                        Location location = new Location("");
                        location.setLatitude(lisOfPlaces.get(i).get(j + 1).getgPlaceLocation().latitude);
                        location.setLongitude(lisOfPlaces.get(i).get(j + 1).getgPlaceLocation().longitude);
                        GooglePlaces googlePlaces = lisOfPlaces.get(i).get(j);
                        googlePlaces.setDistanceFromSource(SelectionActivity.GetDistance(googlePlaces.getgPlaceLocation()
                                , location));
                        googlePlaces.setTimeToTravel(SelectionActivity.getTimeToTravel(googlePlaces.getDistanceFromSource()));
                        lisOfPlaces.get(i).set(j, googlePlaces);
                    }
                }
            }

            Utility.setDayNo(dayNo);
            Utility.setMainCollection(lisOfPlaces);

            Intent intent = new Intent(getActivity(), MapActivity.class);
            intent.putExtra(UtilConstants.ISDAYSPLAN, true);
            intent.putExtra(UtilConstants.SHOWBUTTON, false);
            intent.putExtra(UtilConstants.ISNAVIGATION, true);
            startActivity(intent);


        } catch (JSONException exp) {
            exp.printStackTrace();
        }
    }

    private void prepareNData(int position) {
        try {
            JSONArray jsonArray = new JSONArray(planData.get(position));
            ArrayList<GooglePlaces> places = new ArrayList<GooglePlaces>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                places.add(GetGooglePlace(jsonObject));
            }

            for (int i = 0; i < places.size(); i++) {
                if (i == 1) {
                    Location location = new Location("");
                    location.setLatitude(places.get(0).getgPlaceLocation().latitude);
                    location.setLongitude(places.get(0).getgPlaceLocation().longitude);
                    GooglePlaces googlePlaces = places.get(i);
                    googlePlaces.setDistanceFromSource(SelectionActivity.GetDistance(googlePlaces.getgPlaceLocation()
                            , location));
                    googlePlaces.setTimeToTravel(SelectionActivity.getTimeToTravel(googlePlaces.getDistanceFromSource()));
                    places.set(i, googlePlaces);
                } else if (i == places.size() - 1) {
                    Location location = new Location("");
                    location.setLatitude(places.get(0).getgPlaceLocation().latitude);
                    location.setLongitude(places.get(0).getgPlaceLocation().longitude);
                    GooglePlaces googlePlaces = places.get(i);
                    googlePlaces.setDistanceFromSource(SelectionActivity.GetDistance(googlePlaces.getgPlaceLocation()
                            , location));
                    googlePlaces.setTimeToTravel(SelectionActivity.getTimeToTravel(googlePlaces.getDistanceFromSource()));
                    places.set(i, googlePlaces);
                } else {
                    Location location = new Location("");
                    location.setLatitude(places.get(i + 1).getgPlaceLocation().latitude);
                    location.setLongitude(places.get(i + 1).getgPlaceLocation().longitude);
                    GooglePlaces googlePlaces = places.get(i);
                    googlePlaces.setDistanceFromSource(SelectionActivity.GetDistance(googlePlaces.getgPlaceLocation()
                            , location));
                    googlePlaces.setTimeToTravel(SelectionActivity.getTimeToTravel(googlePlaces.getDistanceFromSource()));
                    places.set(i, googlePlaces);
                }
            }

            Utility.mUpdatedPlaces.clear();
            Utility.mUpdatedPlaces.addAll(places);

            for (GooglePlaces googlePlaces : Utility.mUpdatedPlaces) {
                Log.d("BEFORE", googlePlaces.getgPlaceId() + " - "
                        + googlePlaces.getgPlaceName());
            }

            Intent intent = new Intent(getActivity(), MapActivity.class);
            intent.putExtra(UtilConstants.SHOWBUTTON, false);
            intent.putExtra(UtilConstants.ISNAVIGATION, true);
            startActivity(intent);
        } catch (JSONException exp) {
            exp.printStackTrace();
        }
    }

    private GooglePlaces GetGooglePlace(JSONObject jsonObject) throws JSONException {
        GooglePlaces googlePlaces = new GooglePlaces();
        googlePlaces.setgPlaceId(jsonObject.getString("data5"));
        googlePlaces.setgPlaceName(jsonObject.getString("data2"));
        googlePlaces.setgPlaceLocation(GetLocation(jsonObject.getString("data3")));
        googlePlaces.setgPlaceRating(Float.parseFloat(jsonObject.getString("data4")));
        googlePlaces.setSrno(Integer.parseInt(jsonObject.getString("data1")));
        googlePlaces.setDayno(Integer.parseInt(jsonObject.getString("data0")));
        return googlePlaces;
    }

    private LatLng GetLocation(String location) {
        String[] loction = location.split(",");
        return new LatLng(Double.parseDouble(loction[0]), Double.parseDouble(loction[1]));
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetTravelPlan().execute(UID);
    }

    private class GetTravelPlan extends AsyncTask<String, JSONObject, String> {
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
                response = jsonParse.Parse(restAPI.getSmartPlan(strings[0]));
            } catch (Exception ex) {
                response = ex.getMessage();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mDialog.dismiss();
            Log.d("TRAVEL_PLAN", "onPostExecute: " + s);
            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(getActivity(), pair.first, pair.second, false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    if (status.compareTo("ok") == 0) {

                        planId = new ArrayList<String>();
                        dest = new ArrayList<String>();
                        destType = new ArrayList<String>();
                        noOfDays = new ArrayList<String>();
                        planData = new ArrayList<String>();

                        JSONArray jsonArray = jsonObject.getJSONArray("Data");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject json = jsonArray.getJSONObject(i);
                            planId.add(json.getString("planid"));
                            dest.add(json.getString("dest"));
                            destType.add(json.getString("type"));
                            noOfDays.add(json.getString("nodays"));
                            planData.add(json.getString("Data"));
                        }

                        TravelAdapter travelAdapter = new TravelAdapter(getActivity(), R.layout.item_plan, planId);
                        list_Places.setAdapter(travelAdapter);

                    } else if (status.compareTo("no") == 0) {
                        list_Places.setAdapter(null);
                        Utility.ShowAlertDialog(getActivity(), "No Plan", "Could not find any plan, you have not added any plan", false);
                    } else {
                        String error = jsonObject.getString("Data");
                        Log.d("TRAVEL_PLAN", "onPostExecute: Error - " + error);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class TravelAdapter extends ArrayAdapter<String> {

        private Context adapContext;

        public TravelAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> objects) {
            super(context, resource, objects);
            this.adapContext = context;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(adapContext).inflate(R.layout.item_plan, null, false);
                viewHolder.textName = convertView.findViewById(R.id.text_plan_name);
                viewHolder.textData = convertView.findViewById(R.id.text_plan_detail);
                viewHolder.deletePlan = convertView.findViewById(R.id.delete_plan);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (Integer.parseInt(noOfDays.get(position)) == -1) {
                viewHolder.textName.setText(Html.fromHtml("<b>Within City</b>"));
                try {
                    JSONArray jsonArray = new JSONArray(planData.get(position));
                    viewHolder.textData.setText(Html.fromHtml("<b>No. of Place (Visit) : </b>" + (jsonArray.length() - 1)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                viewHolder.textName.setText(Html.fromHtml("<b>Destination : </b>" + dest.get(position)));
                viewHolder.textData.setText(Html.fromHtml("<b>No Of Days : </b>" + noOfDays.get(position)));
            }

            viewHolder.deletePlan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                            .setTitle("Delete Plan")
                            .setMessage("Are you sure, you want to Delete this Plan ?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    new DeletePlan().execute(planId.get(position));
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create();
                    alertDialog.show();
                }
            });

            return convertView;
        }

        private class ViewHolder {
            TextView textName, textData;
            ImageView deletePlan;
        }
    }

    private class DeletePlan extends AsyncTask<String, JSONObject, String> {
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
                response = jsonParse.Parse(restAPI.deletePlan(strings[0]));
            } catch (Exception ex) {
                response = ex.getMessage();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mDialog.dismiss();
            Log.d("TRAVEL_PLAN", "onPostExecute: " + s);
            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(getActivity(), pair.first, pair.second, false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    if (status.compareTo("true") == 0) {

                        Toast.makeText(getActivity(), "Plan Deleted", Toast.LENGTH_SHORT).show();

                        new GetTravelPlan().execute(UID);

                    } else {
                        String error = jsonObject.getString("Data");
                        Toast.makeText(getActivity(), "Something went wrong, Try Again", Toast.LENGTH_SHORT).show();
                        Log.d("TRAVEL_PLAN", "onPostExecute: Error - " + error);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
