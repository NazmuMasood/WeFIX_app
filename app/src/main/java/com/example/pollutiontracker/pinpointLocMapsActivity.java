package com.example.pollutiontracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.Locale;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class pinpointLocMapsActivity extends FragmentActivity implements
        OnMapReadyCallback
        //,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    private GoogleMap mMap;
    EditText locationET; Button confirmLocationButton; ImageButton gpsButton;

    //create this at top of onCreate
    //first try
    private FusedLocationProviderClient mFusedLocationClient;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location gpsLoc, markerLoc;
    Boolean allowLocUpdt = true, tooZoomedOut = false, userHandledFirstGpsPrompt=false;
    Geocoder geocoder;
    List<Address> addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinpoint_loc_maps);

        locationET = findViewById(R.id.locationET);
        locationET.setFocusableInTouchMode(false);
        locationET.clearFocus();
        confirmLocationButton = findViewById(R.id.confirmLocButton);
        confirmLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tooZoomedOut){
                    mMap.animateCamera( CameraUpdateFactory.zoomTo( 17.0f ) );
                }
                else { toast("Confirmed location!");}
            }
        });
        gpsButton = findViewById(R.id.gpsImgButton);
        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allowLocUpdt = true;
                Loc_Update();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map1);
        mapFragment.getMapAsync(this);

        //first try
        //ADD THIS LINE
        mFusedLocationClient  = getFusedLocationProviderClient(this);
        buildGoogleApiClient();
        createLocationRequest();
        Loc_Update();
        //...
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions()
                .position(sydney)
                .title("I'm here")
                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_location_pin_32dp))
                .flat(true))
                .setDraggable(true);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        moveToCurrentLocation(sydney);*/

        //Marker onDragListener
        /*mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {}

            @Override
            public void onMarkerDrag(Marker marker) {}

            @Override
            public void onMarkerDragEnd(Marker marker) {

                LatLng newLoc = marker.getPosition();

                Location temp = new Location(LocationManager.GPS_PROVIDER);
                temp.setLatitude(newLoc.latitude);
                temp.setLongitude(newLoc.longitude);
                markerLoc = temp;

                String newLocString = markerLoc.getLatitude()+", "+markerLoc.getLongitude();
                locationET.setText(newLocString);
            }
        });*/

        toaster.longToast("Please drag map to set location..",
                pinpointLocMapsActivity.this);

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

                locationET.getText().clear();

                /*String newLocString = markerLoc.getLatitude()+", "+markerLoc.getLongitude();
                locationET.setText(newLocString);*/
                String address = getAddressFromLocation(markerLoc);
                if (address!=null){
                    locationET.setText(address);
                }
                else {
                    //locationET.setText(markerLoc.getLatitude()+", "+markerLoc.getLongitude());
                }

                //Zoom-in to location if too much zoomed-out
                float zoom = mMap.getCameraPosition().zoom;
                //toast("zoom : "+zoom+"f");
                if (zoom<16.5f){
                    tooZoomedOut = true;
                    confirmLocationButton.setText("Pinpoint location");
                }
                else {
                    tooZoomedOut = false;
                    confirmLocationButton.setText("CONFIRM LOCATION");
                }
            }
        });

    }

    //get current location - First try- stackoverflow
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        //toast("googleClient connected");
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location!=null){
                                        //toast("mFusedLocation getLastLocation");
                                        //LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                                        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
                                        //locationET.setText(location.getLatitude()+", "+location.getLongitude());
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        //LOG
                    }
                })
                .addApi(LocationServices.API)
                .build();
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private void Loc_Update() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    if (ContextCompat.checkSelfPermission(pinpointLocMapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,new LocationCallback(){
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                for (Location location : locationResult.getLocations()) {
                                    //Do what you want with location
                                    //like update camera
                                    gpsLoc = location;
                                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16f));
                                    //locationET.setText(location.getLatitude()+", "+location.getLongitude());
                                    //toast("loc_update");
                                    moveToGPSLocation();
                                }

                            }
                        },null);

                    }
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(pinpointLocMapsActivity.this, 2001);
                                break;
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.

                            break;
                    }
                }}
        });//task-listener
        //builder.setAlwaysShow(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (resultCode) {
            case Activity.RESULT_OK:
                // All required changes were successfully made
                toast("User permitted gps on");

                userHandledFirstGpsPrompt = true;

                Loc_Update();
                break;
            case Activity.RESULT_CANCELED:
                // The user was asked to change settings, but chose not to
                toast("User denied gps on");

                //Very first time when activity starts and user cancels turn-on-gps prompt
                if (!userHandledFirstGpsPrompt) {
                    if (ContextCompat.checkSelfPermission(pinpointLocMapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) { //Last location available
                                    toast("last location ");
                                    gpsLoc = location;
                                    moveToGPSLocation();

                                } else { //No last location available
                                    toast("custom location");
                                    LatLng shaplaChottorLatLng = new LatLng(23.726623, 90.421576);

                                    Location temp = new Location(LocationManager.GPS_PROVIDER);
                                    temp.setLatitude(shaplaChottorLatLng.latitude);
                                    temp.setLongitude(shaplaChottorLatLng.longitude);

                                    gpsLoc = temp; markerLoc = temp;

                                    //This is called only once when activity just started and user denied GPS..
                                    //..which gives a whole zoomed-out view of the country
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(markerLoc.getLatitude()
                                            , markerLoc.getLongitude()), 6.5f));

                                    tooZoomedOut = true;
                                }
                                userHandledFirstGpsPrompt = true;
                            }
                        });
                    }
                }

                break;
            default:
                break;
        }
    }

    private void moveToGPSLocation(){
        if(allowLocUpdt) {
            Location location = gpsLoc;
            if (location != null) {
                markerLoc = gpsLoc;

                // "Loading location..." <-- locationET hint
                locationET.getText().clear();

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude()
                        , location.getLongitude()), 16f));

                String address = getAddressFromLocation(markerLoc);
                if (address!=null){
                    locationET.setText(address);
                }
                else {
                    //locationET.setText(markerLoc.getLatitude()+", "+markerLoc.getLongitude());
                }
            }
            allowLocUpdt = false;
        }
    }

    private String getAddressFromLocation(Location markerLoc) {
        String addressToDisp = "";
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(markerLoc.getLatitude(), markerLoc.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            if (addresses!=null) {
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();

                addressToDisp = address;
                /*String toastAddress = "address: "+address+
                        "\ncity: "+city+"\nstate: "+state+
                        "\ncountry :"+country+"\npostalCode: "+postalCode+
                        "\nknownName: "+knownName;
                toaster.longToast(toastAddress, pinpointLocMapsActivity.this);*/
                return addressToDisp;
            }
            else {
                locationET.setText("Location unavailable. Please try again...");
                return null;
            }
        }
        catch (Exception e){e.printStackTrace();}

        //toast("Internet is unstable. Please try again..");
        if (userHandledFirstGpsPrompt) {
            locationET.setText("Location unavailable. Please try again...");
            if (!activeNetwork()){
                toaster.longToast("No internet connection. Please try again...",
                        pinpointLocMapsActivity.this);
            }
        }

        return null;
    }
    //First try end

    private void toast(String message) {
        try {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    //Check if location permission on
    /*
    int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean checkPermissions(){
        return (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){
            if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getMyLocation();
            }else {
                toaster.shortToast("Grant location permission to access this feature",
                        pinpointLocMapsActivity.this);
                Intent intent =  new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
    }
    */


    //Method to zoom in to location
    private void moveToCurrentLocation(LatLng currentLocation)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 4000, null);
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

    public boolean activeNetwork () {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        return isConnected;
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        //setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }*/

    /*@Override
    public void onBackPressed() {
        super.onBackPressed();
    }*/
}
