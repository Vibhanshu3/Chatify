package com.example.chatify;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.chatify.activity.MainActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private TextInputLayout settingName;
    private TextInputLayout settingStatus;
    private CircleImageView settingImage;
    private ProgressBar settingProgBar;
    ProgressDialog loadingBar;
    Bitmap thumb_bitmap = null;
    private Uri uri;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseAuth mauth;
    private String currUserID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        settingImage = findViewById(R.id.setting_Image);
        settingName = findViewById(R.id.setting_Name);
        settingStatus = findViewById(R.id.setting_Status);
       // settingProgBar = findViewById(R.id.setting_progressBar);
        loadingBar = new ProgressDialog(this);

        mtoolbar = findViewById(R.id.setting_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mauth = FirebaseAuth.getInstance();
        currUserID = mauth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        Log.d("Setting", "onCreate: " + "in settings");

        retriveUserInfo();

    }

    private void retriveUserInfo() {
        databaseReference.child("User").child(currUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("User_Name")
                                && dataSnapshot.hasChild("User_Image")) {
                            String username = dataSnapshot.child("User_Name").getValue().toString();
                            String status = dataSnapshot.child("User_Status").getValue().toString();
                            String userimage = dataSnapshot.child("User_Image").getValue().toString();

                            settingName.getEditText().setText(username);
                            settingStatus.getEditText().setText(status);
                            Picasso.get().load(userimage).placeholder(R.drawable.default_image).into(settingImage);



                        } else if (dataSnapshot.exists() && dataSnapshot.hasChild("User_Name")) {
                            String username = dataSnapshot.child("User_Name").getValue().toString();
                            String status = dataSnapshot.child("User_Status").getValue().toString();

                            settingName.getEditText().setText(username);
                            settingStatus.getEditText().setText(status);
                        } else {

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public boolean validateName() {

        String userName = settingName.getEditText().getText().toString();

        if (userName.isEmpty()) {
            settingName.setError("Field can't be empty");
            return false;
        } else {
            settingName.setError(null);
            return true;
        }
    }

    public void Continue(View view) {
        settingProgBar.setVisibility(View.VISIBLE);
        if (!validateName()) {
            settingProgBar.setVisibility(View.INVISIBLE);
            return;
        }

        String username = settingName.getEditText().getText().toString();
        String status = settingStatus.getEditText().getText().toString();

        databaseReference.child("User").child(currUserID).child("User_Status").setValue(status);
        // databaseReference.child("User Image").setValue(R.drawable.default_image);
        databaseReference.child("User").child(currUserID).child("User_Name").setValue(username)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            settingProgBar.setVisibility(View.INVISIBLE);
                            Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                            //  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();

                        } else {
                            String error = task.getException().toString();
                            Toast.makeText(SettingActivity.this, "Error : " + error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public void camera(View view) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(SettingActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(SettingActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

            } else {
                // Toast.makeText(SettingActivity.this, "You Already have a Permission", Toast.LENGTH_SHORT).show();
                bringImage();
            }
        } else {
            bringImage();
        }
    }

    private void bringImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SettingActivity.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Uploading Your Profile");
                loadingBar.setMessage("Please wait");
                loadingBar.show();

                uri = result.getUri();
                settingImage.setImageURI(uri);
                //  isChanged = true;

                uploadImageToFirebaseDatabase();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void uploadImageToFirebaseDatabase() {
        File thumbUri = new File(uri.getPath());

        try {
            thumb_bitmap = new Compressor(this)
                    .setMaxWidth(200)
                    .setMaxHeight(200)
                    .setQuality(50)
                    .compressToBitmap(thumbUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //for compressed image
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] thumbByte = baos.toByteArray();

        final UploadTask uploadTask = storageReference.child("ProfileThumbs").child(currUserID + ".jpg")
                .putBytes(thumbByte);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return storageReference.child("ProfileThumbs").child(currUserID + ".jpg").getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    databaseReference.child("User").child(currUserID).child("User_Thumb_Image").setValue(downloadUri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SettingActivity.this, "Thumb Image Uploaded to Database", Toast.LENGTH_SHORT).show();
                            } else {
                                String e = task.getException().toString();
                                Toast.makeText(SettingActivity.this, e, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(SettingActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //for original image
        final StorageReference filePath = storageReference.child("ProfileImages").child(currUserID + ".jpg");
        filePath.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {

                    String downloadUri = task.getResult().toString();
                    databaseReference.child("User").child(currUserID).child("User_Image").setValue(downloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SettingActivity.this, "Image Uploaded to Database", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
                } else {
                    Toast.makeText(SettingActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }
}