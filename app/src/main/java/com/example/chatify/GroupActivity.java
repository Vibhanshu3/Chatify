package com.example.chatify;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

//import com.example.chatify.Adapters.MessageAdapter;
import com.example.chatify.Data.Messages;
import com.example.chatify.activity.MainActivity;
import com.example.chatify.adapters.MessageAdapter;
import com.example.chatify.model.Group;
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
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class GroupActivity extends AppCompatActivity {

    private String messageReceiverID, receivedGroupImage, messageSenderID, receivedGroupName, currUserName;
    private TextView groupName;
    private CircleImageView groupImage;
    private Toolbar chatToolbar;
    private Group group;

    private ImageButton sendMessageBtn, sendFilesBtn;
    private EditText messageInputText;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private DatabaseReference groupReference;

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
//        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //info of receiver.
        group = new Gson().fromJson(getIntent().getStringExtra("group"), Group.class);
        receivedGroupImage = group.getGroupImage();
        receivedGroupName = group.getGroupName();

        //toolbar.
        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        chatToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent groupIntent = new Intent(GroupActivity.this, GroupProfileActivity.class);
                groupIntent.putExtra("group",new Gson().toJson(group));
                startActivity(groupIntent);
            }
        });

        //?custom bar layout info.
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        groupName = findViewById(R.id.custom_profile_name);
        groupImage = findViewById(R.id.custom_profile_image);

        //chat input functions
        sendMessageBtn = findViewById(R.id.send_msg_btn);
        sendFilesBtn = findViewById(R.id.send_files_btn);
        messageInputText = findViewById(R.id.input_msg);

        groupName.setText(receivedGroupName);
      //  Picasso.get().load(receivedGroupImage).placeholder(R.drawable.default_image).into(groupImage);

        //firebase database.
        databaseReference = FirebaseDatabase.getInstance().getReference();
        groupReference = FirebaseDatabase.getInstance().getReference().child("Groups");
        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        databaseReference.child("User").child(messageSenderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currUserName = dataSnapshot.child("User_Name").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //date and time.
        Calendar calender = Calendar.getInstance();
        SimpleDateFormat currDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrDate = currDate.format(calender.getTime());

        SimpleDateFormat currTime = new SimpleDateFormat("hh:mm a");
        saveCurrTime = currTime.format(calender.getTime());
        loadingBar = new ProgressDialog(this);

        //send message button.
        sendFilesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "Ms word Files"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);
                builder.setTitle("Select the files");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            checker = "image";

                            //send to phone gallery
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 438);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        }
                        if (which == 1) {
                            checker = "pdf";

                            //send to file manager.
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/*");
                            startActivityForResult(intent.createChooser(intent, "Select PDF file"), 438);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        }

                        if (which == 2) {
                            checker = "docx";

                            //send to file manager.
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/*");
                            startActivityForResult(intent.createChooser(intent, "Select Ms word file"), 438);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        }
                    }
                });
                builder.show();
            }
        });

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

    @OnClick(R.id.send_msg_btn)
    void abc() {
        sendMessage();

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

                DatabaseReference groupReference = databaseReference.child("Groups").child(group.getGroupId())
                        .child("messages").push();
                String messagePushID = groupReference.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + checker);

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
                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", my_Url);
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", messageReceiverID);
                            messageTextBody.put("messageID", messagePushID);
                            messageTextBody.put("time", saveCurrTime);
                            messageTextBody.put("date", saveCurrDate);
                            messageTextBody.put("fromName", currUserName);
                            messageTextBody.put("messageType", "group");

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put("Groups" + "/" + group.getGroupId() + "/" + "messages" + "/" + messagePushID, messageTextBody);

                            databaseReference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(GroupActivity.this, "Image sent successfully", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();

                                    } else {
                                        Toast.makeText(GroupActivity.this, "error", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();

                                    }
                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(GroupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


            } else if (checker.equals("image")) {
                Log.d("inimage", "onActivityResult: " + "inimage");
                StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                        .child("Image Files");

                DatabaseReference groupReference = databaseReference.child("Groups").child(group.getGroupId())
                        .child("messages").push();
                String messagePushID = groupReference.getKey();

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
                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", my_Url);
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", messageReceiverID);
                            messageTextBody.put("messageID", messagePushID);
                            messageTextBody.put("time", saveCurrTime);
                            messageTextBody.put("date", saveCurrDate);
                            messageTextBody.put("fromName", currUserName);
                            messageTextBody.put("messageType", "group");

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put("Groups" + "/" + group.getGroupId() + "/" + "messages" + "/" + messagePushID, messageTextBody);

                            databaseReference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(GroupActivity.this, "Image sent successfully", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();

                                    } else {
                                        Toast.makeText(GroupActivity.this, "error", Toast.LENGTH_SHORT).show();
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

        groupReference.child(group.getGroupId()).child("messages")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages message = dataSnapshot.getValue(Messages.class);
                        Log.d("list", "onChildAdded: " + message.getMessageID() + " " + message.getName());
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
            DatabaseReference groupReference = databaseReference.child("Groups").child(group.getGroupId())
                .child("messages").push();
            String messagePushID = groupReference.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrTime);
            messageTextBody.put("date", saveCurrDate);
            messageTextBody.put("fromName", currUserName);
            messageTextBody.put("messageType", "group");

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put("Groups" + "/" + group.getGroupId() + "/" + "messages" + "/" + messagePushID, messageTextBody);

            databaseReference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(GroupActivity.this, "message sent successfully", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(GroupActivity.this, "error", Toast.LENGTH_SHORT).show();

                    }

                    messageInputText.setText(null);
                }
            });
        }

    }
}
