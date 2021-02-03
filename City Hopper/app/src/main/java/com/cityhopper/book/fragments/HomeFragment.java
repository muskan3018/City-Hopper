package com.cityhopper.book.fragments;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.cityhopper.book.R;
import com.cityhopper.book.city.SelectionActivity;
import com.cityhopper.book.helper.UtilConstants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment{
    private static String TAG = "HomeSuggest";
    private SharedPreferences sharedPreferences;

    public static final int FIXED_WIDTH = 4;

    private TextView btnStart;


    private String UID = "";

    LinearLayout Linstart,Linend;
    TextView Starttime,Endtime,Tothours;

    Calendar Startcal,Endcal;
    SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");

    TimePickerDialog StartPicker=null;
    TimePickerDialog EndPicker=null;
    int tothours=0;
    int totmins=0;

    public HomeFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences(UtilConstants.SharedPref, Context.MODE_PRIVATE);
        UID = sharedPreferences.getString(UtilConstants.UID, "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_fragmennt, container, false);

        Linstart= view.findViewById(R.id.lin_start);
        Linend= view.findViewById(R.id.lin_end);
        Starttime= view.findViewById(R.id.starttime);
        Endtime= view.findViewById(R.id.endtime);
        Tothours= view.findViewById(R.id.hours);

        btnStart = view.findViewById(R.id.btn_Ok);
        initCalendar();

        return view;
    }

    public void initCalendar()
    {
        Startcal=Calendar.getInstance();
        Starttime.setText(sdf.format(Startcal.getTime()));
        Endcal=Calendar.getInstance();
        Endcal.add(Calendar.HOUR_OF_DAY,2);
        Endtime.setText(sdf.format(Endcal.getTime()));

        hoursBetween(Startcal,Endcal);

        Linstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogStart();
            }
        });

        Linend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogEnd();
            }
        });
    }

    public void DialogStart()
    {
        StartPicker=new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Startcal.set(Calendar.HOUR_OF_DAY,hourOfDay);
                Startcal.set(Calendar.MINUTE,minute);
                Starttime.setText(sdf.format(Startcal.getTime()));
                Endtime.setText("");
                Tothours.setText("");
                tothours=0;
                DialogEnd();
            }
        },Startcal.get(Calendar.HOUR_OF_DAY),Startcal.get(Calendar.MINUTE),true);
        StartPicker.show();
    }

    public void DialogEnd()
    {
        EndPicker=new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                Calendar temp=Calendar.getInstance();
                temp.set(Calendar.HOUR_OF_DAY,hourOfDay);
                temp.set(Calendar.MINUTE,minute);

                if(sdf.format(temp.getTime()).equals(sdf.format(Startcal.getTime())))
                {
                    Snackbar.make(Linstart,"Both the times cannot be the Same",Snackbar.LENGTH_SHORT).show();
//                    Tothours.setText("");
//                    tothours=0;
                }
                else if(temp.before(Startcal))
                {
                    Snackbar.make(Linstart,"End time cannot Less then Start Time",Snackbar.LENGTH_SHORT).show();
//                    Tothours.setText("");
//                    tothours=0;
                }
                else
                {
                    Endcal.set(Calendar.HOUR_OF_DAY,hourOfDay);
                    Endcal.set(Calendar.MINUTE,minute);
                    Endtime.setText(sdf.format(Endcal.getTime()));
                    hoursBetween(Startcal,Endcal);
                }
            }
        },Endcal.get(Calendar.HOUR_OF_DAY),Endcal.get(Calendar.MINUTE),true);
        EndPicker.show();
    }

    public void hoursBetween(Calendar startDate, Calendar endDate) {
        long end = endDate.getTimeInMillis();
        long start = startDate.getTimeInMillis();
        long hours=TimeUnit.MILLISECONDS.toHours(Math.abs(end - start));
        long mins=TimeUnit.MILLISECONDS.toMinutes(Math.abs(end - start));
        int minutes = (int)mins % 60;
        Tothours.setText("Total hours : "+hours+"."+minutes);
        tothours=(int)hours;
        totmins=(int)mins;
    }

    @Override
    public void onStart() {
        super.onStart();

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tothours> 24) {
                    Snackbar.make(v, "Total Hours Should be less then 24", Snackbar.LENGTH_SHORT).show();
                }
                else if (tothours==0) {
                    Snackbar.make(v, "Select Start and End Time", Snackbar.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent(getActivity(), SelectionActivity.class);
                    intent.putExtra(UtilConstants.HOURS,tothours);
                    intent.putExtra(UtilConstants.MINS,totmins);
                    startActivity(intent);
                }
            }
        });
    }
}
