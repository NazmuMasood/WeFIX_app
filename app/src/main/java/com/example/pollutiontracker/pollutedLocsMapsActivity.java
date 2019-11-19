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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.List;
import java.util.Locale;

public class pollutedLocsMapsActivity extends FragmentActivity
        implements OnMapReadyCallback
{

    private GoogleMap mMap;
    DatabaseReference ref;
    Button reportButton, pollutedLocSearchTypeButton, pollutedLocCustomConfirmButton;
    EditText pollutedLocCustomSearchET; ImageView pollutedLocMarkerIV;
    int pollutedLocSearchType = 0;
    Location markerLoc; Geocoder geocoder; List<Address> addresses;
    Boolean tooZoomedOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polluted_locs_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //handling the custom 'reported location' search implementation
        reportButton = findViewById(R.id.reportButton);

        pollutedLocSearchTypeButton = findViewById(R.id.pollutedLocSearchTypeButton);
        pollutedLocCustomConfirmButton = findViewById(R.id.pollutedLocCustomConfirmButton);
        pollutedLocCustomConfirmButton.setVisibility(View.GONE);

        pollutedLocCustomSearchET = findViewById(R.id.pollutedLocCustomSearchET);
        pollutedLocCustomSearchET.setFocusableInTouchMode(false);
        pollutedLocCustomSearchET.clearFocus();
        pollutedLocCustomSearchET.setVisibility(View.GONE);

        pollutedLocMarkerIV = findViewById(R.id.pollutedLocMarkerIV);
        pollutedLocMarkerIV.setVisibility(View.GONE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        pollutedLocCustomConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tooZoomedOut){
                    mMap.animateCamera( CameraUpdateFactory.zoomTo( 14.0f ) );
                }
                else {
                    toaster.shortToast("Confirmed location!\n"
                            +markerLoc.getLatitude()+", "+markerLoc.getLongitude()
                            +"\n"+pollutedLocCustomSearchET.getText(), pollutedLocsMapsActivity.this);

                }
            }
        });
        pollutedLocSearchTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pollutedLocSearchType==0){
                    pollutedLocSearchType = 1;
                    pollutedLocSearchTypeButton.setText("ALL");
                    pollutedLocCustomConfirmButton.setVisibility(View.VISIBLE);
                    pollutedLocCustomSearchET.setVisibility(View.VISIBLE);
                    pollutedLocMarkerIV.setVisibility(View.VISIBLE);
                    mMap.clear();

                    LatLng midLatLng = mMap.getCameraPosition().target;
                    Location temp = new Location(LocationManager.GPS_PROVIDER);
                    temp.setLatitude(midLatLng.latitude);
                    temp.setLongitude(midLatLng.longitude);
                    markerLoc = temp;
                    String address = getAddressFromLocation(markerLoc);
                    if (address!=null){
                        pollutedLocCustomSearchET.setText(address);
                    }
                    else {
                        pollutedLocCustomSearchET.setText(markerLoc.getLatitude()+", "+markerLoc.getLongitude());
                    }
                }
                else {
                    pollutedLocSearchType = 0;
                    pollutedLocSearchTypeButton.setText("CUSTOM");
                    pollutedLocCustomConfirmButton.setVisibility(View.GONE);
                    pollutedLocCustomSearchET.setVisibility(View.GONE);
                    pollutedLocMarkerIV.setVisibility(View.GONE);
                    getPollutedLocations();
                }
            }
        });
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(pollutedLocsMapsActivity.this, pinpointLocMapsActivity.class);
                startActivity(intent);
            }
        });

        //getting reported locations from firebase and populating map
        getPollutedLocations();

        //LatLng rajshahi = new LatLng(24.367350, 88.636055);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(rajshahi));
        LatLng shaplaChottor = new LatLng(23.726623, 90.421576);
        moveToLocation(shaplaChottor);

        //Map cameraMovedListener
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                //get LatLng at the center by calling
                LatLng midLatLng = mMap.getCameraPosition().target;

                Location temp = new Location(LocationManager.GPS_PROVIDER);
                temp.setLatitude(midLatLng.latitude);
                temp.setLongitude(midLatLng.longitude);
                markerLoc = temp;

                pollutedLocCustomSearchET.getText().clear();

                String address = getAddressFromLocation(markerLoc);
                if (address!=null){
                    pollutedLocCustomSearchET.setText(address);
                }
                else {
                    pollutedLocCustomSearchET.setText(markerLoc.getLatitude()+", "+markerLoc.getLongitude());
                }

                //Zoom-in to location if too much zoomed-out
                float zoom = mMap.getCameraPosition().zoom;
                if (zoom<14f){
                    tooZoomedOut = true;
                    pollutedLocCustomConfirmButton.setText("PINPOINT");
                }
                else {
                    tooZoomedOut = false;
                    pollutedLocCustomConfirmButton.setText("CONFIRM");
                }
            }
        });
    }

    private void getPollutedLocations(){
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,7f));
        // Zoom in, animating the camera.
        //mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(6.5f), 2000, null);
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

    private String getAddressFromLocation(Location markerLoc) {
        String addressToDisp = "";
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(markerLoc.getLatitude(), markerLoc.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            if (addresses!=null) {
                String address = addresses.get(0).getAddressLine(0);
                addressToDisp = address;
                return addressToDisp;
            }
            else {
                pollutedLocCustomSearchET.setText("Location unavailable. Please try again...");
                return null;
            }
        }
        catch (Exception e){e.printStackTrace();}
        return null;
    }
}
