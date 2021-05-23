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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private AppCompatButton syncButton;
    private AppCompatTextView locationTV, dateTv, fazarTv, dhurTv, asrTv, magribTv, ishaTv, sunRiseTv, sunSetTv, midNightTv;
    private SharedPreferences locationPref;
    private String latitude, longitude, address;
    private String LOCATION_PREFERENCE = "location_preferences";
    private CommonBridge commonBridge;
    private ProgressBar progressBar;
    LocationManager manager;
    private static final int REQUEST_LOCATION = 1;
    private SimpleDateFormat formatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init
        syncButton = findViewById(R.id.syncBtn);
        locationTV = findViewById(R.id.locationTV);
        dateTv = findViewById(R.id.dateTV);
        fazarTv = findViewById(R.id.fazrTV);
        dhurTv = findViewById(R.id.dhurTV);
        asrTv = findViewById(R.id.asrTV);
        magribTv = findViewById(R.id.magribTV);
        ishaTv = findViewById(R.id.ishaTV);
        sunRiseTv = findViewById(R.id.sunRiseTV);
        sunSetTv = findViewById(R.id.sunSetTV);
        midNightTv = findViewById(R.id.midNightTV);
        progressBar = findViewById(R.id.progressBar);

        //sync data variable
        formatter = new SimpleDateFormat("dd MMMM yyyy");
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
        //set data
        dateTv.setText(formatter.format(now));
        sunRiseTv.setText("Sun rise: " + prayerTimes.get(1));
        sunSetTv.setText("Sun set: " + prayerTimes.get(4));
        fazarTv.setText(prayerTimes.get(0));
        dhurTv.setText(prayerTimes.get(2));
        asrTv.setText(prayerTimes.get(3));
        magribTv.setText(prayerTimes.get(5));
        ishaTv.setText(prayerTimes.get(6));
        midNightTv.setText(prayerTimes.get(7));


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