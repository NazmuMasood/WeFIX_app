package com.example.pollutiontracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class reportActivity extends AppCompatActivity {
    GeoFire geoFire; DatabaseReference ref; Location markerLoc; String place;
    ArrayList<Report> reports;
    ArrayList<String> images; ArrayList<String> imgDescriptions;
    TextView reportStatTV;
    SliderView sliderView; SliderAdapterExample adapter;
    String sources = ""; int flagCount = 0;
    LinkedHashMap<String, String> reportStatMap; Set<String> sourceHashSet;
    HashMap<String, Integer> eachCategoryFlagCount;
    Button graphButton; RelativeLayout locImgPlaceholerRL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            markerLoc = getIntent().getParcelableExtra("MarkerLoc");
            place = getIntent().getStringExtra("MarkerAddress");
        }
        else {return;}

        locImgPlaceholerRL = findViewById(R.id.locImgPlaceholderRL);
        graphButton = findViewById(R.id.graphButton);
        graphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(reportActivity.this, graphActivity.class);
                intent.putExtra("EachCategoryFlagCount", eachCategoryFlagCount);
                startActivity(intent);
            }
        });
        reportStatTV = findViewById(R.id.reportStatTV);
        reportStatMap = new LinkedHashMap<>();
        sourceHashSet = new HashSet<>();
        eachCategoryFlagCount = new HashMap<>();
        prepareEachCategoryFlagCountHashMap();
        prepareReportStatHashMap();
        updateReportStatTV();
        reports = new ArrayList<>();
        images = new ArrayList<>();
        imgDescriptions = new ArrayList<>();
        ref = FirebaseDatabase.getInstance().getReference("pollution-tracker/reports");
        final DatabaseReference geoFireRef = FirebaseDatabase.getInstance().getReference("pollution-tracker/geofire");
        geoFire = new GeoFire(geoFireRef);
        GeoQuery geoQuery = geoFire.queryAtLocation(
                new GeoLocation(markerLoc.getLatitude(), markerLoc.getLongitude()), 1.0);
        addEventListenerToGeoQuery(geoQuery);

        //setting up the image_slider
        sliderView = findViewById(R.id.imageSlider);
        adapter = new SliderAdapterExample(this, images, imgDescriptions);
        sliderView.setSliderAdapter(adapter);
    }

    private void updateSourcesKeyValue(){
        sources = "";
        boolean firstItem = true;
        for (String s : sourceHashSet){
            if (firstItem){
                sources = sources + s;
                firstItem = false;
            } else {
                sources = sources + ", "+ s;
            }
        }
    }

    private void updateReportStatTV(){
        String reportStat = "----- Report Stats -----";
        for (LinkedHashMap.Entry<String, String> entry : reportStatMap.entrySet()){
            if (entry.getKey().equals("Sources") && !sourceHashSet.isEmpty()){
                reportStat = reportStat + "\n" + entry.getKey() + ": " + sources;
            }
            else if (entry.getKey().equals("No. of flags")){
                reportStat = reportStat + "\n" + entry.getKey() + ": " + flagCount;
            }
            else {
                reportStat = reportStat + "\n" + entry.getKey() + ": " + entry.getValue();
            }
        }
        reportStatTV.setText(reportStat);
    }

    private void updateReportStatHashMap(String statTypeKey, String value){
        reportStatMap.put(statTypeKey, value);
    }

    private void prepareReportStatHashMap() {
        if (place!=null) {
            reportStatMap.put("Place", place);
        }else {reportStatMap.put("Place", "NA");}
        reportStatMap.put("Location", markerLoc.getLatitude()+", "+markerLoc.getLongitude());
        reportStatMap.put("Total population", "NA");
        reportStatMap.put("Population level", "NA");
        reportStatMap.put("Measure", "NA(AQI)");
        reportStatMap.put("Population affected", "NA");
        reportStatMap.put("Sources", "NA");
        reportStatMap.put("No. of flags", "NA");
    }

    private void prepareEachCategoryFlagCountHashMap() {
        String[] categories = getResources().getStringArray(R.array.category_array);
        for (String category : categories){
            eachCategoryFlagCount.put(category, 0);
        }
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
                        reports.add(report);

                        //Updating the reports stat textview
                        if (!sourceHashSet.contains(report.source)){
                            sourceHashSet.add(report.source);
                            updateSourcesKeyValue();
                            updateReportStatHashMap("Sources", sources);
                        }
                        eachCategoryFlagCount.put(report.category, eachCategoryFlagCount.get(report.category)+1);
                        flagCount++;
                        updateReportStatHashMap("No. of flags", Integer.toString(flagCount));
                        updateReportStatTV();

                        //Populating the image_slider
                        if (report.imagesUrl != null) {
                            if(locImgPlaceholerRL.getVisibility()==View.VISIBLE){
                                locImgPlaceholerRL.setVisibility(View.GONE);
                            }
                            images.addAll(report.imagesUrl);
                            for (int i=0; i<report.imagesUrl.size(); i++) {
                                imgDescriptions.add(report.address);
                            }
                            adapter.setItems(images, imgDescriptions);
                            adapter.notifyDataSetChanged();
                        }
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
                Log.d("onGeoQReady", "All initial data has been loaded and events have been fired!");
                /*for (Report report : reports){
                    Log.d("onGeoQReady", "address: "+report.address);
                    if (report.imagesUrl != null) {
                        images.addAll(report.imagesUrl);
                        for (int i=0; i<report.imagesUrl.size(); i++) {
                            imgDescriptions.add(report.address);
                        }
                    }
                }
                for (String s: images) {
                    Log.d("onGeoQReady", "imageUrl: " + s);
                }
                for (String s: imgDescriptions) {
                    Log.d("onGeoQReady", "imgDescription: " + s);
                }
                Log.d("onGeoQReady", "Reports count: " + reports.size());*/
                updateReportStatTV();
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.d("onGeoQueryError", "There was an error with this query: " + error);
            }
        });


    }
}
