package com.example.chatify;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingActivity2 extends AppCompatActivity {

    private Toolbar mtoolbar;
    private TextView settingName;
    private TextView settingStatus;
    private CircleImageView settingImage;
    private ProgressBar settingProgBar;
    ProgressDialog loadingBar;
    Bitmap thumb_bitmap = null;
    private Uri uri;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseAuth mauth;
    private String currUserID;


    private TextView changeName;
    private TextView changeStatus;
    private Button changeEmail;
    private Button changePassword;

    //bottomSheet views
    private EditText bottomSheetTextInput;
    private TextView bottomSheetHeading;
    private Button bottomSheetSave;
    private Button bottomSheetCancel;


    private BottomSheetDialog bottomSheetDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting2);

        settingImage = findViewById(R.id.setting_image);
        settingName = findViewById(R.id.setting_username);
        settingStatus = findViewById(R.id.setting_userstatus);
        // settingProgBar = findViewById(R.id.setting_progressBar);
        loadingBar = new ProgressDialog(this);

        changeName = findViewById(R.id.change_name);
        changeStatus = findViewById(R.id.change_status);


        //setting toolbar
        mtoolbar = findViewById(R.id.setting_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //firebase
        mauth = FirebaseAuth.getInstance();
        currUserID = mauth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference.keepSynced(true); //adding offline capabilities.

        Log.d("Setting", "onCreate: " + "in settings");

        //bottomSheet
        bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetDialogView = getLayoutInflater()
                .inflate(R.layout.bottom_sheet_layout, null);
        bottomSheetDialog.setContentView(bottomSheetDialogView);

        bottomSheetTextInput = bottomSheetDialogView.findViewById(R.id.bottom_sheet_text);
        bottomSheetHeading = bottomSheetDialogView.findViewById(R.id.bottom_sheet_heading);
        bottomSheetSave = bottomSheetDialogView.findViewById(R.id.bottom_sheet_save);
        bottomSheetCancel = bottomSheetDialogView.findViewById(R.id.bottom_sheet_cancel);


        changeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetHeading.setText("Enter your name");
                bottomSheetDialog.show();

                bottomSheetSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!TextUtils.isEmpty(bottomSheetTextInput.getText())){
                            updateUserName(bottomSheetTextInput.getText().toString());
                            bottomSheetTextInput.setText(null);
                        }
                    }
                });


            }
        });

        changeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetHeading.setText("Enter your status");
                bottomSheetDialog.show();

                bottomSheetSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!TextUtils.isEmpty(bottomSheetTextInput.getText())){
                            updateUserStatus(bottomSheetTextInput.getText().toString());
                            bottomSheetTextInput.setText(null);
                        }
                    }
                });
            }
        });

        retriveUserInfo();


    }

    private void updateUserStatus(String status) {
        databaseReference.child("User").child(currUserID).child("User_Status").setValue(status);

    }

    private void updateUserName(String name) {
        databaseReference.child("User").child(currUserID).child("User_Name").setValue(name);

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
                            final String userimage = dataSnapshot.child("User_Image").getValue().toString();

                            settingName.setText(username);
                            settingStatus.setText(status);

                           // Picasso.get().load(userimage).placeholder(R.drawable.default_image).into(settingImage);

                            Picasso.get().load(userimage).networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.default_image).into(settingImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                    Log.d("yes", "onSuccess:  " +"OFFLINE");
                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(userimage).placeholder(R.drawable.default_image).into(settingImage);
                                    Log.d("error", "onError: " + e.toString());

                                }
                            });

                        } else if (dataSnapshot.exists() && dataSnapshot.hasChild("User_Name") && dataSnapshot.hasChild("User_Status")) {
                            String username = dataSnapshot.child("User_Name").getValue().toString();
                            String status = dataSnapshot.child("User_Status").getValue().toString();

                            settingName.setText(username);
                            settingStatus.setText(status);
                        } else if(dataSnapshot.exists() && dataSnapshot.hasChild("User_Name")){
                            String username = dataSnapshot.child("User_Name").getValue().toString();

                            settingName.setText(username);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    public void camera(View view) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(com.example.chatify.SettingActivity2.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(com.example.chatify.SettingActivity2.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

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
                .start(com.example.chatify.SettingActivity2.this);
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
                                Toast.makeText(com.example.chatify.SettingActivity2.this, "Thumb Image Uploaded to Database", Toast.LENGTH_SHORT).show();
                            } else {
                                String e = task.getException().toString();
                                Toast.makeText(com.example.chatify.SettingActivity2.this, e, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(com.example.chatify.SettingActivity2.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(com.example.chatify.SettingActivity2.this, "Image Uploaded to Database", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
                } else {
                    Toast.makeText(com.example.chatify.SettingActivity2.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }
}
