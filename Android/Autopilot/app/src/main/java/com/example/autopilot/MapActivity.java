package com.example.autopilot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity {
    private MapView mapView;
    private GoogleMap gMap;
    public static Double targetLatitude;
    public static Double targetLongitude;
    private static final String TAG ="MapActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapView = (MapView) findViewById(R.id.MapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this::onMapReady);
    }
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.setMyLocationEnabled(true);
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                gMap.clear();
                gMap.addMarker(new MarkerOptions().position(point).title("Selected Location"));
                // Use 'point' to handle the selected location
                targetLatitude = point.latitude;
                targetLongitude = point.longitude;
                Log.i(TAG, "onMapClick: " + Double.toString(targetLatitude) + " " + Double.toString(targetLongitude));
            }
        });
    }

    // Implement all lifecycle methods for mapView:
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}