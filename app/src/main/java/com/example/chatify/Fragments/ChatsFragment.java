package com.example.chatify.Fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatify.ChatActivity;
import com.example.chatify.Data.AllUsers;
import com.example.chatify.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment implements SearchView.OnQueryTextListener {

    private View privateChatsView;
    private RecyclerView chatsRecView;

    private DatabaseReference chatsReference, userReference;
    private FirebaseAuth mAuth;
    private String curruserID;

    private List<AllUsers> list;

    public ChatsFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView =  inflater.inflate(R.layout.fragment_chats, container, false);

        chatsRecView = privateChatsView.findViewById(R.id.chats_rec_view);
        chatsRecView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        curruserID = mAuth.getCurrentUser().getUid();
        chatsReference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(curruserID);
        userReference = FirebaseDatabase.getInstance().getReference().child("User");

        list = new ArrayList<>();

        return privateChatsView;
    }



    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<AllUsers> options = new FirebaseRecyclerOptions
                .Builder<AllUsers>()
                .setQuery(chatsReference, AllUsers.class)
                .build();

        FirebaseRecyclerAdapter<AllUsers, ChatsFragment.AllUserViewHolder> adapter
                = new FirebaseRecyclerAdapter<AllUsers, AllUserViewHolder>(options) {
            @NonNull
            @Override
            public AllUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
                ChatsFragment.AllUserViewHolder allUserViewHolder = new AllUserViewHolder(view);
                return allUserViewHolder;
            }

            @Override
            protected void onBindViewHolder(@NonNull final AllUserViewHolder allUserViewHolder, int i, @NonNull AllUsers allUsers) {
                final String userIDs = getRef(i).getKey();
                final String[] image = {"defafult_image"};

                userReference.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

//                      Toast.makeText(getContext(), "hi", Toast.LENGTH_SHORT).show();

                        AllUsers user = new AllUsers();

                        if(dataSnapshot.exists()){
                            if(dataSnapshot.hasChild("User_Image")){
                                image[0] = dataSnapshot.child("User_Image").getValue().toString();
                                Picasso.get().load(image[0]).placeholder(R.drawable.default_image).into(allUserViewHolder.userImage);

                                user.setUser_Image(image[0]);


                            }

                            final String name = dataSnapshot.child("User_Name").getValue().toString();
                            final String status = dataSnapshot.child("User_Status").getValue().toString();

                            allUserViewHolder.userName.setText(name);
                            allUserViewHolder.userStatus.setText("Last Seen" + "\n" + "Date" + "Time");

                            user.setUser_Name(name);

                            list.add(user);


                            if(dataSnapshot.child("User_State").hasChild("State"))
                            {
                                String state = dataSnapshot.child("User_State").child("State").getValue().toString();

                                String date = dataSnapshot.child("User_State").child("Date").getValue().toString();
                                String time = dataSnapshot.child("User_State").child("Time").getValue().toString();

                                if(state.equals("Online")){
                                    allUserViewHolder.userStatus.setText("Online");
                                }else if(state.equals("Offline")){
                                    allUserViewHolder.userStatus.setText("Last seen: " + date + " " + time);

                                }

                            }else{
                                allUserViewHolder.userStatus.setText("Offline");
                            }


                            allUserViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("visited_User_id", userIDs);
                                    chatIntent.putExtra("visited_User_name", name);
                                    chatIntent.putExtra("visited_User_image", image[0]);


                                    startActivity(chatIntent);
                                }
                            });
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        };

        chatsRecView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
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


}
