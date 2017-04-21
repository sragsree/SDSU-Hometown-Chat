package com.example.sreer.sdsuchat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapSetterActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private Button set, search;
    private TextView location;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_setter);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        set = (Button) findViewById(R.id.button_map_set);
        search = (Button) findViewById(R.id.button_map_ok);
        location = (EditText) findViewById(R.id.EditText_map_location);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        LatLng myLatLng = new LatLng(32.715786,-117.158340);
        CameraPosition myPosition = new CameraPosition.Builder().target(myLatLng).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(myPosition));
    }

    public void onMapClick(LatLng point) {
        mMap.clear();
        latitude = point.latitude;
        longitude = point.longitude;
        mMap.addMarker(new MarkerOptions().position(point).title("My Location"));
        CameraPosition myPosition = new CameraPosition.Builder()
                .target(point)
                .zoom(mMap.getCameraPosition().zoom)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(myPosition));

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goHome();
            }
        });
    }

    public void search(){
        Geocoder gc = new Geocoder(this);
        try {
            List<Address> list = gc.getFromLocationName(location.getText().toString(),1);
            if(list.size()>0) {
                mMap.clear();
                Address add = list.get(0);
                //String locality = add.getLocality();
                //Toast.makeText(this, locality, Toast.LENGTH_SHORT).show();
                LatLng point = new LatLng(add.getLatitude(), add.getLongitude());
                CameraPosition myPosition = new CameraPosition.Builder()
                        .target(point)
                        .zoom(10f)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(myPosition));
            }
            else
                Toast.makeText(this, "Could not find location", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goHome() {
        Log.i("GoHome", "goingHome");
        Intent goHome = getIntent();
        goHome.putExtra("latitude", String.valueOf(latitude));
        goHome.putExtra("longitude", String.valueOf(longitude));
        setResult(RESULT_OK, goHome);
        finish();
    }
}
