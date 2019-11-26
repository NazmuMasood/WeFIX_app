package com.example.pollutiontracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class reportActivity extends AppCompatActivity {
    GeoFire geoFire; DatabaseReference ref; Location markerLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            markerLoc = getIntent().getParcelableExtra("MarkerLoc");
        }
        else {return;}

        ref = FirebaseDatabase.getInstance().getReference("pollution-tracker/reports");
        final DatabaseReference geoFireRef = FirebaseDatabase.getInstance().getReference("pollution-tracker/geofire");
        geoFire = new GeoFire(geoFireRef);
        GeoQuery geoQuery = geoFire.queryAtLocation(
                new GeoLocation(markerLoc.getLatitude(), markerLoc.getLongitude()), 1.0);
        addEventListenerToGeoQuery(geoQuery);
    }

    private void addEventListenerToGeoQuery(GeoQuery geoQuery) {
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d("onKeyEnterGeoFire", String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
                //String loc = String.valueOf(location.latitude) + ", " + String.valueOf(location.longitude);
                //Log.d("geoQueryEventListener", "onKeyEntered: " + key + " @ " + loc);
                FirebaseDatabase.getInstance()
                        .getReference("pollution-tracker/reports/"+key)
                        .getRef().addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Report report = dataSnapshot.getValue(Report.class);
                        Log.d("onDataChangeGeoFire", "report: location ="+report.location.latitude+", "+report.location.longitude);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onKeyExited(String key) {
                Log.d("onKeyExitGeoFire", String.format("Key %s is no longer in the search area", key));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d("onKeyMoveGeoFire", String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {
                Log.d("onGeoQuery", "All initial data has been loaded and events have been fired!");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.d("onGeoQueryError", "There was an error with this query: " + error);
            }
        });
    }
}
