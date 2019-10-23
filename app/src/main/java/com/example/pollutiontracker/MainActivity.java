package com.example.pollutiontracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button locationsButton, statsButton, pinpointLocButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Button to polluted-locations-map-activity
        locationsButton = findViewById(R.id.button1);
        locationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, pollutedLocsMapsActivity.class);
                startActivity(intent);
            }
        });

        //Button to graphs-charts-activity
        statsButton = findViewById(R.id.button2);

        pinpointLocButton = findViewById(R.id.button3);
        pinpointLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    Intent intent = new Intent(MainActivity.this, pinpointLocMapsActivity.class);
                    startActivity(intent);
                }
                else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }
            }
        });
    }

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
                Intent intent =  new Intent(this, pinpointLocMapsActivity.class);
                startActivity(intent);
            }else {
                toaster.shortToast("Grant location permission to access this feature",
                        MainActivity.this);
            }
        }
    }

}
