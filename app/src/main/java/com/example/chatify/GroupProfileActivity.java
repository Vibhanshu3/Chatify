package com.example.chatify;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.adapters.GroupProfileAdapter;
import com.example.chatify.model.Group;
import com.example.chatify.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class GroupProfileActivity extends AppCompatActivity {

    private Toolbar group_desc_toolbar;
    private Group group;
    private String receivedGroupName;
    private ImageView group_desc_imageview;
    private RecyclerView memberList;
    private DatabaseReference memberReference;
    private DatabaseReference databaseReference;
    List<User> members;

    private GroupProfileAdapter groupProfileAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);

        group_desc_toolbar = findViewById(R.id.group_desc_toolbar);
        setSupportActionBar(group_desc_toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        group = new Gson().fromJson(getIntent().getStringExtra("group"), Group.class);


        group_desc_imageview = findViewById(R.id.group_desc_image);
        memberList = findViewById(R.id.member_list);
        members = new ArrayList<>();


        //firebase
        memberReference = FirebaseDatabase.getInstance().getReference().child("Groups").child(group.getGroupId()).child("members");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("User");

        group = new Gson().fromJson(getIntent().getStringExtra("group"), Group.class);
        receivedGroupName = group.getGroupName();

        group_desc_toolbar.setTitle(receivedGroupName);
        if(group.getGroupImage() != null && !group.getGroupImage().equals("")) {
            Picasso.get().load(group.getGroupImage()).placeholder(R.drawable.default_image).into(group_desc_imageview);
        }

        memberReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int i = 0; i < dataSnapshot.getChildrenCount(); i++) {
                    memberReference.child(String.valueOf(i)).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String user = dataSnapshot.child("member").getValue().toString();

                            databaseReference.child(user).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    User userclass = dataSnapshot.getValue(User.class);

                                    Log.d("value", "onDataChange: " + userclass);
                                    members.add(userclass);
                                    Log.d("value", "members: " + members);
                                   // groupProfileAdapter.updateList(members);
                                    groupProfileAdapter = new GroupProfileAdapter(GroupProfileActivity.this, members);
                                    memberList.setAdapter(groupProfileAdapter);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

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
        Log.d("thisis", "onCreate: " + members);

        memberList.setLayoutManager(new LinearLayoutManager(this));

    }
}
