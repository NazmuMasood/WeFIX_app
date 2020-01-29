package com.example.pollutiontracker;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smarteist.autoimageslider.SliderView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.GONE;

public class popupSingleReport extends Activity {
    TextView popupSummaryTV, noImgMessageTV, noAudioMessageTV;
    FrameLayout popupImageSliderFL, popupAudioFL;
    View audioSectionDivider;
    SliderView sliderView; SliderAdapterExample adapter;
    ArrayList<String> images; ArrayList<String> imgDescriptions;
    ImageButton closePopupSingleSummaryIB;

    Report report; String reportStat;

    MediaPlayer player; ImageButton popupPlayAudioIB, popupStopAudioIB;
    ProgressBar audioLoadingPB; Timer mTimer;

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


    /*
     *
     * Dealing with audio
     * */
    private void playAudio() {
        audioLoadingPB.setVisibility(View.VISIBLE);
        audioLoadingPB.setIndeterminate(true);

        player = new MediaPlayer();
        player.setAudioAttributes( new AudioAttributes
                .Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
        try {
            player.setDataSource(report.audiosUrl);
            player.prepareAsync();

            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    player.start();
                    if (player.isPlaying()){
                        popupPlayAudioIB.setVisibility(GONE);
                        popupStopAudioIB.setVisibility(View.VISIBLE);
                        audioLoadingPB.setIndeterminate(false);
                        audioLoadingPB.setProgress(0);

                        final double duration = player.getDuration();
                        /*final int totalDivisions = player.getDuration()/1000;
                        final int amountToUpdate = 100 / totalDivisions;*/
                        mTimer = new Timer();
                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        if (player!=null && player.isPlaying()) {
                                            double currentPos = player.getCurrentPosition();
                                            double currentProgressDouble = ( currentPos/ duration) * 100;
                                            int currentProgress = (int)currentProgressDouble;
                                            audioLoadingPB.setProgress(currentProgress);
                                            //toaster.shortToast("Progress: "+currentProgress+"- "+currentProgressDouble+"- "+currentPos+"- "+duration, getApplicationContext());
                                            /*int p = audioLoadingPB.getProgress();
                                            if (p<100){
                                                audioLoadingPB.setProgress(p+amountToUpdate);
                                                toaster.shortToast("AmntToUpdt-"+amountToUpdate+", getProg"+p,
                                                        getApplicationContext());
                                            }*/
                                        }
                                    }
                                });
                            }
                        }, 0, 1000);
                    }

                }
            });
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    popupPlayAudioIB.setVisibility(View.VISIBLE);
                    popupStopAudioIB.setVisibility(GONE);
                    audioLoadingPB.setVisibility(GONE);
                    if (mTimer!=null) {
                        mTimer.cancel();
                    }
                }
            });
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    toaster.shortToast("Sorry couldn't play requested audio", popupSingleReport.this);
                    audioLoadingPB.setVisibility(GONE);
                    return false;
                }
            });


        } catch (IOException e) {
            Log.e("AUDIO_PLAY_ERROR", "prepare() failed");
            toaster.longToast("Sorry the audio file doesn't exist anymore", getApplicationContext());
        }
    }

    private void stopAudio() {
        if (player.isPlaying()&& player!=null) {
            player.release();
            player = null;
            popupPlayAudioIB.setVisibility(View.VISIBLE);
            popupStopAudioIB.setVisibility(GONE);
            audioLoadingPB.setVisibility(GONE);
            if (mTimer!=null) {
                mTimer.cancel();
            }
        }
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
        audioLoadingPB = findViewById(R.id.audioLoadingPB);
        audioLoadingPB.setVisibility(GONE);

        popupPlayAudioIB = findViewById(R.id.popupPlayAudioIB);
        popupPlayAudioIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playAudio();
            }
        });
        popupStopAudioIB = findViewById(R.id.popupStopAudioIB);
        popupStopAudioIB.setVisibility(GONE);
        popupStopAudioIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopAudio();
            }
        });

        closePopupSingleSummaryIB = findViewById(R.id.closePopupSingleSummaryIB);
    }
}
