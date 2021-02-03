/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cityhopper.book.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import java.util.List;

public class LocationBroadcast extends BroadcastReceiver {
    private static final String TAG = "LUBroadcastReceiver";

    static final String ACTION_PROCESS_UPDATES =
            "LOCATION_UPDATED";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> mLocations = result.getLocations();
                    String LatLng = mLocations.get(0).getLatitude()+","+mLocations.get(0).getLongitude();
                    Utility.setmLocation(mLocations.get(0));
//                    SavedData.setLat(String.valueOf(mLocations.get(0).getLatitude()));
//                    SavedData.setLng(String.valueOf(mLocations.get(0).getLongitude()));
//                    LocationSendHelper locationSendHelper = new LocationSendHelper(context, LatLng);
//                    locationSendHelper.SendData();
                    LocationServices locationServices = new LocationServices(context);
                    locationServices.stopLocationRequest();
                    Log.d("Broadcast", LatLng);
                }
            }
        }
    }
}
