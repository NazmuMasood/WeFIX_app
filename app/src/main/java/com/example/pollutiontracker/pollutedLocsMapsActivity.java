package com.example.pollutiontracker;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
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

import java.util.ArrayList;
import java.util.Arrays;

public class pollutedLocsMapsActivity extends FragmentActivity
        implements OnMapReadyCallback
{

    private GoogleMap mMap;
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

        // Add a marker in Sydney and move the camera
        //marker's url: https://www.flaticon.com/free-icon/placeholder_149060
        ArrayList<Double> lat = new ArrayList<>(Arrays.asList( 24.367350, 24.368346, 24.366773));
        ArrayList<Double> lng = new ArrayList<>(Arrays.asList( 88.636055, 88.638465, 88.639275));

        ArrayList<LatLng> latLngs = new ArrayList<>();
        for (int i=0; i<lat.size(); i++) {
            latLngs.add(new LatLng( lat.get(i), lng.get(i) ));
            Log.i("LatLngs","New location: "+latLngs.get(i).latitude+", "+latLngs.get(i).longitude);
        }

        for (LatLng point : latLngs) {
            mMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title("A Marker in Rajshahi")
                    .icon(bitmapDescriptorFromVector(this, R.drawable.ic_blue_marker_32dp))
                    .flat(true));
        }

        LatLng rajshahi = new LatLng(24.367350, 88.636055);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(rajshahi));
        moveToCurrentLocation(rajshahi);
    }

    private void moveToCurrentLocation(LatLng currentLocation)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
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
