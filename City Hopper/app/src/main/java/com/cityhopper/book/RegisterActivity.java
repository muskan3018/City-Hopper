package com.cityhopper.book;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioButton;

import com.cityhopper.book.helper.JSONParse;
import com.cityhopper.book.helper.RestAPI;
import com.cityhopper.book.helper.Utility;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class RegisterActivity extends AppCompatActivity {
    private static String TAG = "RegisterActivity";
    private GridView cuisine_Grid, places_Grid;
    private String[] cuisine, places;
    private ArrayList<String> sCuisines, sPlaces;
    private EditText edtName, edtEmail, edtContact, edtAddress, edtPassword, edtAge;
    private RadioButton male, female, rdoVegetarian, rdoNonVeg, rdoDrink, rdoNonDrink;
    private Button btn_Submit;
    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mDialog = new Dialog(RegisterActivity.this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.loading_dialog);
        mDialog.setCancelable(false);

        edtName = (EditText) findViewById(R.id.reg_name);
        edtEmail = (EditText) findViewById(R.id.reg_email);
        edtContact = (EditText) findViewById(R.id.reg_Contact);
        edtAddress = (EditText) findViewById(R.id.reg_Address);

        edtAge = (EditText) findViewById(R.id.reg_Age);

        male = (RadioButton) findViewById(R.id.rdo_maleReg);
        female = (RadioButton) findViewById(R.id.rdo_femaleReg);
        rdoDrink = (RadioButton) findViewById(R.id.rdo_drinkerReg);
        rdoNonDrink = (RadioButton) findViewById(R.id.rdo_n_drinkerReg);
        rdoVegetarian = (RadioButton) findViewById(R.id.rdo_VegReg);
        rdoNonVeg = (RadioButton) findViewById(R.id.rdo_n_VegReg);

        edtPassword = (EditText) findViewById(R.id.reg_Password);

        btn_Submit = findViewById(R.id.reg_button);

        cuisine_Grid = findViewById(R.id.grid_cuisine);
        places_Grid = findViewById(R.id.grid_places);

        cuisine = getResources().getStringArray(R.array.Cuisine);
        places = getResources().getStringArray(R.array.Places);

        sCuisines = new ArrayList<>(Collections.nCopies(cuisine.length, "NA"));
        sPlaces = new ArrayList<>(Collections.nCopies(places.length, "NA"));

        cuisine_Grid.setAdapter(new CheckBoxAdapter(RegisterActivity.this, cuisine, true));
        places_Grid.setAdapter(new CheckBoxAdapter(RegisterActivity.this, places, false));

    }

    @Override
    protected void onStart() {
        super.onStart();
        btn_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Validate()){
                    if(isItemSelected(sCuisines, cuisine)){
                        Snackbar.make(btn_Submit,"Please, Choose at-least one Item from Cuisine", Snackbar.LENGTH_SHORT).show();
                    }else if(isItemSelected(sPlaces, places)){
                        Snackbar.make(btn_Submit,"Please, Choose at-least one Item from Places", Snackbar.LENGTH_SHORT).show();
                    }else {
                        callRegister();
                    }
                }
            }
        });
    }

    public static boolean isItemSelected(@NonNull ArrayList<String> list, String[] cmp) {
        boolean selected = true;
        for(int i = 0;i<list.size();i++){
            if(list.get(i).compareTo(cmp[i]) == 0){
                selected = false;
                break;
            }
        }
        return selected;
    }

    private void callRegister() {
        String cuisine = getStringFromArray(sCuisines);
        Log.d("REGISTER", "Cuisine - "+cuisine);

        String places = getStringFromArray(sPlaces);
        Log.d("REGISTER", "Places - "+places);

        //String Name,String Email,String Contact,String Address,String gender,String age,
        // String Place_Interests,String Cuisine,String isveg,String isdrinker,String Pass
        Log.d(TAG, edtName.getText().toString()+"\n"+edtEmail.getText().toString()
                        +"\n"+ edtContact.getText().toString()+"\n"+edtAddress.getText().toString()
                        +"\n"+ (male.isChecked() ? "Male" : "Female")+"\n"+edtAge.getText().toString()+"\n"+places+"\n"+cuisine
                        +"\n"+ rdoVegetarian.isChecked() +"\n"+ rdoDrink.isChecked() +"\n"+ edtPassword.getText().toString());

        new RegisterUser().execute(edtName.getText().toString(),edtEmail.getText().toString()
                                    , edtContact.getText().toString(),edtAddress.getText().toString()
                                    , (male.isChecked() ? "Male" : "Female"),edtAge.getText().toString(), places, cuisine
                                    , rdoVegetarian.isChecked()+"", rdoDrink.isChecked()+"" ,edtPassword.getText().toString());

    }

    public static String getStringFromArray(@NonNull ArrayList<String> sString) {
        String result = "";
        for(String string : sString){
            if(string.compareTo("NA") != 0)
                result = result.concat(string+",");
        }
        result = (String) result.subSequence(0, result.length()-1);
        return result;
    }

    private boolean Validate() {
        boolean valid = true;
        EditText[] editTexts = new EditText[]{edtName, edtEmail,edtEmail,edtContact,edtContact,edtAge,edtAge,edtContact,edtContact,edtContact,edtAddress, edtPassword};
        String[] message = new String[]{"Please, Enter Name", "Please, Enter Your Email", "Please, Enter Valid Email"
                                ,"Please, Enter Your Contact","Please, Enter Your Valid Contact","Please, Enter Your Age"
                                ,"Please, Enter Your Age between 15 to 100","Please, Choose Gender(Male or Female)"
                                ,"Please, Choose are you Vegetarian or not","Please, Choose do you drink(Alcohol) or not"
                                ,"Please, Enter Your Address","Please, Enter Your Password"};
        for(int i = 0;i<editTexts.length;i++){
            if(i == 2){
                if(!Patterns.EMAIL_ADDRESS.matcher(editTexts[i].getText().toString()).matches()){
                    Snackbar.make(btn_Submit, message[i], Snackbar.LENGTH_SHORT).show();
                    editTexts[i].requestFocus();
                    valid = false;
                    break;
                }
            }else if(i == 4){
                if(editTexts[i].getText().length() < 10){
                    Snackbar.make(btn_Submit, message[i], Snackbar.LENGTH_SHORT).show();
                    valid = false;
                    editTexts[i].requestFocus();
                    break;
                }
            }else if(i == 6){
                if(Integer.parseInt(editTexts[i].getText().toString()) < 15 || Integer.parseInt(editTexts[i].getText().toString()) > 100){
                    Snackbar.make(btn_Submit, message[i], Snackbar.LENGTH_SHORT).show();
                    valid = false;
                    editTexts[i].requestFocus();
                    break;
                }
            }else if(i == 7){
                if(!(male.isChecked() || female.isChecked())){
                    Snackbar.make(btn_Submit, message[i], Snackbar.LENGTH_SHORT).show();
                    valid = false;
                    break;
                }
            }else if(i == 8){
                if(!(rdoVegetarian.isChecked() || rdoNonVeg.isChecked())){
                    Snackbar.make(btn_Submit, message[i], Snackbar.LENGTH_SHORT).show();
                    valid = false;
                    break;
                }
            }else if(i == 9){
                if(!(rdoDrink.isChecked() || rdoNonDrink.isChecked())){
                    Snackbar.make(btn_Submit, message[i], Snackbar.LENGTH_SHORT).show();
                    valid = false;
                    break;
                }
            }else if(editTexts[i].getText().length() == 0){
                Snackbar.make(btn_Submit, message[i], Snackbar.LENGTH_SHORT).show();
                editTexts[i].requestFocus();
                valid = false;
                break;
            }
        }
        return valid;
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
                checkBox.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
                checkBox.setGravity(Gravity.LEFT | Gravity.CENTER);
            } else {
                checkBox = (CheckBox) convertView;
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

    private class RegisterUser extends AsyncTask<String, JSONObject, String> {

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
                JSONObject jsonObject = restAPI.Register(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6]
                        , strings[7], strings[8], strings[9], strings[10]);
                response = jsonParse.Parse(jsonObject);
            } catch (Exception exp) {
                response = exp.getMessage();
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
                ShowAlertDialog(pair.first, pair.second, false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    if (status.compareTo("true") == 0) {
                        ShowAlertDialog("Success", "Successfully Registered, Press OK to Login", true);
                    } else if (status.compareTo("already") == 0) {
                        ShowAlertDialog("Already", "A user with same email already exists", false);
                    } else {
                        String error = jsonObject.getString("Data");
                        Log.d(TAG, error);
                    }

                } catch (Exception exp) {
                    Log.d(TAG, exp.getMessage());
                }
            }
        }
    }

    private void ShowAlertDialog(String title, String message, final boolean isFinish) {
        AlertDialog.Builder aDialogBuilder = new AlertDialog.Builder(RegisterActivity.this);
        aDialogBuilder.setTitle(title);
        aDialogBuilder.setMessage(message);
        aDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (isFinish)
                    finish();
            }
        });
        AlertDialog alertDialog = aDialogBuilder.create();
        alertDialog.show();
    }

}
