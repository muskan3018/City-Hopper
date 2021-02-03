package com.cityhopper.book;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.cityhopper.book.helper.JSONParse;
import com.cityhopper.book.helper.RestAPI;
import com.cityhopper.book.helper.UtilConstants;
import com.cityhopper.book.helper.Utility;
import com.cityhopper.book.pojo.UserProfile;

import org.json.JSONArray;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "loginActivity";
    private EditText edt_Email, edt_Pass;
    private Button btn_register, btn_Login;
    private Dialog mDialog;

    private SharedPreferences sharedPreferences;
    private String UID = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(UtilConstants.SharedPref, Context.MODE_PRIVATE);
        UID = sharedPreferences.getString(UtilConstants.UID, "");


        if(UID.compareTo("") != 0){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else {
            setContentView(R.layout.activity_login);
            edt_Email = findViewById(R.id.login_email);
            edt_Pass = findViewById(R.id.login_pass);
            btn_Login = findViewById(R.id.login_btn);
            btn_register = findViewById(R.id.login_btn_reg);

            mDialog = new Dialog(LoginActivity.this);
            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mDialog.setContentView(R.layout.loading_dialog);
            mDialog.setCancelable(false);

            btn_Login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(edt_Email.getText().length() == 0){
                        Snackbar.make(v, "Please, Enter Email", Snackbar.LENGTH_SHORT).show();
                        edt_Email.requestFocus();
                    }else if(!Patterns.EMAIL_ADDRESS.matcher(edt_Email.getText().toString()).matches()){
                        Snackbar.make(v, "Please, Enter Valid Email", Snackbar.LENGTH_SHORT).show();
                        edt_Email.requestFocus();
                    }else if(edt_Pass.getText().length() == 0) {
                        Snackbar.make(v, "Please, Enter Password", Snackbar.LENGTH_SHORT).show();
                        edt_Pass.requestFocus();
                    }else {
                        new Login().execute(edt_Email.getText().toString(), edt_Pass.getText().toString());
                    }
                }
            });
            btn_register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private class Login extends AsyncTask<String, JSONObject, String> {

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
                JSONObject jsonObject = restAPI.Login(strings[0], strings[1]);
                response = jsonParse.Parse(jsonObject);
            } catch (Exception exp) {
                response = "App-"+exp.getMessage();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG,s);
            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(LoginActivity.this, pair.first, pair.second, false);
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String status = json.getString("status");
                    if (status.compareTo("ok") == 0) {
                        JSONArray jsonArray = json.getJSONArray("Data");
                        JSONObject jsonObject = jsonArray.getJSONObject(0);

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(UtilConstants.UID, jsonObject.getString("data0"));
                        editor.putString(UtilConstants.UNAME, jsonObject.getString("data1"));
                        editor.apply();
                        editor.commit();

                        new GetProfile().execute(jsonObject.getString("data0"));

                    } else if (status.compareTo("false") == 0) {

                        mDialog.dismiss();
                        Utility.ShowAlertDialog(LoginActivity.this, "Invalid Credentials", "You have entered an Email Or Password", false);
                    } else {
                        mDialog.dismiss();
                        String error = json.getString("Data");
                        Log.d(TAG, error);
                    }
                } catch (Exception exp) {
                    mDialog.dismiss();
                    Log.d(TAG, exp.getMessage());
                    exp.printStackTrace();
                }
            }

        }
    }

    private class GetProfile extends AsyncTask<String, JSONObject, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            mDialog.show();
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
            mDialog.dismiss();

            Log.d(TAG, s);
            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(LoginActivity.this, pair.first, pair.second, false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    if (status.compareTo("ok") == 0) {
                        JSONArray jsonArray = jsonObject.getJSONArray("Data");
                        //Uid,1Name,Email,3Contact,4Address,5Place_Interests, 6Cuisine, 7Vegetarian, 8Drinker

                        JSONObject jsonObject1 = jsonArray.getJSONObject(0);

                        Gson gson = new Gson();
                        String json = gson.toJson(new UserProfile(jsonObject1.getString("data1"), jsonObject1.getString("data3"), jsonObject1.getString("data5"),
                                jsonObject1.getString("data6"), jsonObject1.getString("data9"), jsonObject1.getString("data10"),
                                jsonObject1.getString("data4"), jsonObject1.getString("data8"), jsonObject1.getString("data7")));

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(UtilConstants.USER_PROFILE, json);
                        editor.commit();


                        Intent register = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(register);
                        finish();

                    } else if (status.compareTo("no") == 0) {

                    } else {
                        String error = jsonObject.getString("Data");
                        Log.d(TAG, error);
                    }
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }
        }

    }

}
