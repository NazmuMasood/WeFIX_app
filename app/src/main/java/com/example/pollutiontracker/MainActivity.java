package com.example.pollutiontracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button locationsButton, statsButton;

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
    }
}
