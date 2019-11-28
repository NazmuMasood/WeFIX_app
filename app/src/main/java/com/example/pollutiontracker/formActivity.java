package com.example.pollutiontracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

public class formActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        View.OnLongClickListener,
        View.OnClickListener
{
    Boolean allFieldsSatisfy = true;

    EditText locationET;
    Spinner categorySpinner, sourceSpinner, extentSpinner;
    String[] categories, sources, extents;
    Button submitButton;

    //Image related
    static final int REQUEST_IMAGE_CHOOSE = 2, REQUEST_IMAGE_CAPTURE = 1;
    Uri mImageUri;
    HashMap<String, Uri> images = new HashMap<>(); ArrayList<String> imagesUrl = new ArrayList<>();
    RelativeLayout imgRL; LinearLayout imgLL; ImageButton imgIB;
    ProgressBar mProgressBar; TextView mProgressTV;
        //explicitly camera related
    String currentPhotoPath; //Absolute path where captured images would be stored
    Uri photoURI;//save this uri in onSaveInstance state else it might become null when..
                 //..user rotates device while using camera
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
        //Image intent dialog
    ImageButton cameraIB, galleryIB; Dialog imgIntentDialog;

    //Location data which we got from pinpointLocMapsActivity
    com.example.pollutiontracker.mLatLng mLatLng; String mAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mAddress = bundle.getString("address");
            LatLng tmpLatLng = bundle.getParcelable("LatLng");
            mLatLng = new mLatLng(tmpLatLng.latitude, tmpLatLng.longitude);
            toaster.shortToast("address "+mAddress, formActivity.this);
        }
        else{
            toaster.shortToast("Sorry couldn't find location data. Please set location again..", formActivity.this);
            Intent intent = new Intent(formActivity.this, pinpointLocMapsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        locationET = findViewById(R.id.locationET);
        locationET.setText(mAddress);
        locationET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(formActivity.this, pinpointLocMapsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        categorySpinner = findViewById(R.id.categorySpinner);
        sourceSpinner = findViewById(R.id.sourceSpinner);
        extentSpinner = findViewById(R.id.extentSpinner);
        //Image upload
        imgRL = findViewById(R.id.imgRelativeLayout);
        imgLL = findViewById(R.id.imgLinearLayout);
        imgIB = findViewById(R.id.imgIB);
        mProgressBar = findViewById(R.id.mProgressBar);
        mProgressTV = findViewById(R.id.mProgressTV);
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
                if (allFieldsSatisfy) {
                    //toaster.shortToast("Your report is being uploaded", formActivity.this);
                    handleUpload();
                }
                else { toaster.shortToast("Please fill all input fields", formActivity.this); }
            }
        });
    }

    /*
    *
    * Dealing with report upload/post
    * */
    private void handleUpload() {
        //@SuppressWarnings("VisibleForTests")
        if (!activeNetwork()){
            toaster.shortToast("No internet connection. Please try again...", formActivity.this);
            return;
        }
        //if no image has been selected
        if (images.isEmpty()){
            handleReportUpload(null);
        }
        //if image(s) has been selected
        else {
            handleImgUpload();
        }
    }

    private void handleReportUpload(ArrayList<String> imagesUrl){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reportsRef = database.getReference("pollution-tracker/reports");
        final DatabaseReference geoFireRef = FirebaseDatabase.getInstance().getReference("pollution-tracker/geofire");

        String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());

        mProgressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        /*
         *Uploading the report to firebase database after all images uploaded
         */
        //Map<String, Report> reports = new HashMap<>();
        //ArrayList<Report> reports = new ArrayList<>();

        //LatLng rajshahi = new LatLng(24.367350, 88.636055);
        //Map<String, LatLng> atLoc = new HashMap<>();
        //atLoc.put("location", rajshahi);
        Report report;
        if (imagesUrl==null) {
            report = new Report(
                    mLatLng,
                    timeStamp,
                    mAddress,
                    categorySpinner.getSelectedItem().toString(),
                    sourceSpinner.getSelectedItem().toString(),
                    extentSpinner.getSelectedItem().toString()
            );
        }
        else {
            report = new Report(
                    mLatLng,
                    timeStamp,
                    mAddress,
                    categorySpinner.getSelectedItem().toString(),
                    sourceSpinner.getSelectedItem().toString(),
                    extentSpinner.getSelectedItem().toString(),
                    imagesUrl
            );
        }
        //reports.put(timeStamp, report);
        //reports.add(report);

        final String reportId = reportsRef.push().getKey();
        reportsRef.child(reportId).setValue(report).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    GeoFire geoFire = new GeoFire(geoFireRef);
                    geoFire.setLocation(reportId, new GeoLocation(mLatLng.latitude, mLatLng.longitude),
                            new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    if (error != null) {
                                        System.err.println("There was an error saving the location to GeoFire: " + error);
                                    } else {
                                        System.out.println("Location saved on server successfully!");
                                    }
                                }
                            });

                    toaster.shortToast("Report was posted successfully", formActivity.this);
                    mProgressBar.setVisibility(View.GONE);

                    Intent intent = new Intent(formActivity.this, pollutedLocsMapsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                else {toaster.longToast("Report post error. Please try again...", formActivity.this);}
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
    }

    private void handleImgUpload() {
        /*
         *Uploading the images first to firebase storage, one-by-one
         */
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("pollution-tracker");

        final ArrayList<UploadTask> uploadTasks = new ArrayList<>();
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(0);

        for (final HashMap.Entry<String, Uri> entry : images.entrySet()) {
            final Uri file = entry.getValue();
            final StorageReference imagesRef = storageRef.child("images/" + file.getLastPathSegment()+"_"
                                                                +System.currentTimeMillis());
            UploadTask uploadTask = imagesRef.putFile(file);
            uploadTasks.add(uploadTask);

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    toaster.shortToast("Upload failed: "+ file.getLastPathSegment()+" -"+exception.getMessage(), formActivity.this);

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uriTask.isComplete());
                    Uri downloadUri = uriTask.getResult();
                    imagesUrl.add(downloadUri.toString());
                    toaster.shortToast("Upload success: " + file.getLastPathSegment(), formActivity.this);
                    mProgressTV.setText("Upload success: "+ file.getLastPathSegment()+".jpg");
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    mProgressTV.setText("Uploading "+ file.getLastPathSegment()+": "+(int)progress+"%");
                }
            });
        }//for loop

        Task finalTask = Tasks.whenAll(uploadTasks);
        finalTask.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                toaster.shortToast("All images were uploaded", formActivity.this);
                mProgressBar.setVisibility(View.GONE);
                for (String s : imagesUrl){Log.d("imagesUrl", s);}
                handleReportUpload(imagesUrl);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                toaster.shortToast("Some images weren't uploaded", formActivity.this);
            }
        });

    }

    public boolean activeNetwork () {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        return isConnected;
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
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_CANCELED){
            this.getContentResolver().delete(photoURI, null, null);
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

    //setPic() to ImageView
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(formActivity.this, pollutedLocsMapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

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

    // getRealPathFromURI(Uri contentURI)
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
