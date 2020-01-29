package com.example.pollutiontracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.ExifInterface;
import android.media.MediaPlayer;
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

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;
import id.zelory.compressor.Compressor;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
import static android.view.View.GONE;

public class formActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        View.OnLongClickListener,
        View.OnClickListener
{
    Boolean allFieldsSatisfy = true; LinearLayout formParentLL; String deviceModel = GetDeviceInfo.getDeviceName();

    EditText locationET;
    Spinner categorySpinner, sourceSpinner, extentSpinner; Spinner subCategorySpinner;
    String[] categories, sources, extents; String[] subCategories;
    ArrayAdapter<String> adapter2;
    Button submitButton;

    //Image related
    static final int REQUEST_IMAGE_CHOOSE = 2, REQUEST_IMAGE_CAPTURE = 1;
    Uri mImageUri;
    LinkedHashMap<String, Uri> images = new LinkedHashMap<>(); //stores selected image's uri
    LinkedHashMap<Uri, String> imagesFilePath = new LinkedHashMap<>();//stores each image's absolute path
    LinkedHashMap<Uri, Integer> imagesIntentType = new LinkedHashMap<>();//stores each image's intent type i.e. gallery or camera
    ArrayList<String> imagesUrl = new ArrayList<>();//stores uploaded image's firebase storage url
    ArrayList<Uri> imagesUri = new ArrayList<>();
    RelativeLayout imgRL; LinearLayout imgLL; ImageButton imgIB;
    LinearLayout setImgIconLL; TextView setImgIconTV;
    ProgressBar mProgressBar; TextView mProgressTV;
        //explicitly camera related
    String currentPhotoPath; //Absolute path where captured images would be stored
    Uri photoURI;//save this uri in onSaveInstance state else it might become null when..
                 //..user rotates device while using camera
    private static final int MY_CAMERA_PERMISSION_CODE = 100, READ_EXTERNAL_STORAGE_CODE = 200;
        //Image intent dialog
    ImageButton cameraIB, galleryIB; Dialog imgIntentDialog;

    //Location data which we got from pinpointLocMapsActivity
    com.example.pollutiontracker.mLatLng mLatLng; String mAddress;

    //auth
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    //audio chooser
    RelativeLayout audioChooserRL, audioPlayerRL;
    ImageButton audioChooserIB, playAudioIB, stopAudioIB, resetAudioIB, removeAudioIB;
    final static int TAG_CODE_PERMISSION_AUDIO = 111;
    final static int REQUEST_AUDIO_CAPTURE = 3;
    String audioFilePath, audioFile;
    String audiosUrl;
    MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mAddress = bundle.getString("address");
            LatLng tmpLatLng = bundle.getParcelable("LatLng");
            mLatLng = new mLatLng(tmpLatLng.latitude, tmpLatLng.longitude);
            //toaster.shortToast("address "+mAddress, formActivity.this);
        }
        else{
            toaster.shortToast("Sorry couldn't find location data. Please set location again..", formActivity.this);
            Intent intent = new Intent(formActivity.this, pinpointLocMapsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        formParentLL = findViewById(R.id.formParentLL);
        setImgIconLL = findViewById(R.id.setImgIconLL);
        setImgIconTV = findViewById(R.id.setImgIconTV);
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

        sources = getResources().getStringArray(R.array.air_array);
        adapter2 = new ArrayAdapter<>(this,
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
                //showImgIntentPopup();
                openFileChooser("camera");
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

        //audio chooser
        audioChooserRL = findViewById(R.id.audioChooserRL);
        audioPlayerRL = findViewById(R.id.audioPlayerRL);
        audioChooserIB = findViewById(R.id.audioChooserIB);
        audioChooserIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(formActivity.this, android.Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(formActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                PackageManager.PERMISSION_GRANTED&&
                        ContextCompat.checkSelfPermission(formActivity.this, Manifest.permission.WAKE_LOCK) ==
                                PackageManager.PERMISSION_GRANTED) {
                    chooseAudio();
                } else {
                    //Toast.makeText(pollutedLocsMapsActivity.this, "No audio permissions", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(formActivity.this, new String[] {
                                    Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.WAKE_LOCK},
                            TAG_CODE_PERMISSION_AUDIO);
                }
            }
        });
        playAudioIB = findViewById(R.id.playAudioIB);
        playAudioIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playAudio();
            }
        });
        stopAudioIB = findViewById(R.id.stopAudioIB);
        stopAudioIB.setVisibility(GONE);
        stopAudioIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopAudio();
            }
        });
        resetAudioIB = findViewById(R.id.resetAudioIB);
        resetAudioIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetAudio();
            }
        });
        removeAudioIB = findViewById(R.id.removeAudioIB);
        removeAudioIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAudio();
            }
        });
    }



    /*
     *
     * Dealing with audio
     * */
    private void playAudio() {
        player = new MediaPlayer();
        player.setAudioAttributes( new AudioAttributes
                .Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
        try {
            player.setDataSource(audioFilePath);
            player.prepare();

            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    player.start();
                    if (player.isPlaying()){
                        playAudioIB.setVisibility(GONE);
                        stopAudioIB.setVisibility(View.VISIBLE);
                    }
                }
            });
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    playAudioIB.setVisibility(View.VISIBLE);
                    stopAudioIB.setVisibility(GONE);
                }
            });
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    toaster.shortToast("Sorry couldn't play requested audio", formActivity.this);
                    return false;
                }
            });
        } catch (IOException e) {
            Log.e("AUDIO_PLAY_ERROR", "prepare() failed");
            toaster.shortToast("Sorry the audio file doesn't exist anymore", getApplicationContext());
        }
    }

    private void stopAudio() {
        if (player.isPlaying()&& player!=null) {
            player.release();
            player = null;
            playAudioIB.setVisibility(View.VISIBLE);
            stopAudioIB.setVisibility(GONE);
        }
    }

    private void resetAudio() {
        stopAudio();
        //audioFile = null; audioFilePath = null;
        chooseAudio();
    }

    private void removeAudio() {
        stopAudio();
        audioChooserRL.setVisibility(View.VISIBLE);
        audioPlayerRL.setVisibility(View.GONE);
        audioFile = null; audioFilePath = null;
    }

    private void chooseAudio() {
        audioFile = "AUDIO_"+ System.currentTimeMillis();
        audioFilePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC) +"/"+ audioFile +".wav";
        int color = ContextCompat.getColor(this, R.color.colorPrimary);
        AndroidAudioRecorder.with(this)
                // Required
                .setFilePath(audioFilePath)
                .setColor(color)
                .setRequestCode(REQUEST_AUDIO_CAPTURE)

                // Optional
                .setSource(AudioSource.MIC)
                .setChannel(AudioChannel.STEREO)
                .setSampleRate(AudioSampleRate.HZ_8000)
                .setAutoStart(true)
                .setKeepDisplayOn(true)

                // Start recording
                .record();
    }

    private Uri getAudioUri(){
        try {
            /*File audio = File.createTempFile(
                    audioFile,   //prefix
                    ".wav",          //suffix
                    getExternalFilesDir(Environment.DIRECTORY_MUSIC)       //directory
            );
            Uri audioUri = FileProvider.getUriForFile(this,
                    "com.example.pollutiontracker.fileprovider",
                    audio);
            this.grantUriPermission(getPackageName(), audioUri,
                    FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);*/
            Uri audioUri  = Uri.fromFile(new File(audioFilePath));
            return audioUri;
        }catch (Exception e){e.printStackTrace();}
        return null;
    }

    /*
     *
     * Dealing with issues about anonymous uploads
     * */
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // do your stuff
        } else {
            signInAnonymously();
        }
    }
    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnSuccessListener(this, new  OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // do your stuff
            }
        })
        .addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("signInAnonFail", "signInAnonymously:FAILURE", exception);
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
        //if no image and no audio has been selected
        if (images.isEmpty()&& audioFile == null){
            handleReportUpload(null, null);
        }
        //if image(s) has been selected
        else {
            handleFileUpload();
        }
    }

    private void handleReportUpload(ArrayList<String> imagesUrl, String audiosUrl){
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
        /*if (imagesUrl==null) {
            report = new Report(
                    mLatLng,
                    timeStamp,
                    mAddress,
                    categorySpinner.getSelectedItem().toString(),
                    sourceSpinner.getSelectedItem().toString(),
                    extentSpinner.getSelectedItem().toString()
            );
        }*/
        //else {
            report = new Report(
                    mLatLng,
                    timeStamp,
                    mAddress,
                    categorySpinner.getSelectedItem().toString(),
                    sourceSpinner.getSelectedItem().toString(),
                    extentSpinner.getSelectedItem().toString(),
                    imagesUrl,
                    audiosUrl
            );
        //}
        //reports.put(timeStamp, report);
        //reports.add(report);

        //Log.d("Report", report.imagesUrl.get(0));
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

    private void handleFileUpload() {
        /*
         *Uploading the images first to firebase storage, one-by-one
         */
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("pollution-tracker");

        final ArrayList<UploadTask> uploadTasks = new ArrayList<>();
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressTV.setVisibility(View.VISIBLE);

        /*for(LinkedHashMap.Entry<String, Uri> entry : images.entrySet()){
            imagesUri.add(entry.getValue());
        }

        for (Uri uri : imagesUri){
            Log.d("imagesUri", uri.toString());
        }*/

        //audio upload
        if (audioFile!=null) {
            final Uri audioUri = getAudioUri();
            if (audioUri!=null) {
                /*mProgressTV.setText(audioUri.toString());
                return;*/
                mProgressBar.setProgress(0);
                final StorageReference audiosRef = storageRef.child("audios/"
                        + audioFile + "_" + System.currentTimeMillis()
                );
                UploadTask uploadTaskAudio = audiosRef.putFile(audioUri);
                uploadTasks.add(uploadTaskAudio);
                uploadTaskAudio.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        toaster.shortToast("Upload failed: " + audioUri.getLastPathSegment() + " -" + exception.getMessage(), formActivity.this);

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isComplete()) ;
                        Uri downloadUri = uriTask.getResult();
                        audiosUrl = downloadUri.toString();
                        mProgressTV.setText("Upload success: " + audioUri.getLastPathSegment());
                        Log.d("onSuccess", "An audio just uploaded..url:" + downloadUri.toString());
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        mProgressTV.setText("Uploading audio: " + audioUri.getLastPathSegment() /*+ ": " + (int) progress + "%"*/);
                        mProgressBar.setProgress((int)progress);
                    }
                });
            }
            else {toaster.shortToast("Couldn't upload audio", formActivity.this);}
        }

        //image upload
        //for (final Uri file : imagesUri){
        for (final LinkedHashMap.Entry<String, Uri> entry : images.entrySet()) {
            mProgressBar.setProgress(0);
            try {
                final Uri file = entry.getValue();
                String imageTypePath;
                if (imagesIntentType.get(file)==1) { imageTypePath = "IMG_GALLERY";}
                else { imageTypePath = "IMG_CAMERA"; }
                final StorageReference imagesRef = storageRef.child("images/"
                    + imageTypePath
                    +"_"+ deviceModel
                    //+ file.getLastPathSegment()
                    + "_"+ System.currentTimeMillis()
                    //+"_"+ new SimpleDateFormat("ddMMyyyy_HH:mm:ss").format(new Date())
                );

                UploadTask uploadTask;

                /*if (imagesIntentType.get(file)==1) { //Compressing image if intent "Gallery"
                    //Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), file);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 8;
                    Bitmap bmp = BitmapFactory.decodeFile(imagesFilePath.get(file), options);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    //Fixing rotated images
                    int imageRotation = getImageRotation(new File(imagesFilePath.get(file)));
                    //Log.d("imgRotation", imageRotation+"");
                    Log.d("Uploading", "currPhotoPath: "+imagesFilePath.get(file)+" file-uri: "+file.toString());
                    if (imageRotation != 0) {
                        //bmp = getBitmapRotatedByDegree(bmp, imageRotation);
                        Matrix matrix = new Matrix();
                        matrix.preRotate(90);
                        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                    }

                    bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                    byte[] data = baos.toByteArray();

                    uploadTask = imagesRef.putBytes(data);
                }else {*/
                    uploadTask = imagesRef.putFile(file);
                //}
                uploadTasks.add(uploadTask);

                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        toaster.shortToast("Upload failed: " + file.getLastPathSegment() + " -" + exception.getMessage(), formActivity.this);

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isComplete());
                        Uri downloadUri = uriTask.getResult();
                        imagesUrl.add(downloadUri.toString());
                        mProgressTV.setText("Upload success: " + file.getLastPathSegment());
                        Log.d("onSuccess", "An image just uploaded..url:"+downloadUri.toString());
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        mProgressTV.setText("Uploading image: " + file.getLastPathSegment() /*+ ": " + (int) progress + "%"*/);
                        mProgressBar.setProgress((int)progress);
                    }
                });

            }//try block
            catch (Exception e){e.printStackTrace();}
        }//for loop

        //Task downTasks = Tasks.whenAll(downloadUrlTasks);
        Task allTasks = Tasks.whenAll(uploadTasks);
        allTasks.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                //toaster.shortToast("All files were uploaded", formActivity.this);
                mProgressBar.setVisibility(View.GONE);
                mProgressTV.setVisibility(View.GONE);
                //for (String s : imagesUrl){Log.d("imagesUrl", s);}
                handleReportUpload(imagesUrl, audiosUrl);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                toaster.shortToast("Some files weren't uploaded", formActivity.this);
            }
        });
    }

    //Handling the rotation of the image
    public static int getImageRotation(
            final File imageFile
    ) {

        ExifInterface exif = null;
        int exifRotation = 0;

        try {
            exif = new ExifInterface(imageFile.getPath());
            exifRotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (exif == null)
            return 0;
        else
            return exifToDegrees(exifRotation);
    }

    public static int exifToDegrees(int rotation) {
        if (rotation == ExifInterface.ORIENTATION_ROTATE_90)
            return 90;
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_180)
            return 180;
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_270)
            return 270;

        return 0;
    }

    private static Bitmap getBitmapRotatedByDegree(Bitmap bitmap, int rotationDegree) {
        Matrix matrix = new Matrix();
        matrix.preRotate(rotationDegree);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
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

        //If audio intent..
        if(requestCode == REQUEST_AUDIO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                audioChooserRL.setVisibility(View.GONE);
                audioPlayerRL.setVisibility(View.VISIBLE);
            } else if (resultCode == RESULT_CANCELED) {
                // Oops! User has canceled the recording
                audioFile = null; audioFilePath = null;
            }
        }

        //If image is captured via camera
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            if (photoURI!=null) {

                mImageUri = photoURI;

                Log.d("currPhotoPath", currentPhotoPath);
                try {
                //Compressing the image
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;
                Bitmap bmp = BitmapFactory.decodeFile(currentPhotoPath, options);
                //Fixing rotated images
                int imageRotation = getImageRotation(new File(currentPhotoPath));
                if (imageRotation != 0)
                    bmp = getBitmapRotatedByDegree(bmp, imageRotation);
                FileOutputStream fos = new FileOutputStream(currentPhotoPath);

               /* ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                fos.write(bytes.toByteArray());*/

                bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.flush();
                fos.close();
                }catch (Exception e){e.printStackTrace();}

                //Custom compressor library
                /*File compressedImageFile = new Compressor(this)
                        .setMaxWidth(1024)
                        .setMaxHeight(768)
                        .setQuality(75)
                        .setCompressFormat(Bitmap.CompressFormat.WEBP)
                        .setDestinationDirectoryPath(getExternalFilesDir(
                                Environment.DIRECTORY_PICTURES).getAbsolutePath())
                        .compressToFile();*/

                //also store the intent type (gallery/image) of each image to a map
                imagesIntentType.put(mImageUri, 0);//0 means its camera intent

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
                    this.grantUriPermission(getPackageName(), mImageUri,
                            FLAG_GRANT_READ_URI_PERMISSION
                    //                | FLAG_GRANT_WRITE_URI_PERMISSION
                    );
                    currentPhotoPath = ImageFilePathGallery.getPath(formActivity.this, mImageUri);
                    Log.d("currPhotoPath", currentPhotoPath);
                    //also string the intent type (gallery/image) of each image to a map
                    imagesIntentType.put(mImageUri, 1);//1 means its gallery intent
                    setPicToScrollview();

                    currentImgSelect++;
                }
            }// if [ data.getClipData() ] ends

            //If user selects a single image
            else if (data.getData() != null){

                mImageUri = data.getData();
                this.grantUriPermission(getPackageName(), mImageUri,
                        FLAG_GRANT_READ_URI_PERMISSION
                               // | FLAG_GRANT_WRITE_URI_PERMISSION
                );
                currentPhotoPath = ImageFilePathGallery.getPath(formActivity.this, mImageUri);
                Log.d("currPhotoPath", currentPhotoPath);
                //also string the intent type (gallery/image) of each image to a map
                imagesIntentType.put(mImageUri, 1);//1 means its gallery intent
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
        currentPhotoPath = image.getAbsolutePath();
        //currentPhotoPath = image.getName();
        //Log.d("createImageFile", "image.getAbsolutePath="+image.getAbsolutePath());
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

    private void dispatchGalleryImageIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_IMAGE_CHOOSE);
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
        if (requestCode == READ_EXTERNAL_STORAGE_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_LONG).show();
                dispatchGalleryImageIntent();
            }
            else
            {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == TAG_CODE_PERMISSION_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                chooseAudio();
            } else {
                Toast.makeText(this, "Audio permissions denied", Toast.LENGTH_LONG).show();
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
                    //toaster.shortToast("Camera intent starting", formActivity.this);
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
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_CODE);
            }//if write_external_storage permission already granted
            else {
                dispatchGalleryImageIntent();
            }
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

            //fixing rotated (gallery) images
            int imageRotation = getImageRotation(new File(currentPhotoPath));
            if (imageRotation != 0) {
                Matrix matrix = new Matrix();
                matrix.preRotate(90);
                scaled = Bitmap.createBitmap(scaled, 0, 0, scaled.getWidth(), scaled.getHeight(), matrix, true);
            }

            ImageView tempImg = new ImageView(this);
            tempImg.setImageBitmap(scaled);
            long time = new Date().getTime();

            int mWidth = imgIB.getWidth();
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    mWidth,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            lp.setMarginStart(15);
            tempImg.setLayoutParams(lp);
            tempImg.setTag("IMG_" + time);
            imgLL.addView(tempImg);

            //Displaying message on how to remove a selected image
            if (images.isEmpty()) {
                final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                        "To remove selected images, please long-press on them", Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                })
                .show();
            }

            images.put("IMG_" + time, mImageUri);
            tempImg.setOnLongClickListener(this);
            tempImg.setOnClickListener(this);

            //also storing the filepath of each image to a map
            imagesFilePath.put(mImageUri, currentPhotoPath);

            //changing the 'set image' to 'add image' and vice-versa and also snackBar display
            handleSetImgIcon();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSetImgIcon(){
        if (images.isEmpty()){
            setImgIconTV.setText("Set Image");
        }
        else {
            setImgIconTV.setText("Add Image");
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
        //toaster.longToast("You clicked image : "+imageView.getTag(), formActivity.this);

        String key = (String) imageView.getTag();
        Uri thisImageUri = images.get(key);
        //imgIV.setImageURI(thisImageUri);

        Log.d("onClick", "thisImageUri:"+thisImageUri.toString());
        Intent intent = new Intent(this, imgActivity.class);
        intent.putExtra("ImageUri", thisImageUri);
        intent.putExtra("CurrentPhotoPath", imagesFilePath.get(thisImageUri));
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View v) {
        //return false;
        ImageView imageView = imgLL.findViewWithTag(v.getTag());
        toaster.longToast("Removed image : "+imageView.getTag(), formActivity.this);

        String key = (String) imageView.getTag();
        images.remove(key);
        imgLL.removeView(v);

        handleSetImgIcon();
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
                //toaster.shortToast("You selected category "+categories[position]+" "+position, formActivity.this);
                switch (position) {
                    case 0:
                        sources = getResources().getStringArray(R.array.air_array); break;
                    case 1:
                        sources = getResources().getStringArray(R.array.water_array); break;
                    case 2:
                        sources = getResources().getStringArray(R.array.sound_array);break;
                    case 3:
                        sources = getResources().getStringArray(R.array.land_array); break;
                    case 4:
                        sources = getResources().getStringArray(R.array.other_array); break;
                }
                adapter2 = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, sources);
                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sourceSpinner.setAdapter(adapter2);
                if (position == 2){
                    audioChooserRL.setVisibility(View.VISIBLE);
                }
                else {audioChooserRL.setVisibility(View.GONE);}
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
    /*public static String getRealPathFromURI(Context context, Uri contentURI) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI, null,
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
                Toast.makeText(context, "Can't get imgPath from Uri", Toast.LENGTH_SHORT).show();

                result = "";
            }
            cursor.close();
        }
        return result;
    }*/
}
