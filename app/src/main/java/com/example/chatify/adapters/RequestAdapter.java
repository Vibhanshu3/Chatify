package com.example.chatify.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.R;
import com.example.chatify.model.RequestModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

//FixME: fix view after request update

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private Context context;
    private List<RequestModel> list;
    private FirebaseAuth mauth;
    private String currUserID;
    private DatabaseReference chatRequestReference;

    public RequestAdapter(Context context, List<RequestModel> list) {
        this.context = context;
        this.list = list;
        Log.d("userlist", "SearchAdapter: " + list);
        mauth = FirebaseAuth.getInstance();
        currUserID = mauth.getCurrentUser().getUid();
        chatRequestReference = FirebaseDatabase.getInstance().getReference().child("Chat Request");

    }

    @NonNull
    @Override
    public RequestAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_request_layout, parent, false);
        RequestAdapter.RequestViewHolder requestViewHolder = new RequestAdapter.RequestViewHolder(view);
        return requestViewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.RequestViewHolder requestViewHolder, int i) {
        requestViewHolder.accept.setVisibility(View.VISIBLE);
        requestViewHolder.cancel.setVisibility(View.VISIBLE);

        RequestModel requestModel = list.get(i);

        if (requestModel.getReqType().equals("received")) {

            if (requestModel.getUser_Image() != null) {
                String image = requestModel.getUser_Image();
                Picasso.get().load(image).placeholder(R.drawable.default_image).into(requestViewHolder.userImage);

            }
            final String name = requestModel.getUser_Name();

            requestViewHolder.userName.setText(name);
            requestViewHolder.userStatus.setText("wants to cnnect with you");

            requestViewHolder.accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseReference contactsReference = FirebaseDatabase.getInstance().getReference().child("Contacts");
                    contactsReference.child(currUserID).child(requestModel.getUserId()).child("Contact")
                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                contactsReference.child(requestModel.getUserId()).child(currUserID)
                                        .child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            chatRequestReference.child(currUserID).child(requestModel.getUserId()).removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                chatRequestReference.child(requestModel.getUserId()).child(currUserID).removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    //Toast.makeText(getContext(), "New Contact Added", Toast.LENGTH_SHORT).show();

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
            });

            requestViewHolder.cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chatRequestReference.child(currUserID).child(requestModel.getUserId()).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        chatRequestReference.child(requestModel.getUserId()).child(currUserID).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            //  Toast.makeText(getContext(), "Contact Request Declined", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                }
            });

        } else {
            requestViewHolder.accept.setText("Cancel Sent Request");
            requestViewHolder.cancel.setVisibility(View.INVISIBLE);

            final String name = requestModel.getUser_Name();

            requestViewHolder.userName.setText(name);
            requestViewHolder.userStatus.setText("Your req has been send");

            requestViewHolder.accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chatRequestReference.child(currUserID).child(requestModel.getUserId()).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        chatRequestReference.child(requestModel.getUserId()).child(currUserID).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            //Toast.makeText(getContext(), "Contact Request Declined", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });


                }
            });
        }

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        View mview;
        TextView userName, userStatus;
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
