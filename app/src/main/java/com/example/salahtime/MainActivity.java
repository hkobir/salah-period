package com.example.salahtime;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private AppCompatButton syncButton;
    private AppCompatTextView locationTV, salahTimeTV;
    private SharedPreferences locationPref;
    private String latitude, longitude, address;
    private String LOCATION_PREFERENCE = "location_preferences";
    private CommonBridge commonBridge;
    private ProgressBar progressBar;
    LocationManager manager;
    private static final int REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init
        syncButton = findViewById(R.id.syncBtn);
        locationTV = findViewById(R.id.locationTV);
        salahTimeTV = findViewById(R.id.salahTimesTV);
        progressBar = findViewById(R.id.progressBar);

        //sync data variable
        manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        commonBridge = new CommonBridge(this, progressBar);
        locationPref = this.getSharedPreferences(LOCATION_PREFERENCE, Context.MODE_PRIVATE);
        latitude = locationPref.getString("lat", "");
        longitude = locationPref.getString("lon", "");
        address = locationPref.getString("address", "");


        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(
                        MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                } else {
                    locationRequest();
                }
            }
        });

        populateSalahTime();
    }

    private void locationRequest() {

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            commonBridge.buildAlertMessageNoGps();

        } else {
            commonBridge.getLocation();
        }


    }

    private void populateSalahTime() {
        locationTV.setText(address);

//get salah period
        PrayerTime prayers = new PrayerTime();

        prayers.setTimeFormat(prayers.Time24);
        prayers.setCalcMethod(prayers.MWL); //used method
        prayers.setAsrJuristic(prayers.Shafii);
        prayers.setAdjustHighLats(prayers.AngleBased);
        int[] offsets = {0, 0, 0, 0, 0, 0, 0, 0}; // {Fajr,Sunrise,Dhuhr,Asr,Sunset,Maghrib,Isha, Midnight}
        prayers.tune(offsets);

        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);

        ArrayList<String> prayerTimes = prayers.getPrayerTimes(
                cal,
                Double.valueOf(latitude),
                Double.valueOf(longitude),
                prayers.getBaseTimeZone()
        );
        ArrayList<String> prayerNames = prayers.getTimeNames();
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < prayerTimes.size(); i++) {
            str.append(prayerNames.get(i) + "   ----------   " + prayerTimes.get(i));
            str.append("\n");
            str.append("\n");
//            System.out.println(prayerNames.get(i) + " - " + prayerTimes.get(i));
        }
        salahTimeTV.setText(str);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                commonBridge.getLocation();
            } else {
                Toast.makeText(this, "GPS permission needed!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}