package com.error404.free_pirate.ghost;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.icu.text.Normalizer;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.FirebaseApp;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    public Button btnChoose, btnUpload, btnLogOut;
    public ImageView imageView;
    private Button b;
    private TextView t;
    public EditText landmark;
    public double lon,lat;
    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
    public int flag=0;
    public String formattedDate,UserID;
    private ProgressBar spinner;
    public Date c,newC;


    //public Uri filePath;
    public Bitmap bitmap;
    private LocationManager locationManager;
    private LocationListener listener;
    public GeoPoint locat1;
    public StorageReference reference;
    public StorageReference ref;
   // Firebase userRef = rootRef.child("users/" + rootRef.getAuth().getUid());


    public static final int REQUEST_IMAGE_CAPTURE = 1;
    // public static final String DEBUG_TAG = "PBL";

    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseFirestore db;

    private Date getDateWithOutTime(Date targetDate) {
        Calendar newDate = Calendar.getInstance();
        newDate.setLenient(false);
        newDate.setTime(targetDate);
        newDate.set(Calendar.HOUR_OF_DAY, 0);
        newDate.set(Calendar.MINUTE,0);
        newDate.set(Calendar.SECOND,0);
        newDate.set(Calendar.MILLISECOND,0);

        return newDate.getTime();

    }
    //initialize views
    public void uploadImage() {
        try {

            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();
            String landmark_result = landmark.getText().toString();

            if (bitmap != null) {
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Uploading...");
                progressDialog.show();
                imageView.setDrawingCacheEnabled(true);
                imageView.buildDrawingCache();
                Bitmap bitmap1 = Bitmap.createBitmap(imageView.getDrawingCache());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                ref = storageReference.child("images/" + UUID.randomUUID().toString());
                UploadTask uploadTask = ref.putBytes(data);
                uploadTask
                        //ref.putFile(filePath)
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                        .getTotalByteCount());
                                progressDialog.setMessage("Uploaded " + (int) progress + "%");
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                                Log.d("db",ref.toString());
                                reference=ref;
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            } else {
                throw new Exception("Null bitmap");
            }
            try{
                db = FirebaseFirestore.getInstance();
                Map<String, Object> user = new HashMap<>();
                user.put("Date", newC);
                user.put("ImageRef",ref.toString());
                user.put("Location",locat1);
                user.put("UserID",UserID);
                user.put("landmark",landmark_result);
                db.collection("Home").document()
                        .set(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void avoid) {
                                Log.d("db", "DocumentSnapshot added with reference: "+ref.toString());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("db", "Error adding document", e);
                            }
                        });
            }
            catch(Exception  e)
            {
                Log.e("db",e.toString());
            }

         /*   try {
                db = FirebaseFirestore.getInstance();
                Map<String, Object> user = new HashMap<>();
                user.put("date2", newC);
                if(locat1==null)
                {
                    throw new Exception("Null location");
                }
                if(lat==0 || lon ==0)
                {
                    throw new Exception("Null lat and log");
                }*/
               /*final Task<QuerySnapshot> queryTask=db.collection("Date").whereEqualTo("date2",newC).get();
               queryTask.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            final List<DocumentSnapshot> documentSnapshotList = queryTask.getResult().getDocuments();
                            if (!documentSnapshotList.isEmpty()) {
                                for (DocumentSnapshot document : documentSnapshotList) {
                                    Log.d("db", document.getId() + "=>" + document.getData());

                                }
                            } else {
                                Log.d("db", "Error getting Documents", queryTask.getException());
                                //Create_Date();
                            }
                        }
                   }
                });
                */
               /*
                db.collection("Date").document("date1")
                        .set(user,SetOptions.merge())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void avoid) {
                                Log.d("db", "DocumentSnapshot added with ID: ");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("db", "Error adding document", e);
                            }
                        });*/
                //DocumentReference docRef = db.collection("Date").document("date1").collection("locations").document("location1").collection("Users").document("user2");
                /*DocumentReference docRef = db.collection("Date").document("date1");
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                Log.d("db", "DocumentSnapshot data: " + document.getData());
                            } else {
                                Log.d("db", "No such document");
                            }
                        } else {
                            Log.d("db", "get failed with ", task.getException());
                        }
                    }
                });*/
            /*
            }
            catch (Exception e)
            {
                Log.e("db", e.toString());
                flag=1;
            }*/
        } catch (Exception e) {
            Log.e("UP", e.toString());
            flag=1;
        }
        if(flag==0)
        {
            Clear();
        }
    }

    public void chooseImage() {
        //onSaveInstanceState(cap);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            intent.setType("image/*");
        }
        /*Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);*/
    }


    public void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                        startActivity(new Intent(MainActivity.this, Main2Activity.class));
                        finish();
                    }
                });
    }

    public void check()
    {
        if(UserID==null||imageView.getDrawable()==null||locat1==null||landmark==null)
        {
            btnUpload.setEnabled(false);
            check();
        }
        else
            btnUpload.setEnabled(true);
    }

    public  void Clear()
    {
        landmark.setText(null);
        t.setText(formattedDate);
        imageView.setImageDrawable(null);
        imageView.setImageResource(0);
        btnUpload.setEnabled(false);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //onRestoreInstanceState(cap);
        super.onActivityResult(requestCode, resultCode, data);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(bitmap);
        }
       check();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        btnChoose = (Button) findViewById(R.id.btnChoose);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        btnLogOut = (Button) findViewById(R.id.btnLogOut);
        imageView = (ImageView) findViewById(R.id.imgView);
        landmark=(EditText) findViewById(R.id.landmark);
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);
        t = (TextView) findViewById(R.id.textView);
        c = Calendar.getInstance().getTime();
        newC=getDateWithOutTime(c);
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        formattedDate = df.format(c);
        t.setText(formattedDate);
        UserID=currentFirebaseUser.getUid().toString();
        locat1=null;
        btnUpload.setEnabled(false);
        //b = (Button) findViewById(R.id.button);

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, listener);
            }


        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                spinner.setVisibility(View.VISIBLE);
                lon=location.getLongitude();
                lat=location.getLatitude();
                t.append("\n" + ((int) lat) + "," + ((int)lon));
                locat1 = new GeoPoint(lat, lon);
                locationManager.removeUpdates(this);
                btnChoose.setEnabled(true);
                btnLogOut.setEnabled(false);
                btnUpload.setEnabled(false);
                spinner.setVisibility(View.GONE);
                btnLogOut.setEnabled(true);
                //mLocManager.removeUpdates(listener);
               // mLocManager = null;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };
        configure_button();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                configure_button();
                break;
            default:
                break;
        }
    }

    void configure_button() {
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED    &&  ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA,android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }
            return;
        }
        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
        /*btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //noinspection MissingPermission
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, listener);
            }
        });*/
    }
        //lon=location.getLongitude();
      //  lat=location.getLatitude();
      //  t.append("\n " +lon + " " + lat);
}