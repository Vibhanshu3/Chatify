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
}
