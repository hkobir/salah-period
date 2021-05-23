package com.example.salahtime;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

public class SplashActivity extends AppCompatActivity {
    private CommonBridge commonBridge;
    private ProgressBar progressBar;
    LocationManager locationManager;
    SharedPreferences locationPref;
    private String LOCATION_PREFERENCE = "location_preferences";
    private static final int REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //init
        progressBar = findViewById(R.id.progressLoad);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        commonBridge = new CommonBridge(this, progressBar);
        locationPref = getSharedPreferences(LOCATION_PREFERENCE, Context.MODE_PRIVATE);

        if (locationPref.getString("lat", "").equals("") || locationPref.getString("lon", "").equals("")) {

            if (ActivityCompat.checkSelfPermission(
                    SplashActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    SplashActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) SplashActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            } else {
                locationRequest();
            }


        } else {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }

    }

    private void locationRequest() {

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            commonBridge.buildAlertMessageNoGps();

        } else {
            commonBridge.getLocation();
        }


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

    @Override
    public void onBackPressed() {
        finish();
    }
}