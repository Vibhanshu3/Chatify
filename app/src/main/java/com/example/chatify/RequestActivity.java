package com.example.chatify;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.service.autofill.Dataset;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatify.adapters.GroupProfileAdapter;
import com.example.chatify.adapters.RequestAdapter;
import com.example.chatify.fragments.RequestFragment;
import com.example.chatify.model.RequestModel;
import com.example.chatify.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestActivity extends AppCompatActivity {

    private RecyclerView myrequestrecView;

    private DatabaseReference chatRequestReference, userReference, contactsReference;
    private FirebaseAuth mAuth;
    private String currUserID;

    List<RequestModel> user;
    private RequestAdapter requestAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_request);

        myrequestrecView = findViewById(R.id.request_rec_view);
        myrequestrecView.setLayoutManager(new LinearLayoutManager(this));

        chatRequestReference = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        userReference = FirebaseDatabase.getInstance().getReference().child("User");
        contactsReference = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mAuth = FirebaseAuth.getInstance();
        currUserID = mAuth.getCurrentUser().getUid();

        user = new ArrayList<>();


        chatRequestReference.child(currUserID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("requestKey", "onChildAdded: " +dataSnapshot);
                String userKey = dataSnapshot.getKey();

                chatRequestReference.child(currUserID).child(userKey).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Log.d("requestKey", "onDataChange: " + dataSnapshot.getValue());
                        String req = dataSnapshot.getValue().toString();

                        userReference.child(userKey).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                RequestModel userclass = dataSnapshot.getValue(RequestModel.class);
                                userclass.setReqType(req);
                                userclass.setUserId(userKey);
                                user.add(userclass);
                                requestAdapter = new RequestAdapter(RequestActivity.this, user);
                                myrequestrecView.setAdapter(requestAdapter);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });




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

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions
                .Builder<User>()
                .setQuery(chatRequestReference.child(currUserID), User.class)
                .build();

        FirebaseRecyclerAdapter<User, RequestFragment.RequestViewHolder> adapter =
                new FirebaseRecyclerAdapter<User, RequestFragment.RequestViewHolder>(options) {


                    @Override
                    protected void onBindViewHolder(@NonNull final RequestFragment.RequestViewHolder requestViewHolder, int i, @NonNull User allUsers) {
                        /*requestViewHolder.accept.setVisibility(View.VISIBLE);
                        requestViewHolder.cancel.setVisibility(View.VISIBLE);

                        final String userIDs = getRef(i).getKey();

                        DatabaseReference getTypeRef = getRef(i).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    String type = dataSnapshot.getValue().toString();

                                    if(type.equals("received")){
                                        userReference.child(userIDs).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild("User_Image")){
                                                    String image = dataSnapshot.child("User_Image").getValue().toString();
                                                    Picasso.get().load(image).placeholder(R.drawable.default_image).into(requestViewHolder.userImage);

                                                }
                                                final String name = dataSnapshot.child("User_Name").getValue().toString();

                                                requestViewHolder.userName.setText(name);
                                                requestViewHolder.userStatus.setText("wants to cnnect with you");

                                                requestViewHolder.accept.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        contactsReference.child(currUserID).child(userIDs).child("Contact")
                                                                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    contactsReference.child(userIDs).child(currUserID)
                                                                            .child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){

                                                                                chatRequestReference.child(currUserID).child(userIDs).removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    chatRequestReference.child(userIDs).child(currUserID).removeValue()
                                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                    if (task.isSuccessful()) {
                                                                                                                        Toast.makeText(getContext(), "New Contact Added", Toast.LENGTH_SHORT).show();

                                                                                                                    }
                                                                                                                }
                                                                                                            });
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }}
                                                                    });

                                                                }
                                                            }
                                                        });

                                                    }
                                                });

                                                requestViewHolder.cancel.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        chatRequestReference.child(currUserID).child(userIDs).removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            chatRequestReference.child(userIDs).child(currUserID).removeValue()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                Toast.makeText(getContext(), "Contact Request Declined", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        }
                                                                                    });
                                                                        }
                                                                    }
                                                                });

                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }else{
                                        requestViewHolder.accept.setText("Cancel Sent Request");
                                        requestViewHolder.cancel.setVisibility(View.INVISIBLE);


                                        userReference.child(userIDs).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("User_Image")) {
                                                    String image = dataSnapshot.child("User_Image").getValue().toString();
                                                    Picasso.get().load(image).placeholder(R.drawable.default_image).into(requestViewHolder.userImage);

                                                }
                                                final String name = dataSnapshot.child("User_Name").getValue().toString();

                                                requestViewHolder.userName.setText(name);
                                                requestViewHolder.userStatus.setText("Your req has been send");

                                                requestViewHolder.accept.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        chatRequestReference.child(currUserID).child(userIDs).removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            chatRequestReference.child(userIDs).child(currUserID).removeValue()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                Toast.makeText(getContext(), "Contact Request Declined", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        }
                                                                                    });
                                                                        }
                                                                    }
                                                                });


                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });*/

                    }

                    @NonNull
                    @Override
                    public RequestFragment.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_request_layout, parent, false);
                        RequestFragment.RequestViewHolder requestViewHolder = new RequestFragment.RequestViewHolder(view);
                        return requestViewHolder;
                    }
                };

        myrequestrecView.setAdapter(adapter);
        adapter.startListening();

    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{

        View mview;
        TextView userName,userStatus;
        CircleImageView userImage;
        Button accept, cancel;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            mview = itemView;
            userName = itemView.findViewById(R.id.all_user_name);
            userStatus = itemView.findViewById(R.id.all_user_status);
            userImage = itemView.findViewById(R.id.all_user_image);
            accept = itemView.findViewById(R.id.request_accept_btn);
            cancel = itemView.findViewById(R.id.request_cancel_btn);
        }
    }
}
