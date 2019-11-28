package com.example.pollutiontracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class graphActivity extends AppCompatActivity {

    BarChart barChart; HashMap<String, Integer> eachCategoryFlagCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        barChart = findViewById(R.id.barGraph);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            eachCategoryFlagCount = (HashMap<String, Integer>)getIntent().getSerializableExtra("EachCategoryFlagCount");
        }
        else {return;}

        //x-axis
        String[] tmpCategories = getResources().getStringArray(R.array.category_array);
        ArrayList<String> categories = new ArrayList<>(Arrays.asList(tmpCategories));

        //y-axis
        /*ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0, 5));
        barEntries.add(new BarEntry(1, 8));
        barEntries.add(new BarEntry(2, 12));
        barEntries.add(new BarEntry(3, 2));
        barEntries.add(new BarEntry(4, 9));
        BarDataSet barDataSet = new BarDataSet(barEntries, "Count");*/
        //Air flag count
        ArrayList<BarEntry> barEntries1 = new ArrayList<>();
        barEntries1.add(new BarEntry(1, eachCategoryFlagCount.get(categories.get(0))));
        BarDataSet barDataSet1 = new BarDataSet(barEntries1, categories.get(0));
        barDataSet1.setColor(ContextCompat.getColor(graphActivity.this, R.color.colorAir));
        //Water flag count
        ArrayList<BarEntry> barEntries2 = new ArrayList<>();
        barEntries2.add(new BarEntry(2, eachCategoryFlagCount.get(categories.get(1))));
        BarDataSet barDataSet2 = new BarDataSet(barEntries2, categories.get(1));
        barDataSet2.setColor(ContextCompat.getColor(graphActivity.this,R.color.colorWater));
        //Noise flag count
        ArrayList<BarEntry> barEntries3 = new ArrayList<>();
        barEntries3.add(new BarEntry(3, eachCategoryFlagCount.get(categories.get(2))));
        BarDataSet barDataSet3 = new BarDataSet(barEntries3, categories.get(2));
        barDataSet3.setColor(ContextCompat.getColor(graphActivity.this,R.color.colorNoise));
        //Land flag count
        ArrayList<BarEntry> barEntries4 = new ArrayList<>();
        barEntries4.add(new BarEntry(4, eachCategoryFlagCount.get(categories.get(3))));
        BarDataSet barDataSet4 = new BarDataSet(barEntries4, categories.get(3));
        barDataSet4.setColor(ContextCompat.getColor(graphActivity.this,R.color.colorLand));
        //Others flag count
        ArrayList<BarEntry> barEntries5 = new ArrayList<>();
        barEntries5.add(new BarEntry(5, eachCategoryFlagCount.get(categories.get(4))));
        BarDataSet barDataSet5 = new BarDataSet(barEntries5, categories.get(4));
        barDataSet5.setColor(ContextCompat.getColor(graphActivity.this,R.color.colorOthers));

        //setting the data to the bar-chart
        BarData barData = new BarData(barDataSet1, barDataSet2, barDataSet3, barDataSet4, barDataSet5);
        barChart.setData(barData);
        barChart.animateY(1000);
        barChart.setFitBars(true);

        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);
        barChart.invalidate();

        //setting the x-axis labels
        /*XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(categories));
        xAxis.setCenterAxisLabels(true);*/

        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);

    }
}
