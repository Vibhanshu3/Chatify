package com.example.chatify.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatify.Data.AllUsers;
import com.example.chatify.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private View RequestFragmentView;
    private RecyclerView myrequestrecView;

    private DatabaseReference chatRequestReference, userReference, contactsReference;
    private FirebaseAuth mAuth;
    private String currUserID;

    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestFragmentView =  inflater.inflate(R.layout.fragment_request, container, false);

        myrequestrecView = RequestFragmentView.findViewById(R.id.request_rec_view);
        myrequestrecView.setLayoutManager(new LinearLayoutManager(getContext()));

        chatRequestReference = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        userReference = FirebaseDatabase.getInstance().getReference().child("User");
        contactsReference = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mAuth = FirebaseAuth.getInstance();
        currUserID = mAuth.getCurrentUser().getUid();
        return RequestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<AllUsers> options = new FirebaseRecyclerOptions
                .Builder<AllUsers>()
                .setQuery(chatRequestReference.child(currUserID), AllUsers.class)
                .build();

        FirebaseRecyclerAdapter<AllUsers, RequestViewHolder> adapter =
                new FirebaseRecyclerAdapter<AllUsers, RequestViewHolder>(options) {


                    @Override
                    protected void onBindViewHolder(@NonNull final RequestViewHolder requestViewHolder, int i, @NonNull AllUsers allUsers) {
                        requestViewHolder.accept.setVisibility(View.VISIBLE);
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
                                                String status = dataSnapshot.child("User_Status").getValue().toString();

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


//                                                requestViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(View v) {
//
//                                                        CharSequence options [] = new CharSequence[]{
//                                                                "ACCEPT",
//                                                                "REJECT"
//                                                        };
//
//                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                                                        builder.setTitle(name + "Chat Request");
//
//                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
//                                                            @Override
//                                                            public void onClick(DialogInterface dialog, int which) {
//                                                                if(which==0){
//
////
//                                                                    contactsReference.child(currUserID).child(userIDs).child("Contact")
//                                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                        @Override
//                                                                        public void onComplete(@NonNull Task<Void> task) {
//                                                                            if (task.isSuccessful()) {
//                                                                                contactsReference.child(userIDs).child(currUserID)
//                                                                                        .child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                                    @Override
//                                                                                    public void onComplete(@NonNull Task<Void> task) {
//                                                                                        if(task.isSuccessful()){
//
//                                                                                            chatRequestReference.child(currUserID).child(userIDs).removeValue()
//                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                                                        @Override
//                                                                                                        public void onComplete(@NonNull Task<Void> task) {
//                                                                                                            if (task.isSuccessful()) {
//                                                                                                                chatRequestReference.child(userIDs).child(currUserID).removeValue()
//                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                                                                            @Override
//                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
//                                                                                                                                if (task.isSuccessful()) {
//                                                                                                                                    Toast.makeText(getContext(), "New Contact Added", Toast.LENGTH_SHORT).show();
//
//                                                                                                                                }
//                                                                                                                            }
//                                                                                                                        });
//                                                                                                            }
//                                                                                                        }
//                                                                                                    });
//                                                                                        }}
//                                                                                });
//
//                                                                            }
//                                                                        }
//                                                                    });
//                                                                }
//
//                                                                if(which==1) {
//                                                                    chatRequestReference.child(currUserID).child(userIDs).removeValue()
//                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                                @Override
//                                                                                public void onComplete(@NonNull Task<Void> task) {
//                                                                                    if (task.isSuccessful()) {
//                                                                                        chatRequestReference.child(userIDs).child(currUserID).removeValue()
//                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                                                    @Override
//                                                                                                    public void onComplete(@NonNull Task<Void> task) {
//                                                                                                        if (task.isSuccessful()) {
//                                                                                                            Toast.makeText(getContext(), "Contact Request Declined", Toast.LENGTH_SHORT).show();
//                                                                                                        }
//                                                                                                    }
//                                                                                                });
//                                                                                    }
//                                                                                }
//                                                                            });
//                                                                }
//                                                            }
//                                                        });
//                                                        builder.show();
//
//                                                    }
//                                                });
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
                        });


                    }

                    @NonNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_request_layout, parent, false);
                        RequestViewHolder requestViewHolder = new RequestViewHolder(view);
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
       // ImageView online_status;
        Button accept, cancel;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            mview = itemView;
            userName = itemView.findViewById(R.id.all_user_name);
            userStatus = itemView.findViewById(R.id.all_user_status);
            userImage = itemView.findViewById(R.id.all_user_image);
          //  online_status = itemView.findViewById(R.id.online_user);
            accept = itemView.findViewById(R.id.request_accept_btn);
            cancel = itemView.findViewById(R.id.request_cancel_btn);
        }
    }
}

