package com.example.chatify;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatify.Adapters.MessageAdapter;
import com.example.chatify.Data.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

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
    private ImageButton sendMessageBtn;
    private EditText messageInputText;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;


    private List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView recyclerView;


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

        sendMessageBtn = findViewById(R.id.send_msg_btn);
        messageInputText = findViewById(R.id.input_msg);

        username.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.default_image).into(userimage);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();

            }
        });

        recyclerView = findViewById(R.id.private_message_rec_view);
        messageAdapter = new MessageAdapter(messageList);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messageAdapter);

        displayLastSeen();
    }



    private void displayLastSeen() {

        databaseReference.child("User").child(messageSenderID).child("User_State").child("State")
                .setValue("Online")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("state", "updateUserStatus: " );

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
            String messageReceicerRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = databaseReference.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceicerRef + "/" + messagePushID, messageTextBody);

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