package com.example.chatify.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatify.adapters.StoryAdapter;
import com.example.chatify.model.User;
import com.example.chatify.Data.Story;
import com.example.chatify.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    private View ContactsView;
    private RecyclerView contactsRecView;

    private RecyclerView contactRecViewStory;
    private StoryAdapter storyAdapter;
    private List<Story> storyList;
    private List<String> contactList;


    private DatabaseReference contactsReference, userReference, databaseReference;

    private FirebaseAuth mAuth;
    private String currUserID;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        contactsRecView = ContactsView.findViewById(R.id.contacts_rec_view);
        contactsRecView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currUserID = mAuth.getCurrentUser().getUid();

        //setting stories.
        storyList = new ArrayList<>();
        contactList = new ArrayList<>();
        contactRecViewStory = ContactsView.findViewById(R.id.contacts_rec_view_story);
        contactRecViewStory.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false);
        contactRecViewStory.setLayoutManager(linearLayoutManager);
        storyAdapter = new StoryAdapter(getContext(), storyList);
        contactRecViewStory.setAdapter(storyAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Contacts").child(currUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        contactList.clear();
                        contactList.add(currUserID);
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            contactList.add(snapshot.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        readStory();

        contactsReference = FirebaseDatabase.getInstance().getReference().child("Contacts");
        userReference = FirebaseDatabase.getInstance().getReference().child("User");



        return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions
                .Builder<User>()
                .setQuery(contactsReference.child(currUserID), User.class)
                .build();


        FirebaseRecyclerAdapter<User, AllUserViewHolder> adapter
                = new FirebaseRecyclerAdapter<User, AllUserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final AllUserViewHolder allUserViewHolder, int i, @NonNull User allUsers) {
                String userIDs = getRef(i).getKey();

                    userReference.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            if(dataSnapshot.child("User_State").hasChild("State"))
                            {
                                String state = dataSnapshot.child("User_State").child("State").getValue().toString();

                                String date = dataSnapshot.child("User_State").child("Date").getValue().toString();
                                String time = dataSnapshot.child("User_State").child("Time").getValue().toString();

                                if(state.equals("Online")){
                                    allUserViewHolder.online_status.setVisibility(View.VISIBLE);
                                }else if(state.equals("Offline")){
                                    allUserViewHolder.online_status.setVisibility(View.INVISIBLE);

                                }

                            }else{
                                allUserViewHolder.online_status.setVisibility(View.INVISIBLE);
                            }


                            if(dataSnapshot.hasChild("User_Image")){
                                String image = dataSnapshot.child("User_Image").getValue().toString();
                                String name = dataSnapshot.child("User_Name").getValue().toString();
                                String status = dataSnapshot.child("User_Status").getValue().toString();

                                allUserViewHolder.userName.setText(name);
                                allUserViewHolder.userStatus.setText(status);
                                Picasso.get().load(image).placeholder(R.drawable.default_image).into(allUserViewHolder.userImage);

                            }else{
                                String name = dataSnapshot.child("User_Name").getValue().toString();
                                String status = dataSnapshot.child("User_Status").getValue().toString();

                                allUserViewHolder.userName.setText(name);
                                allUserViewHolder.userStatus.setText(status);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public AllUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
                ContactsFragment.AllUserViewHolder allUserViewHolder = new AllUserViewHolder(view);
                return allUserViewHolder;
            }
        };

        contactsRecView.setAdapter(adapter);
        adapter.startListening();


    }

    public static class AllUserViewHolder extends RecyclerView.ViewHolder{

        View mview;
        TextView userName,userStatus;
        CircleImageView userImage;
        ImageView online_status;

        public AllUserViewHolder(@NonNull View itemView) {
            super(itemView);

            mview = itemView;
            userName = itemView.findViewById(R.id.all_user_name);
            userStatus = itemView.findViewById(R.id.all_user_status);
            userImage = itemView.findViewById(R.id.all_user_image);
            online_status = itemView.findViewById(R.id.online_user);
        }
    }

    private void readStory(){


        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference().child("Story");
        databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        long timecurrent = System.currentTimeMillis();
                        storyList.clear();
                        storyList.add(new Story("", 0, 0, "",
                                FirebaseAuth.getInstance().getCurrentUser().getUid()));
                        for (String id : contactList) {
                            int countStory = 0;
                            Story story = null;
                            for (DataSnapshot snapshot : dataSnapshot.child(id).getChildren()) {
                                story = snapshot.getValue(Story.class);
                                if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()) {
                                    countStory++;
                                }
                            }
                            if (countStory > 0){
                                storyList.add(story);
                            }
                        }

                        storyAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
