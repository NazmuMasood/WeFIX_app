package com.example.pollutiontracker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class formActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        View.OnLongClickListener,
        View.OnClickListener
{

    EditText locationET;
    Spinner categorySpinner, sourceSpinner, extentSpinner;
    RelativeLayout imgRL; LinearLayout imgLL; ImageButton imgIB; ProgressBar imgProgressBar;
    Button submitButton;

    Boolean allFieldsSatisfy = false;

    String[] categories, sources, extents;
    String selectedCategory, selectedSource, selectedExtent;

    //Image upload
    static final int PICK_IMAGE_REQUEST = 1;
    Uri mImageUri;
    HashMap<String, Uri> images = new HashMap<>();

    ImageView imgIV; EditText imgFileNameET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        locationET = findViewById(R.id.locationET);
        categorySpinner = findViewById(R.id.categorySpinner);
        sourceSpinner = findViewById(R.id.sourceSpinner);
        extentSpinner = findViewById(R.id.extentSpinner);
        imgRL = findViewById(R.id.imgRelativeLayout);
        imgLL = findViewById(R.id.imgLinearLayout);
        imgIB = findViewById(R.id.imgIB);
        imgProgressBar = findViewById(R.id.imgProgressBar);
        submitButton = findViewById(R.id.submitButton);
        //Image upload
        imgIV = findViewById(R.id.imgIV);
        imgFileNameET = findViewById(R.id.imgFileNameET);

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

        imgIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
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

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null){

            //If user selects multiple images
            if(data.getClipData() != null) {
                int countClipData = data.getClipData().getItemCount();
                int currentImgSelect = 0;
                while (currentImgSelect < countClipData) {

                    mImageUri = data.getClipData().getItemAt(currentImgSelect).getUri();

                    try {
                        Bitmap bitmapImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageUri);
                        int nh = (int) (bitmapImage.getHeight() * (512.0 / bitmapImage.getWidth()));
                        Bitmap scaled = Bitmap.createScaledBitmap(bitmapImage, 512, nh, true);

                        ImageView tempImg = new ImageView(this);
                        tempImg.setImageBitmap(scaled);
                        long time = new Date().getTime();

                        int mWidth = imgIB.getWidth();
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                mWidth,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                        lp.setMarginStart(10);
                        tempImg.setLayoutParams(lp);
                        tempImg.setTag("IMG_" + time);
                        imgLL.addView(tempImg);

                        images.put("IMG_" + time, mImageUri);
                        tempImg.setOnLongClickListener(this);
                        tempImg.setOnClickListener(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    currentImgSelect++;
                }
            }// if [ data.getClipData() ] ends

            //If user selects a single image
            else if (data.getData() != null){

                mImageUri = data.getData();

                try {
                    Bitmap bitmapImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageUri);
                    int nh = (int) (bitmapImage.getHeight() * (512.0 / bitmapImage.getWidth()));
                    Bitmap scaled = Bitmap.createScaledBitmap(bitmapImage, 512, nh, true);

                    ImageView tempImg = new ImageView(this);
                    tempImg.setImageBitmap(scaled);
                    long time = new Date().getTime();

                    int mWidth = imgIB.getWidth();
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            mWidth,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    lp.setMarginStart(10);
                    tempImg.setLayoutParams(lp);
                    tempImg.setTag("IMG_" + time);
                    imgLL.addView(tempImg);

                    images.put("IMG_" + time, mImageUri);
                    tempImg.setOnLongClickListener(this);
                    tempImg.setOnClickListener(this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }// else if [ data.getData() ] ends
        }
    }//onActivityResult ends

    @Override
    public void onClick(View v) {
        ImageView imageView = imgLL.findViewWithTag(v.getTag());
        toaster.longToast("You clicked image : "+imageView.getTag(), formActivity.this);

        String key = (String) imageView.getTag();
        Uri thisImageUri = images.get(key);
        //imgIV.setImageURI(thisImageUri);

        Intent intent = new Intent(this, imgActivity.class);
        intent.putExtra("ImageUri", thisImageUri);
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View v) {
        //return false;
        ImageView imageView = imgLL.findViewWithTag(v.getTag());
        toaster.longToast("You long-pressed image : "+imageView.getTag(), formActivity.this);

        String key = (String) imageView.getTag();
        images.remove(key);
        imgLL.removeView(v);

        return true;
    }

    //Spinner item selection
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.categorySpinner:
                //toaster.shortToast("You selected category "+categories[position], formActivity.this);
                break;
            case R.id.sourceSpinner:
                //toaster.shortToast("You selected source "+sources[position], formActivity.this);
                break;
            case R.id.extentSpinner:
                //toaster.shortToast("You selected extent "+extents[position], formActivity.this);
                break;
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) { }
}
