package com.example.pollutiontracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    Button locationsButton, statsButton, pinpointLocButton, formButton;
    SwipeRefreshLayout swipeLayout; BarChart barChart; TextView summaryTV;

    GeoFire geoFire; DatabaseReference ref;
    ArrayList<Report> reports;String sources = ""; int flagCount = 0;
    LinkedHashMap<String, String> reportStatMap; Set<String> sourceHashSet;
    HashMap<String, Integer> eachCategoryFlagCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeLayout = findViewById(R.id.swipeLayout);
        barChart = findViewById(R.id.barGraphMain);
        summaryTV = findViewById(R.id.summaryMainTV);
        reports = new ArrayList<>();
        eachCategoryFlagCount = new HashMap<>();
        reportStatMap = new LinkedHashMap<>();
        sourceHashSet = new HashSet<>();
        prepareEachCategoryFlagCountHashMap();
        prepareReportStatHashMap();
        updateReportStatTV();
        //Gathering statistics for graph
        fetchStats();

        //SwipeLayout
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (activeNetwork()) {
                    finish();
                    startActivity(getIntent());
                }
                else {
                    toaster.shortToast("No internet connection. Please try refreshing by swiping down..", MainActivity.this);
                    if (swipeLayout.isRefreshing()){
                        swipeLayout.setRefreshing(false);
                    }
                }
                //fetchStats();
            }
        });

        //Button to polluted-locations-map-activity
        locationsButton = findViewById(R.id.button1);
        locationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activeNetwork()) {
                    if (checkPermission()) {
                        Intent intent = new Intent(MainActivity.this, pollutedLocsMapsActivity.class);
                        startActivity(intent);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    }
                }
                else {
                    toaster.shortToast("No internet connection. Please try again..", MainActivity.this);
                }
            }
        });

        /*//Button to graphs-charts-activity
        statsButton = findViewById(R.id.button2);
        statsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, reportActivity.class);
                startActivity(intent);
            }
        });

        pinpointLocButton = findViewById(R.id.button3);
        pinpointLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeNetwork()) {
                    if (checkPermission()) {
                        Intent intent = new Intent(MainActivity.this, pinpointLocMapsActivity.class);
                        startActivity(intent);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    }
                }
                else {
                    toaster.shortToast("No internet connection. Please try again..", MainActivity.this);
                }
            }
        });

        formButton = findViewById(R.id.button4);
        formButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, formActivity.class);
                startActivity(intent);
            }
        });*/
    }

    private void fetchStats() {
        if(activeNetwork()) {
            ref = FirebaseDatabase.getInstance().getReference("pollution-tracker/reports");
            final DatabaseReference geoFireRef = FirebaseDatabase.getInstance().getReference("pollution-tracker/geofire");
            geoFire = new GeoFire(geoFireRef);
            GeoQuery geoQuery = geoFire.queryAtLocation(
                    new GeoLocation(23.726623, 90.421576), 450.0);
            addEventListenerToGeoQuery(geoQuery);

           /* new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(false);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                            setGraph();
                        }
                    }, 1000);
                }
            }, 2000);*/
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //swipeLayout.setRefreshing(false);
                    setGraph();
                }
            }, 3000);
            //swipeLayout.setRefreshing(false);
        }
        else {
            toaster.shortToast("No internet connection. Please try refreshing by swiping down..", MainActivity.this);
            if (swipeLayout.isRefreshing()){
                swipeLayout.setRefreshing(false);
            }
        }
    }

    private void setGraph() {
        //x-axis
        String[] tmpCategories = getResources().getStringArray(R.array.category_array);
        ArrayList<String> categories = new ArrayList<>(Arrays.asList(tmpCategories));
        //Air flag count
        ArrayList<BarEntry> barEntries1 = new ArrayList<>();
        barEntries1.add(new BarEntry(1, eachCategoryFlagCount.get(categories.get(0))));
        BarDataSet barDataSet1 = new BarDataSet(barEntries1, categories.get(0));
        barDataSet1.setColor(ContextCompat.getColor(MainActivity.this, R.color.colorAir));
        //Water flag count
        ArrayList<BarEntry> barEntries2 = new ArrayList<>();
        barEntries2.add(new BarEntry(2, eachCategoryFlagCount.get(categories.get(1))));
        BarDataSet barDataSet2 = new BarDataSet(barEntries2, categories.get(1));
        barDataSet2.setColor(ContextCompat.getColor(MainActivity.this,R.color.colorWater));
        //Noise flag count
        ArrayList<BarEntry> barEntries3 = new ArrayList<>();
        barEntries3.add(new BarEntry(3, eachCategoryFlagCount.get(categories.get(2))));
        BarDataSet barDataSet3 = new BarDataSet(barEntries3, categories.get(2));
        barDataSet3.setColor(ContextCompat.getColor(MainActivity.this,R.color.colorNoise));
        //Land flag count
        ArrayList<BarEntry> barEntries4 = new ArrayList<>();
        barEntries4.add(new BarEntry(4, eachCategoryFlagCount.get(categories.get(3))));
        BarDataSet barDataSet4 = new BarDataSet(barEntries4, categories.get(3));
        barDataSet4.setColor(ContextCompat.getColor(MainActivity.this,R.color.colorLand));
        //Others flag count
        ArrayList<BarEntry> barEntries5 = new ArrayList<>();
        barEntries5.add(new BarEntry(5, eachCategoryFlagCount.get(categories.get(4))));
        BarDataSet barDataSet5 = new BarDataSet(barEntries5, categories.get(4));
        barDataSet5.setColor(ContextCompat.getColor(MainActivity.this,R.color.colorOthers));

        //setting the data to the bar-chart
        BarData barData = new BarData(barDataSet1, barDataSet2, barDataSet3, barDataSet4, barDataSet5);
        barChart.setData(barData);
        barChart.animateY(1000);
        barChart.setFitBars(true);

        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);
        barChart.invalidate();

        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);

        //removing the background grid on graph
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getXAxis().setDrawGridLines(false);
    }

    private void addEventListenerToGeoQuery(GeoQuery geoQuery) {
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d("onKeyEnterGeoFire", String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
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
                        /*if (report.imagesUrl != null) {
                            if(locImgPlaceholerRL.getVisibility()==View.VISIBLE){
                                locImgPlaceholerRL.setVisibility(View.GONE);
                            }
                            images.addAll(report.imagesUrl);
                            for (int i=0; i<report.imagesUrl.size(); i++) {
                                imgDescriptions.add(report.address);
                            }
                            adapter.setItems(images, imgDescriptions);
                            adapter.notifyDataSetChanged();
                        }*/
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
        String reportStat = "Place: ALL";//----- Report Stats -----
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
        summaryTV.setText(reportStat);
    }

    private void updateReportStatHashMap(String statTypeKey, String value){
        reportStatMap.put(statTypeKey, value);
    }

    private void prepareReportStatHashMap() {
        //reportStatMap.put("Place", "ALL");
        /*reportStatMap.put("Location", 23.726623+", "+ 90.421576);
        reportStatMap.put("Total population", "NA");
        reportStatMap.put("Population level", "NA");
        reportStatMap.put("Measure", "NA(AQI)");
        reportStatMap.put("Population affected", "NA");*/
        reportStatMap.put("Sources", "NA");
        reportStatMap.put("No. of flags", "NA");
    }

    private void prepareEachCategoryFlagCountHashMap() {
        String[] categories = getResources().getStringArray(R.array.category_array);
        for (String category : categories){
            eachCategoryFlagCount.put(category, 0);
        }
    }

    /*--------------GAP----------*/
    int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    //Check permissions
    private boolean checkPermission(){
        return (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){
            if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Intent intent =  new Intent(this, pollutedLocsMapsActivity.class);
                startActivity(intent);
            }else {
                toaster.shortToast("Grant location permission to access this feature",
                        MainActivity.this);
            }
        }
    }

    public boolean activeNetwork () {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();

        return isConnected;
    }
}
