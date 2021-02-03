package com.cityhopper.book.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CompoundButtonCompat;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.cityhopper.book.R;
import com.cityhopper.book.RegisterActivity;
import com.cityhopper.book.helper.JSONParse;
import com.cityhopper.book.helper.RestAPI;
import com.cityhopper.book.helper.UtilConstants;
import com.cityhopper.book.helper.Utility;
import com.cityhopper.book.pojo.UserProfile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ProfileFragment extends Fragment {
    private static String TAG = "Profile";
    private GridView cuisine_Grid, places_Grid;
    private String[] cuisine, places;
    private ArrayList<String> sCuisines, sPlaces;
    private EditText edtName, edtContact, edtAddress, edtAge;
    private RadioButton male, female, rdoVegetarian, rdoNonVeg, rdoDrink, rdoNonDrink;
    private TextView crd_Submit;
    private Dialog mDialog;

    private SharedPreferences sharedPreferences;
    private String uid = "";

    public ProfileFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences(UtilConstants.SharedPref, Context.MODE_PRIVATE);
        uid = sharedPreferences.getString(UtilConstants.UID, "");

        mDialog = new Dialog(getActivity());
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.loading_dialog);
        mDialog.setCancelable(false);

        cuisine = getResources().getStringArray(R.array.Cuisine);
        places = getResources().getStringArray(R.array.Places);

        sCuisines = new ArrayList<>(Collections.nCopies(cuisine.length, "NA"));
        sPlaces = new ArrayList<>(Collections.nCopies(places.length, "NA"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        edtName = (EditText) view.findViewById(R.id.profile_name);
        edtContact = (EditText) view.findViewById(R.id.profile_contact);
        edtAddress = (EditText) view.findViewById(R.id.profile_address);
        edtAge = (EditText) view.findViewById(R.id.profile_age);

        male = (RadioButton) view.findViewById(R.id.rdo_male);
        female = (RadioButton) view.findViewById(R.id.rdo_female);
        rdoDrink = (RadioButton) view.findViewById(R.id.rdo_drinker);
        rdoNonDrink = (RadioButton) view.findViewById(R.id.rdo_n_drinker);
        rdoVegetarian = (RadioButton) view.findViewById(R.id.rdo_vege);
        rdoNonVeg = (RadioButton) view.findViewById(R.id.rdo_n_vege);

        crd_Submit = view.findViewById(R.id.profile_text);
        crd_Submit.setText("Update");

        cuisine_Grid = view.findViewById(R.id.profile_cuisine);
        places_Grid = view.findViewById(R.id.profile_places);

        crd_Submit.setTextColor(getResources().getColor(R.color.colorWhite));
        crd_Submit.setBackgroundColor(getResources().getColor(R.color.colorAccent));

        edtAddress.setImeOptions(EditorInfo.IME_ACTION_DONE);

        new GetProfile().execute(uid);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        edtAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideInputKeyboard(edtAddress);
                    return true;
                }
                return false;
            }
        });

        crd_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, sCuisines.toString());
                Log.d(TAG, Arrays.asList(cuisine).toString());

                if(Validate()){
                    if (RegisterActivity.isItemSelected(sCuisines, cuisine)) {
                        Snackbar.make(v, "Please, Choose at-least one from Cuisine", Snackbar.LENGTH_SHORT).show();
                    } else if (RegisterActivity.isItemSelected(sPlaces, places)) {
                        Snackbar.make(v, "Please, Choose at-least one from Places", Snackbar.LENGTH_SHORT).show();
                    } else {
                        String cuisine = RegisterActivity.getStringFromArray(sCuisines);
                        Log.d("UPDATE", "Cuisine - " + cuisine);

                        String places = RegisterActivity.getStringFromArray(sPlaces);
                        Log.d("UPDATE", "Places - " + places);

                        //String uid,String name,String contact,String address,String age,String place_Interests,String cuisine,String isveg,String isdrinker
                        Log.d(TAG, uid+"\n"+edtName.getText().toString()+"\n"+ edtContact.getText().toString()
                                +"\n"+ edtAddress.getText().toString()+"\n"+ edtAge.getText().toString()+"\n"+ places+"\n"+ cuisine+"\n"+ rdoVegetarian.isChecked()+"\n"+ rdoDrink.isChecked()+"");


                        new UpdateProfile().execute(uid, edtName.getText().toString(), edtContact.getText().toString()
                                , edtAddress.getText().toString(), edtAge.getText().toString(), places, cuisine, rdoVegetarian.isChecked()+"", rdoDrink.isChecked()+"");
                    }
                }
            }
        });

    }

    private boolean Validate() {
        boolean valid = true;
                                                //0        1        2         3     4       5           6           7         8
        EditText[] editTexts = new EditText[]{edtName,edtContact,edtContact,edtAge,edtAge,edtContact,edtContact,edtContact,edtAddress};
        String[] message = new String[]{"Please, Enter Name","Please, Enter Your Contact"
                ,"Please, Enter Your Valid Contact","Please, Enter Your Age"
                ,"Please, Enter Your Age between 15 to 100","Please, Choose Gender(Male or Female)"
                ,"Please, Choose are you Vegetarian or not","Please, Choose do you drink(Alcohol) or not"
                ,"Please, Enter Your Address"};
        for(int i = 0;i<editTexts.length;i++){
            if(i == 2){
                if(editTexts[i].getText().length() < 10){
                    Snackbar.make(crd_Submit, message[i], Snackbar.LENGTH_SHORT).show();
                    valid = false;
                    break;
                }
            }else if(i == 4){
                if(Integer.parseInt(editTexts[i].getText().toString()) < 15 || Integer.parseInt(editTexts[i].getText().toString()) > 100){
                    Snackbar.make(crd_Submit, message[i], Snackbar.LENGTH_SHORT).show();
                    valid = false;
                    break;
                }
            }else if(i == 5){
                if(!(male.isChecked() || female.isChecked())){
                    Snackbar.make(crd_Submit, message[i], Snackbar.LENGTH_SHORT).show();
                    valid = false;
                    break;
                }
            }else if(i == 6){
                if(!(rdoVegetarian.isChecked() || rdoNonVeg.isChecked())){
                    Snackbar.make(crd_Submit, message[i], Snackbar.LENGTH_SHORT).show();
                    valid = false;
                    break;
                }
            }else if(i == 7){
                if(!(rdoDrink.isChecked() || rdoNonDrink.isChecked())){
                    Snackbar.make(crd_Submit, message[i], Snackbar.LENGTH_SHORT).show();
                    valid = false;
                    break;
                }
            }else if(editTexts[i].getText().length() == 0){
                Snackbar.make(crd_Submit, message[i], Snackbar.LENGTH_SHORT).show();
                editTexts[i].requestFocus();
                valid = false;
                break;
            }
        }
        return valid;
    }

    private void ShowSnackBar(EditText editText, String message) {
        Snackbar.make(crd_Submit, message, Snackbar.LENGTH_SHORT).show();
        editText.requestFocus();
    }

    private void hideInputKeyboard(View editText) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private class GetProfile extends AsyncTask<String, JSONObject, String> {
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
                JSONObject jsonObject = restAPI.getProfile(strings[0]);
                response = jsonParse.Parse(jsonObject);
            } catch (Exception exp) {
                response = exp.getMessage();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.d(TAG, s);
            if (Utility.checkConnection(s)) {
                mDialog.dismiss();
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(getActivity(), pair.first, pair.second, false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    if (status.compareTo("ok") == 0) {
                        JSONArray jsonArray = jsonObject.getJSONArray("Data");
                        //Uid,1Name,Email,3Contact,4Address,5Place_Interests, 6Cuisine, 7Vegetarian, 8Drinker
                        JSONObject jsonObject1 = jsonArray.getJSONObject(0);

                        Gson gson = new Gson();
                        String json = gson.toJson(new UserProfile(jsonObject1.getString("data1"), jsonObject1.getString("data3"), jsonObject1.getString("data6"),
                                jsonObject1.getString("data5"), jsonObject1.getString("data9"), jsonObject1.getString("data10"),
                                jsonObject1.getString("data4"), jsonObject1.getString("data8"), jsonObject1.getString("data7")));

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(UtilConstants.USER_PROFILE, json);
                        editor.commit();

                        //@NonNull String UName,@NonNull String UContact, @NonNull String UAge
                        //                    ,@NonNull String UGender,@NonNull String UVeg,@NonNull String UDrinker
                        setValues(new UserProfile(jsonObject1.getString("data1"), jsonObject1.getString("data3"), jsonObject1.getString("data6"),
                                jsonObject1.getString("data5"), jsonObject1.getString("data9"), jsonObject1.getString("data10"),
                                jsonObject1.getString("data4"), jsonObject1.getString("data8"), jsonObject1.getString("data7")));

                    } else if (status.compareTo("no") == 0) {
                        mDialog.dismiss();
                        Toast.makeText(getActivity(), "Could not find Your Profile", Toast.LENGTH_SHORT).show();
                    } else {
                        mDialog.dismiss();
                        String error = jsonObject.getString("Data");
                        Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, error);
                    }
                } catch (Exception exp) {
                    mDialog.dismiss();
                    exp.printStackTrace();
                }
            }
        }

    }

    private void setValues(UserProfile userProfile) {
        Log.d("USER", userProfile.getName() + "\n" + userProfile.getContact() + "\n" + userProfile.getAddress()
                + "\n" + userProfile.getCuisine() + "\n" + userProfile.getPlaces());

//        Utility.setUserProfile(userProfile);

        edtName.setText(userProfile.getName());
        edtContact.setText(userProfile.getContact());
        edtAddress.setText(userProfile.getAddress());
        edtAge.setText(userProfile.getAge());

        edtName.setSelection(edtName.getText().length());
        edtContact.setSelection(edtContact.getText().length());
        edtAge.setSelection(edtAge.getText().length());
        edtAddress.setSelection(edtAddress.getText().length());

        //Gender Radio Button
        if (userProfile.getGender().compareTo("Male") == 0){
            male.setChecked(true);
            male.setClickable(false);
            female.setClickable(false);
        }
        else{
            female.setChecked(true);
            male.setClickable(false);
            female.setClickable(false);
        }

        //Vegetarian Radio Button
        boolean vCheck = userProfile.getVegetarian().compareTo("true") == 0;
        if (vCheck)
            rdoVegetarian.setChecked(true);
        else
            rdoNonVeg.setChecked(true);

        if (userProfile.getDrinker().compareTo("true") == 0)
            rdoDrink.setChecked(true);
        else
            rdoNonDrink.setChecked(true);

        String[] cui = userProfile.getCuisine().split(",");
        if (cui.length == 1) {
            sCuisines.set(getItemPosition(cuisine, cui[0]), cui[0]);
        } else {
            for (String food : cui) {
                sCuisines.set(getItemPosition(cuisine, food), food);
            }
        }

        String[] pla = userProfile.getPlaces().split(",");
        if (pla.length == 1) {
            sPlaces.set(getItemPosition(places, pla[0]), pla[0]);
        } else {
            for (String place : pla) {
                sPlaces.set(getItemPosition(places, place), place);
            }
        }

        Log.d("PLACE_CUIS", sCuisines.size() + "\n" + sPlaces.size());

        cuisine_Grid.setAdapter(new CheckBoxAdapter(getActivity().getApplicationContext(), cuisine, true));
        places_Grid.setAdapter(new CheckBoxAdapter(getActivity().getApplicationContext(), places, false));

        if (mDialog.isShowing())
            mDialog.dismiss();
    }

    private int getItemPosition(String[] array, String itemName) {
        Log.d("ITEMS", Arrays.asList(array).toString());
        int possition = -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i].compareTo(itemName) == 0) {
                Log.d("ITEMS", array[i] + " - " + itemName + " - " + i);
                possition = i;
                break;
            }
        }
        return possition;
    }

    public class CheckBoxAdapter extends BaseAdapter {
        private Context mContext;
        private String[] cArrayName;
        private boolean isCuisine;

        public CheckBoxAdapter(Context c, @NonNull String[] name, boolean isCuisinne) {
            mContext = c;
            cArrayName = name;
            isCuisine = isCuisinne;
        }

        public int getCount() {
            return cArrayName.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(final int position, View convertView, ViewGroup parent) {
            CheckBox checkBox;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                checkBox = new CheckBox(mContext);

                checkBox.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                checkBox.setText(cArrayName[position]);
                checkBox.setGravity(Gravity.CENTER | Gravity.LEFT);
                checkBox.setTextColor(mContext.getResources().getColor(R.color.colorWhite));
                int states[][] = {{android.R.attr.state_checked}, {}};
                int colors[] = {getResources().getColor(R.color.colorWhite), getResources().getColor(R.color.colorAccent)};
                CompoundButtonCompat.setButtonTintList(checkBox, new ColorStateList(states, colors));
            } else {
                checkBox = (CheckBox) convertView;
            }

            if (isCuisine) {
                if (sCuisines.get(position).compareTo("NA") != 0) {
                    checkBox.setChecked(true);
                }
            } else {
                if (sPlaces.get(position).compareTo("NA") != 0) {
                    checkBox.setChecked(true);
                }
            }

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        if (isCuisine) {
                            sCuisines.set(position, cArrayName[position]);
                        } else {
                            sPlaces.set(position, cArrayName[position]);
                        }
                    } else {
                        if (isCuisine) {
                            sCuisines.set(position, "NA");
                        } else {
                            sPlaces.set(position, "NA");
                        }
                    }
                }
            });

            return checkBox;
        }
    }

    private class UpdateProfile extends AsyncTask<String, JSONObject, String> {
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
                JSONObject jsonObject = restAPI.UpdateProfile(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6], strings[7],strings[8]);
                response = jsonParse.Parse(jsonObject);
            } catch (Exception e) {
                response = e.getMessage();
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
                Utility.ShowAlertDialog(getActivity(), pair.first, pair.second, false);
            } else {
                try {

                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");

                    if (status.compareTo("true") == 0) {
                        AlertDialog.Builder aDialogBuilder = new AlertDialog.Builder(getActivity());
                        aDialogBuilder.setTitle("Success");
                        aDialogBuilder.setMessage("Update Successfully");
                        aDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                new GetProfile().execute(uid);
                            }
                        });
                        AlertDialog alertDialog = aDialogBuilder.create();
                        alertDialog.show();
                    } else {
                        Toast.makeText(getActivity(), "Something went wrong try again", Toast.LENGTH_SHORT).show();
                        String error = jsonObject.getString("Data");
                        Log.d(TAG, error);
                    }

                } catch (Exception exp) {
                    Log.d(TAG, exp.getMessage());
                    exp.printStackTrace();
                }
            }
        }
    }

}
