package com.example.pollutiontracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class popupGraph extends Activity implements View.OnTouchListener {
    BarChart barChart; HashMap<String, Integer> eachCategoryFlagCount;
    Window window; ImageButton closePopupGraphIB;
    RelativeLayout popupSummaryRL, popupGraphRL;
    TextView popupStatsTV; String reportStat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setFinishOnTouchOutside(false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_graph);
        barChart = findViewById(R.id.popupBarGraph);
        popupStatsTV = findViewById(R.id.popupStatsTV);
        popupGraphRL = findViewById(R.id.popupGraphRL);
        popupSummaryRL = findViewById(R.id.popupSummaryRL);

        System.out.println("PopupDialogue started");
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        window = getWindow();
        //window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setGravity(Gravity.CENTER);
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        //close pop-up button
        closePopupGraphIB = findViewById(R.id.closePopupGraphIB);
        closePopupGraphIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupGraph.this.finish();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (getIntent().getStringExtra("Summary")!=null){
                reportStat = getIntent().getStringExtra("Summary");
                popupStatsTV.setText(reportStat);
                popupGraphRL.setVisibility(View.GONE);
                return;
            }
            else if (getIntent().getSerializableExtra("EachCategoryFlagCount")!=null) {
                eachCategoryFlagCount = (HashMap<String, Integer>) getIntent().getSerializableExtra("EachCategoryFlagCount");
                for (HashMap.Entry<String, Integer> entry : eachCategoryFlagCount.entrySet()) {
                    System.out.println(entry.getKey() + '[' + entry.getValue() + ']');
                }
                popupSummaryRL.setVisibility(View.GONE);
            }
        }
        else {return;}

        //Graph handling
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
        barDataSet1.setColor(ContextCompat.getColor(popupGraph.this, R.color.colorAir));
        //Water flag count
        ArrayList<BarEntry> barEntries2 = new ArrayList<>();
        barEntries2.add(new BarEntry(2, eachCategoryFlagCount.get(categories.get(1))));
        BarDataSet barDataSet2 = new BarDataSet(barEntries2, categories.get(1));
        barDataSet2.setColor(ContextCompat.getColor(popupGraph.this,R.color.colorWater));
        //Noise flag count
        ArrayList<BarEntry> barEntries3 = new ArrayList<>();
        barEntries3.add(new BarEntry(3, eachCategoryFlagCount.get(categories.get(2))));
        BarDataSet barDataSet3 = new BarDataSet(barEntries3, categories.get(2));
        barDataSet3.setColor(ContextCompat.getColor(popupGraph.this,R.color.colorNoise));
        //Land flag count
        ArrayList<BarEntry> barEntries4 = new ArrayList<>();
        barEntries4.add(new BarEntry(4, eachCategoryFlagCount.get(categories.get(3))));
        BarDataSet barDataSet4 = new BarDataSet(barEntries4, categories.get(3));
        barDataSet4.setColor(ContextCompat.getColor(popupGraph.this,R.color.colorLand));
        //Others flag count
        ArrayList<BarEntry> barEntries5 = new ArrayList<>();
        barEntries5.add(new BarEntry(5, eachCategoryFlagCount.get(categories.get(4))));
        BarDataSet barDataSet5 = new BarDataSet(barEntries5, categories.get(4));
        barDataSet5.setColor(ContextCompat.getColor(popupGraph.this,R.color.colorOthers));

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

        //removing the background grid on graph
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getXAxis().setDrawGridLines(false);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    public void onBackPressed()
    {
        popupGraph.this.finish();
        super.onBackPressed();
    }
}
