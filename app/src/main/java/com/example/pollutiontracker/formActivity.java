package com.example.pollutiontracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

public class formActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText locationET;
    Spinner categorySpinner, sourceSpinner, extentSpinner;
    ImageButton pictureIB; Button submitButton;

    Boolean allFieldsSatisfy = false;

    String[] categories, sources, extents;
    String selectedCategory, selectedSource, selectedExtent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        locationET = findViewById(R.id.locationET);
        categorySpinner = findViewById(R.id.categorySpinner);
        sourceSpinner = findViewById(R.id.sourceSpinner);
        extentSpinner = findViewById(R.id.extentSpinner);
        pictureIB = findViewById(R.id.pictureIB);
        submitButton = findViewById(R.id.submitButton);

        categories = getResources().getStringArray(R.array.category_array);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter1);
        categorySpinner.setOnItemSelectedListener(this);

        sources = getResources().getStringArray(R.array.source_array);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, sources);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sourceSpinner.setAdapter(adapter2);
        sourceSpinner.setOnItemSelectedListener(this);

        extents = getResources().getStringArray(R.array.extent_array);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, extents);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        extentSpinner.setAdapter(adapter3);
        extentSpinner.setOnItemSelectedListener(this);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allFieldsSatisfy)
                    toaster.longToast("Your report is being uploaded", formActivity.this);
                else
                    toaster.shortToast("Please fill all input fields", formActivity.this);
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.categorySpinner:
                toaster.shortToast("You selected category "+categories[position], formActivity.this);
                break;
            case R.id.sourceSpinner:
                toaster.shortToast("You selected source "+sources[position], formActivity.this);
                break;
            case R.id.extentSpinner:
                toaster.shortToast("You selected extent "+extents[position], formActivity.this);
                break;
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) { }
}
