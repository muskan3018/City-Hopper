package com.cityhopper.book;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.cityhopper.book.helper.JSONParse;
import com.cityhopper.book.helper.RestAPI;
import com.cityhopper.book.helper.UtilConstants;
import com.cityhopper.book.helper.Utility;

import org.json.JSONObject;

public class ChangePassword extends AppCompatActivity {

    private static final String TAG = "ChangePassword";
    private EditText old_password, new_password;
    private Button btn_submit;
    private Dialog mDialog;
    private SharedPreferences sharedPreferences;
    private String UID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        getSupportActionBar().setTitle("Change Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        sharedPreferences = getSharedPreferences(UtilConstants.SharedPref, Context.MODE_PRIVATE);
        UID = sharedPreferences.getString(UtilConstants.UID, "");
        mDialog = new Dialog(ChangePassword.this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.loading_dialog);
        mDialog.setCancelable(false);

        old_password = (EditText) findViewById(R.id.edt_old_password);
        new_password = (EditText) findViewById(R.id.edt_new_password);
        btn_submit = findViewById(R.id.btn_changePassword);

    }

    @Override
    protected void onStart() {
        super.onStart();

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (old_password.getText().length() == 0) {
                    Snackbar.make(v, "Please, Enter Old Password", Snackbar.LENGTH_SHORT).show();
                    old_password.requestFocus();
                } else if (new_password.getText().length() == 0) {
                    Snackbar.make(v, "Please, Enter New Password", Snackbar.LENGTH_SHORT).show();
                    new_password.requestFocus();
                } else {
                    new ResetPassword().execute(UID, old_password.getText().toString(), new_password.getText().toString());
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    private class ResetPassword extends AsyncTask<String, JSONObject, String> {

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
                JSONObject jsonObject = restAPI.ChangePassword(strings[0], strings[1], strings[2]);
                response = jsonParse.Parse(jsonObject);
            } catch (Exception exp) {
                response = exp.getMessage();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(ChangePassword.this, pair.first, pair.second, false);
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String status = json.getString("status");
                    if (status.compareTo("true") == 0) {

                        mDialog.dismiss();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(UtilConstants.UID, "");
                        editor.putString(UtilConstants.UNAME, "");
                        editor.putString(UtilConstants.USER_PROFILE, "");
                        editor.commit();

                        AlertDialog alertDialog = Utility.getAlertBuilder(ChangePassword.this, "Success"
                                , "Password Updated Successfully, Press OK Login", true).create();
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(ChangePassword.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        });
                        alertDialog.show();

                    } else if (status.compareTo("false") == 0) {

                        mDialog.dismiss();

                        Utility.ShowAlertDialog(ChangePassword.this, "Incorrect Password", "Previous Password is Incorrect, Try Again", false);

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
}
