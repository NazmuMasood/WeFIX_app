package com.example.pollutiontracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
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
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
import static java.security.AccessController.getContext;

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

    //Image upload
    static final int REQUEST_IMAGE_CHOOSE = 2, REQUEST_IMAGE_CAPTURE = 1;
    Uri mImageUri;
    HashMap<String, Uri> images = new HashMap<>();

    //Image capture
    String currentPhotoPath; //Absolute path where captured images would be stored
    Uri photoURI;//save this uri in onSaveInstance state else it might become null when..
                 //..user rotates device while using camera
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    //Image intent dialog
    ImageButton cameraIB, galleryIB; Dialog imgIntentDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        locationET = findViewById(R.id.locationET);
        categorySpinner = findViewById(R.id.categorySpinner);
        sourceSpinner = findViewById(R.id.sourceSpinner);
        extentSpinner = findViewById(R.id.extentSpinner);
        //Image upload
        imgRL = findViewById(R.id.imgRelativeLayout);
        imgLL = findViewById(R.id.imgLinearLayout);
        imgIB = findViewById(R.id.imgIB);
        imgProgressBar = findViewById(R.id.imgProgressBar);
        submitButton = findViewById(R.id.submitButton);
        //Image intent dialog
        imgIntentDialog = new Dialog(this);

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
                showImgIntentPopup();
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


    /*
    *
    * Dealing with "Image" feature
    * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //If image is captured via camera
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            if (photoURI!=null) {

                mImageUri = photoURI;
                setPicToScrollview();

            }
            else {toaster.longToast("Cannot find the photo uri", formActivity.this);}
        }

        //If image is selected from gallery
        if (requestCode == REQUEST_IMAGE_CHOOSE && resultCode == RESULT_OK
                && data != null){

            //If user selects multiple images
            if(data.getClipData() != null) {
                int countClipData = data.getClipData().getItemCount();
                int currentImgSelect = 0;
                while (currentImgSelect < countClipData) {

                    mImageUri = data.getClipData().getItemAt(currentImgSelect).getUri();
                    setPicToScrollview();

                    currentImgSelect++;
                }
            }// if [ data.getClipData() ] ends

            //If user selects a single image
            else if (data.getData() != null){

                mImageUri = data.getData();
                setPicToScrollview();

            }// else if [ data.getData() ] ends

        }//If gallery
    }//onActivityResult ends

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        //currentPhotoPath = image.getAbsolutePath();
        currentPhotoPath = image.getName();
        Log.d("createImageFile", "image.getAbsolutePath="+image.getAbsolutePath());
        Log.d("createImageFile", "currentPhotoPath="+currentPhotoPath);
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.pollutiontracker.fileprovider",
                        photoFile);
                this.grantUriPermission(getPackageName(), photoURI,
                        FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                //photoURI = null; //test
                dispatchTakePictureIntent();
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openFileChooser(String intentType) {
        if (intentType.equals("camera")) {
            PackageManager pm = this.getPackageManager();
            if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }//if camera permission already granted
                else {
                    toaster.shortToast("Camera intent starting", formActivity.this);
                    //photoURI = null; //test
                    dispatchTakePictureIntent();
                }//else camera permission granted
            }//if has camera
            else {
                toaster.longToast("Sorry your device doesn't have a camera",
                        formActivity.this);
            }//else doesn't have camera
        }//if intentType == camera

        if (intentType.equals("gallery")) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, REQUEST_IMAGE_CHOOSE);
        }//if intentType == gallery
    }

    private void showImgIntentPopup(){
        imgIntentDialog.setContentView(R.layout.activity_image_intent_dialog);
        cameraIB = imgIntentDialog.findViewById(R.id.cameraIB);
        cameraIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser("camera");
                imgIntentDialog.dismiss();
            }
        });
        galleryIB = imgIntentDialog.findViewById(R.id.galleryIB);
        galleryIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser("gallery");
                imgIntentDialog.dismiss();
            }
        });

        imgIntentDialog.setCanceledOnTouchOutside(true);
        imgIntentDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        imgIntentDialog.show();
    }

    //setBitmapToScrollview()
    private void setPicToScrollview(){
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
    }

    //setPicToImageView()
    /*private void setPic() {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        //bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }*/

    @Override
    public void onClick(View v) {
        ImageView imageView = imgLL.findViewWithTag(v.getTag());
        toaster.longToast("You clicked image : "+imageView.getTag(), formActivity.this);

        String key = (String) imageView.getTag();
        Uri thisImageUri = images.get(key);
        //imgIV.setImageURI(thisImageUri);

        Log.d("onClick", "thisImageUri:"+thisImageUri.toString());
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


    /*
    *
    *Dealing with "Spinner" feature
    */
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


    /*
    *
    * Unused methods (for now)
    */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        /*if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }*/
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        /*if (photoURI != null) {
            outState.putParcelable("uri_file_path", photoURI);
            //toaster.shortToast("onSaveInstanceState photoURI="+photoURI.toString(), formActivity.this);
            Log.d("onSaveInstanceState", "photoURI="+photoURI.toString());
        }*/

        /*if (currentPhotoPath != null){
            outState.putString("uri_file_path", currentPhotoPath);
            Log.d("onSaveInstanceState", "currentPhotoPath="+currentPhotoPath);
            Log.d("onSaveInstanceState", "photoURI="+photoURI.toString());
        }*/
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //if (savedInstanceState.containsKey("uri_file_path")) {

            /*toaster.longToast("ahaa onRestoreInstanceState getParcel="+
                            savedInstanceState.getString("uri_file_path"),
                    formActivity.this);
            Log.d("onRestoreInstanceState", "saveInstanceState.getParcelable="+
                    savedInstanceState.getString("uri_file_path"));

            if (photoURI == null) {
                //File tmp = new File(savedInstanceState.getString("uri_file_path"));
                photoURI = savedInstanceState.getParcelable("uri_file_path");
                //photoURI = FileProvider.getUriForFile(this,
                // "com.example.pollutiontracker.fileprovider", tmp);
                Log.d("onRestoreInstanceState", "photoURI="+photoURI.toString());
                if (photoURI!=null){
                    //toaster.longToast("photoUri"+photoURI.toString(), formActivity.this);
                    mImageUri = photoURI;
                    setPicToScrollview();
                }
            }*/

            /*if (currentPhotoPath == null && savedInstanceState.getString("uri_file_path")!=null){
                currentPhotoPath = savedInstanceState.getString("uri_file_path");

                //File imagePath = new File(this..getFilesDir(),
                //  "Pictures");
                File newFile = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), currentPhotoPath);
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.pollutiontracker.fileprovider", newFile);

                Log.d("onRestoreInstanceState", "currentPhotoPath="+currentPhotoPath);
                Log.d("onRestoreInstanceState", "photoURI="+photoURI.toString());
                if (photoURI!=null){
                    mImageUri = photoURI;
                    setPicToScrollview();
                }
            }*/
        // }
    }

    /*public String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = this.getContentResolver().query(contentURI, null,
                null, null, null);

        if (cursor == null) { // Source is Dropbox or other similar local file
            // path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            try {
                int idx = cursor
                        .getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
            } catch (Exception e) {
                //AppLog.handleException(ImageHelper.class.getName(), e);
                Toast.makeText(this, "Can't get imgPath from Uri", Toast.LENGTH_SHORT).show();

                result = "";
            }
            cursor.close();
        }
        return result;
    }*/
}
