package com.example.chatify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private Button sendMessageReqBtn, declineMessageReqBtn;
    private CircleImageView profileImage;
    private TextView profileStatus;
    private TextView profileName;

    private String recCurrUser;

    private DatabaseReference databaseReference, chatReqReference, contactsReference, notificationReference;
    private FirebaseAuth mauth;
    private String senderUserID, currState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        recCurrUser = getIntent().getExtras().get("visited user").toString();

        profileName = findViewById(R.id.profile_name);
        profileStatus = findViewById(R.id.profile_status);
        profileImage = findViewById(R.id.profile_Image);
        sendMessageReqBtn = findViewById(R.id.send_message_btn);
        declineMessageReqBtn = findViewById(R.id.decline_message_btn);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        chatReqReference = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        contactsReference = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationReference = FirebaseDatabase.getInstance().getReference().child("Notification");

        mauth = FirebaseAuth.getInstance();
        senderUserID = mauth.getCurrentUser().getUid();

        //unfriend
        currState = "new";

        retriveUserInfo();
    }

    private void retriveUserInfo() {
        databaseReference.child("User").child(recCurrUser)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("User_Image")) {
                            String username = dataSnapshot.child("User_Name").getValue().toString();
                            String status = dataSnapshot.child("User_Status").getValue().toString();
                            String userimage = dataSnapshot.child("User_Image").getValue().toString();

                            profileName.setText(username);
                            profileStatus.setText(status);
                        
                            Picasso.get().load(userimage).placeholder(R.drawable.profile_image).into(profileImage);

                            manageChatRequset();
                            
                        } else {
                            String username = dataSnapshot.child("User_Name").getValue().toString();
                            String status = dataSnapshot.child("User_Status").getValue().toString();

                            profileName.setText(username);
                            profileStatus.setText(status);

                            manageChatRequset();
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void manageChatRequset() {

        chatReqReference.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(recCurrUser)){
                            String request_type = dataSnapshot.child(recCurrUser).child("request_type").getValue().toString();
                            if(request_type.equals("sent")){
                                currState = "request_send";
                                sendMessageReqBtn.setText("Decline Request");

                            }else if(request_type.equals("received")){
                                currState = "request_received";
                                sendMessageReqBtn.setText("Accept Request" );

                                declineMessageReqBtn.setVisibility(View.VISIBLE);
                                declineMessageReqBtn.setEnabled(true);

                                declineMessageReqBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        cancelChatRequest();
                                    }
                                });
                            }
                        }else{
                            contactsReference.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(recCurrUser)){
                                                currState = "friends";
                                                sendMessageReqBtn.setText("Remove Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if(!senderUserID.equals(recCurrUser)){
            sendMessageReqBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageReqBtn.setEnabled(false);
                    
                    if(currState.equals("new")){
                        sendChatRequset();
                    }
                    if(currState.equals("request_send")){
                        cancelChatRequest();
                    }
                    if(currState.equals("request_received")){
                        acceptChatRequest();
                    }
                    if(currState.equals("friends")){
                        removespecificContact();
                    }

                }
            });

        }else{
            sendMessageReqBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void removespecificContact() {

        contactsReference.child(senderUserID).child(recCurrUser)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            contactsReference.child(recCurrUser).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendMessageReqBtn.setEnabled(true);
                                                currState = "new";
                                                sendMessageReqBtn.setText("Send Message");

                                                declineMessageReqBtn.setVisibility(View.INVISIBLE);
                                                declineMessageReqBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void acceptChatRequest() {

        contactsReference.child(senderUserID).child(recCurrUser)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            contactsReference.child(recCurrUser).child(senderUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                chatReqReference.child(senderUserID).child(recCurrUser)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    chatReqReference.child(recCurrUser).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        sendMessageReqBtn.setEnabled(true);
                                                                                        currState = "friends";
                                                                                        sendMessageReqBtn.setText("Remove Contact");

                                                                                        declineMessageReqBtn.setVisibility(View.INVISIBLE);
                                                                                        declineMessageReqBtn.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });


    }

    private void cancelChatRequest() {

        chatReqReference.child(senderUserID).child(recCurrUser)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                           chatReqReference.child(recCurrUser).child(senderUserID)
                                   .removeValue()
                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           if(task.isSuccessful()){
                                               sendMessageReqBtn.setEnabled(true);
                                               currState = "new";
                                               sendMessageReqBtn.setText("Send Request");

                                               declineMessageReqBtn.setVisibility(View.INVISIBLE);
                                               declineMessageReqBtn.setEnabled(false);
                                           }
                                       }
                                   });
                        }
                    }
                });

    }

    private void sendChatRequset() {
        chatReqReference.child(senderUserID).child(recCurrUser)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            chatReqReference.child(recCurrUser).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){

                                                HashMap<String, String> chatNotifiaction = new HashMap<>();
                                                chatNotifiaction.put("From", senderUserID);
                                                chatNotifiaction.put("type", "request");

                                                notificationReference.child(recCurrUser).push()
                                                        .setValue(chatNotifiaction)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    sendMessageReqBtn.setEnabled(true);
                                                                    currState = "request_send";
                                                                    sendMessageReqBtn.setText("Decline Request");
                                                                }
                                                            }
                                                        });


                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
