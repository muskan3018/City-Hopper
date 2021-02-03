package com.cityhopper.book;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.cityhopper.book.fragments.HomeFragment;
import com.cityhopper.book.fragments.ProfileFragment;
import com.cityhopper.book.fragments.TravelPlan;
import com.cityhopper.book.helper.UtilConstants;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{
    private BottomNavigationView bottomNavigationView;
    private Fragment fragment;

    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(UtilConstants.SharedPref, Context.MODE_PRIVATE);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.action_profile:
//                    mTextMessage.setText(R.string.title_entry);
                fragment = new ProfileFragment();
                setActionBarTitle("Profile");
                break;
            case R.id.action_home:
//                Utility.mSelectedPlaces.clear();
//                Utility.mRemovedPlaces.clear();
                fragment = new HomeFragment();
                setActionBarTitle("Home");
                break;
            case R.id.action_plan:
                fragment = new TravelPlan();
                setActionBarTitle("Travel Plan");
                break;
        }
        if (fragment != null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.mainContainer, fragment).commit();
            return true;
        } else {
            Log.e("MainActivity", "Error in creating fragment");
            return false;
        }
    }

    private void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logout, menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_logout){
            logout();
        }else if(item.getItemId() == R.id.menu_changepass){
            Intent intent = new Intent(MainActivity.this, ChangePassword.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(UtilConstants.UID, "");
        editor.putString(UtilConstants.UNAME, "");
        editor.putString(UtilConstants.USER_PROFILE, "");
        editor.commit();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
