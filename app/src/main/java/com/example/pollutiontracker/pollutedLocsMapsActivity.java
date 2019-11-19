package com.example.pollutiontracker;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class pollutedLocsMapsActivity extends FragmentActivity
        implements OnMapReadyCallback
{

    private GoogleMap mMap;
    DatabaseReference ref; Button reportButton;
    ArrayList<Double> lat, lng; ArrayList<String> addresses; ArrayList<LatLng> latLngs;
    LinearLayout list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polluted_locs_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        reportButton = findViewById(R.id.reportButton);
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(pollutedLocsMapsActivity.this, pinpointLocMapsActivity.class);
                startActivity(intent);
            }
        });

        ref = FirebaseDatabase.getInstance().getReference("pollution-tracker/reports");
        // Attach a listener to read the data at our 'reports' reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Report report = snapshot.getValue(Report.class);
                    Log.d("onDataChange", "report: location="+report.location.latitude+", "+report.location.longitude);
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(report.location.latitude, report.location.longitude))
                            .title(report.address)
                            .icon(getMarkerIcon(report.category))
                            .flat(true));
                }
                toaster.shortToast( "Found "+dataSnapshot.getChildrenCount()+" different polluted locations",
                        pollutedLocsMapsActivity.this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        //LatLng rajshahi = new LatLng(24.367350, 88.636055);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(rajshahi));
        LatLng shaplaChottor = new LatLng(23.726623, 90.421576);
        moveToLocation(shaplaChottor);
    }

    private BitmapDescriptor getMarkerIcon(String category){
        if (category.equals("Air")) {
           return bitmapDescriptorFromVector(pollutedLocsMapsActivity.this, R.drawable.ic_marker_air_32dp);
        }
        else if (category.equals("Water")) {
            return bitmapDescriptorFromVector(pollutedLocsMapsActivity.this, R.drawable.ic_marker_water_32dp);
        }
        else if (category.equals("Noise")) {
            return bitmapDescriptorFromVector(pollutedLocsMapsActivity.this, R.drawable.ic_marker_noise_32dp);
        }
        else if (category.equals("Land")) {
            return bitmapDescriptorFromVector(pollutedLocsMapsActivity.this, R.drawable.ic_marker_land_32dp);
        }
        else if (category.equals("Others")) {
            return bitmapDescriptorFromVector(pollutedLocsMapsActivity.this, R.drawable.ic_marker_others_32dp);
        }
        return bitmapDescriptorFromVector(pollutedLocsMapsActivity.this, R.drawable.ic_marker_others_32dp);
    }

    private void moveToLocation(LatLng currentLocation)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,10f));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(6.5f), 2000, null);
    }

    //bitmapDescriptor method to convert vectorAsset to bitmap
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }



}
