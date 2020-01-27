package com.example.pollutiontracker;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static android.view.View.GONE;

public class popupSingleReport extends Activity {
    TextView popupSummaryTV, noImgMessageTV, noAudioMessageTV;
    FrameLayout popupImageSliderFL, popupAudioFL;
    View audioSectionDivider;
    SliderView sliderView; SliderAdapterExample adapter;
    ArrayList<String> images; ArrayList<String> imgDescriptions;
    ImageButton closePopupSingleSummaryIB;

    Report report; String reportStat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_single_report);
        this.setFinishOnTouchOutside(false);

        initViews();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (getIntent().getSerializableExtra("Report")!=null) {
                report = (Report) getIntent().getSerializableExtra("Report");
            }
        }
        else {return;}

        //Analyzing the received report and making summary
        String latitude = String.format(Locale.getDefault(), "%.3f", report.location.latitude);
        String longitude = String.format(Locale.getDefault(), "%.3f", report.location.longitude);
        /*reportStat = "Address: "+report.address+"\n"
                +"Location: "+report.location.latitude+", "+report.location.longitude+"\n"
                +"Pollution type: "+report.category+"\n"
                +"Source: "+report.source+"\n"
                +"Extent: "+report.extent;
        popupSummaryTV.setText(reportStat);*/
        reportStat = "Address: "+report.address+"<br>"
                +"Location: "+latitude+", "+longitude+"<br>"
                +"Pollution type: "+ "<i>" +report.category+ "</i>" +"<br>"
                +"Source: "+ "<i>" +report.source+ "</i>" + "<br>"
                +"Extent: "+ "<i>" +report.extent+ "</i>" ;
        popupSummaryTV.setText(Html.fromHtml(reportStat));

        //Populating the image slider
        if (report.imagesUrl!=null) {
            images = new ArrayList<>();
            imgDescriptions = new ArrayList<>();
            adapter = new SliderAdapterExample(this, images, imgDescriptions);
            sliderView.setSliderAdapter(adapter);

            images.addAll(report.imagesUrl);
            for (int i=0; i<report.imagesUrl.size(); i++) {
                imgDescriptions.add(report.address);
            }
            adapter.setItems(images, imgDescriptions);
            adapter.notifyDataSetChanged();
            noImgMessageTV.setVisibility(GONE);
        }
        else {popupImageSliderFL.setVisibility(GONE);}

        //Audio option view (only for sound pollution)
        if (report.category.equals(getResources().getStringArray(R.array.category_array)[2])){
            audioSectionDivider.setVisibility(View.VISIBLE);
            //sound pollution report has no audio
            if (report.audiosUrl!=null){
                popupAudioFL.setVisibility(View.VISIBLE);
            }
            else { noAudioMessageTV.setVisibility(View.VISIBLE); }
        }

        //Close popup button
        closePopupSingleSummaryIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupSingleReport.this.finish();
            }
        });
    }

    private void initViews(){
        popupSummaryTV = findViewById(R.id.popupSummaryTV);

        popupImageSliderFL = findViewById(R.id.popupImageSliderFL);
        sliderView = findViewById(R.id.popupImageSlider);
        noImgMessageTV = findViewById(R.id.noImgMessageTV);

        audioSectionDivider = findViewById(R.id.audioSectionDivider);
        audioSectionDivider.setVisibility(GONE);
        popupAudioFL = findViewById(R.id.popupAudioFL);
        popupAudioFL.setVisibility(GONE);
        noAudioMessageTV = findViewById(R.id.noAudioMessageTV);
        noAudioMessageTV.setVisibility(GONE);

        closePopupSingleSummaryIB = findViewById(R.id.closePopupSingleSummaryIB);
    }
}
