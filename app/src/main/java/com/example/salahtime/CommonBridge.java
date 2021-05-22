package com.example.salahtime;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CommonBridge {
    private ProgressBar progressBar;
    private double latitiude, longitude;
    SharedPreferences locationPref;
    private static Context context;
    String address = "";
    LocationRequest locationRequest;

    public CommonBridge(Context context, ProgressBar progressBar) {
        this.context = context;
        this.progressBar = progressBar;
    }




    public void getLocation() {
        progressBar.setVisibility(View.VISIBLE);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(context)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(context).removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int lastLocationIndex = locationResult.getLocations().size() - 1;
                            latitiude = locationResult.getLocations().get(lastLocationIndex).getLatitude();
                            longitude = locationResult.getLocations().get(lastLocationIndex).getLongitude();
                            address = getAddressFromLocation(latitiude, longitude);
                            if (!address.equals("")) {
                                saveLocationData(latitiude, longitude, address);
                            } else {
                                Toast.makeText(context, "Empty address or location!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }, Looper.getMainLooper());

    }

    private void saveLocationData(double latitiude, double longitude, String address) {
        locationPref = context.getSharedPreferences("location_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = locationPref.edit();
        editor.putString("lat", String.valueOf(latitiude));
        editor.putString("lon", String.valueOf(longitude));
        editor.putString("address", address);
        editor.commit();

        //go to main activity
        progressBar.setVisibility(View.GONE);
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private String getAddressFromLocation(double latitiude, double longitude) {
        String address = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latitiude, longitude, 1);
            if (addresses.size() > 0) {

                address = addresses.get(0).getAddressLine(0);
//                Log.d("Home: Locality: ", addresses.get(0).getLocality());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return address;
    }

    //check gps location on or off
    public void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Turn on your GPS!");
        builder.setMessage("You should turn on your location, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

}
