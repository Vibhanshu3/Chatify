package com.example.chatify;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.adapters.MessageAdapter;
import com.example.chatify.Data.Messages;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;
    private TextView username, userLastSeen;
    private CircleImageView userimage;
    private Toolbar chatToolbar;

    private ImageButton sendMessageBtn, sendFilesBtn;
    private EditText messageInputText;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;


    private List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView recyclerView;

    private String saveCurrTime, saveCurrDate;

    private String checker = "", my_Url = "";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;

    private FloatingActionButton fab1;
    private FloatingActionButton fab_gallery;
    private FloatingActionButton fab_pdf;
    private Boolean isFABOpen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //info of receiver.
        messageReceiverID = getIntent().getExtras().get("visited_User_id").toString();
        messageReceiverName = getIntent().getExtras().get("visited_User_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visited_User_image").toString();

        //toolbar.
        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        //?custom bar layout info.
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        username = findViewById(R.id.custom_profile_name);
        userimage = findViewById(R.id.custom_profile_image);
        userLastSeen = findViewById(R.id.custom_last_seen);

        //chat input functions
        sendMessageBtn = findViewById(R.id.send_msg_btn);
        messageInputText = findViewById(R.id.input_msg);

        username.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.default_image).into(userimage);

        //firebase database.
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        Calendar calender = Calendar.getInstance();

        SimpleDateFormat currDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrDate = currDate.format(calender.getTime());

        SimpleDateFormat currTime = new SimpleDateFormat("hh:mm a");
        saveCurrTime = currTime.format(calender.getTime());
        loadingBar = new ProgressDialog(this);

        //send message button.;

        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();

            }
        });

        recyclerView = findViewById(R.id.private_message_rec_view);
        messageAdapter = new MessageAdapter(messageList);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messageAdapter);

        //setting floating action btn.
        isFABOpen = false;
        FloatingActionButton fab = findViewById(R.id.fab);
        fab1 = findViewById(R.id.fab1);
        fab_gallery = findViewById(R.id.fab_gallery);
        fab_pdf = findViewById(R.id.fab_pdf);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFABOpen) {
                    showFABMenu();
                } else {
                    closeFABMenu();
                }
            }
        });

        fab_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker = "image";

                //send to phone gallery
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent.createChooser(intent, "Select Image"), 438);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            }
        });

        fab_pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker = "pdf";

                //send to file manager.
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("application/*");
                startActivityForResult(intent.createChooser(intent, "Select PDF file"), 438);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            }
        });


        //setting user toolbar seen.
        displayLastSeen();
    }

    private void showFABMenu() {
        isFABOpen = true;
        fab1.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fab_gallery.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        fab_pdf.animate().translationY(-getResources().getDimension(R.dimen.standard_155));
    }

    private void closeFABMenu() {
        isFABOpen = false;
        fab1.animate().translationY(0);
        fab_gallery.animate().translationY(0);
        fab_pdf.animate().translationY(0);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("hi", "onActivityResult: " + "hi");

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Log.d("forimage", "onActivityResult: " + "forimage");
            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please wait, for a while");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();

            if (!checker.equals("image")) {
                //document
                StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                        .child("Document Files");

                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = databaseReference.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + checker);

//                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                        if(task.isSuccessful()){
//
//                            Map messageDocBody = new HashMap();
//                            messageDocBody.put("message", task.getResult());
//                            messageDocBody.put("name", fileUri.getLastPathSegment());
//                            messageDocBody.put("type", checker);
//                            messageDocBody.put("from", messageSenderID);
//                            messageDocBody.put("to", messageReceiverID);
//                            messageDocBody.put("messageID", messagePushID);
//                            messageDocBody.put("time", saveCurrTime);
//                            messageDocBody.put("date", saveCurrDate);
//
//                            Map messageBodyDetails = new HashMap();
//                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageDocBody);
//                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageDocBody);
//
//                            databaseReference.updateChildren(messageBodyDetails);
//                            loadingBar.dismiss();
//                        }
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        loadingBar.dismiss();
//                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                        double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//                        loadingBar.setMessage((int) p + "%" + "uploaded");
//                    }
//                });

                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();

                            my_Url = downloadUri.toString();

                            //store docx into firebase Database
                            Map messageImageBody = new HashMap();
                            messageImageBody.put("message", my_Url);
                            messageImageBody.put("name", fileUri.getLastPathSegment());
                            messageImageBody.put("type", checker);
                            messageImageBody.put("from", messageSenderID);
                            messageImageBody.put("to", messageReceiverID);
                            messageImageBody.put("messageID", messagePushID);
                            messageImageBody.put("time", saveCurrTime);
                            messageImageBody.put("date", saveCurrDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageImageBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageImageBody);

                            databaseReference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ChatActivity.this, "Image sent successfully", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();

                                    } else {
                                        Toast.makeText(ChatActivity.this, "error", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();

                                    }

                                    // messageInputText.setText(null);
                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


            } else if (checker.equals("image")) {
                Log.d("inimage", "onActivityResult: " + "inimage");
                StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                        .child("Image Files");

                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = databaseReference.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");
                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();

                            my_Url = downloadUri.toString();

                            //store image into firebase Database
                            Map messageImageBody = new HashMap();
                            messageImageBody.put("message", my_Url);
                            messageImageBody.put("name", fileUri.getLastPathSegment());
                            messageImageBody.put("type", checker);
                            messageImageBody.put("from", messageSenderID);
                            messageImageBody.put("to", messageReceiverID);
                            messageImageBody.put("messageID", messagePushID);
                            messageImageBody.put("time", saveCurrTime);
                            messageImageBody.put("date", saveCurrDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageImageBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageImageBody);

                            databaseReference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ChatActivity.this, "Image sent successfully", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();

                                    } else {
                                        Toast.makeText(ChatActivity.this, "error", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();

                                    }
                                }
                            });

                        }
                    }
                });

            } else {
                Toast.makeText(this, "Nothing selected", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();

            }

        }
    }

    private void displayLastSeen() {

        //setting user online state.
        databaseReference.child("User").child(messageSenderID).child("User_State").child("State")
                .setValue("Online")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("state", "updateUserStatus: ");

                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String er = e.getMessage().toString();
                Log.d("error", "onFailure: " + er);
            }
        });

        databaseReference.child("User").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("User_State").hasChild("State")) {
                            String state = dataSnapshot.child("User_State").child("State").getValue().toString();

                            String date = dataSnapshot.child("User_State").child("Date").getValue().toString();
                            String time = dataSnapshot.child("User_State").child("Time").getValue().toString();

                            if (state.equals("Online")) {
                                userLastSeen.setText("Online");
                            } else if (state.equals("Offline")) {
                                userLastSeen.setText("Last seen: " + date + " " + time);

                            }

                        } else {
                            userLastSeen.setText("Offline");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        messageList.clear();
        messageAdapter.notifyDataSetChanged();

        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();

        databaseReference.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        Messages message = dataSnapshot.getValue(Messages.class);

                        messageList.add(message);
                        messageAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void sendMessage() {
        String messageText = messageInputText.getText().toString();
        if (TextUtils.isEmpty(messageText)) {
            //
        } else {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = databaseReference.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrTime);
            messageTextBody.put("date", saveCurrDate);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

            databaseReference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChatActivity.this, "message sent successfully", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(ChatActivity.this, "error", Toast.LENGTH_SHORT).show();

                    }

                    messageInputText.setText(null);
                }
            });


        }

    }
}