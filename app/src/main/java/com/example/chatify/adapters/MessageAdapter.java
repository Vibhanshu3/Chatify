package com.example.chatify.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.Data.Messages;
import com.example.chatify.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    List<Messages> userMessagesList;

    DatabaseReference userReference;

    private FirebaseAuth mauth;

    String currUser;

    public MessageAdapter(List<Messages> userMessagesList){
        this.userMessagesList = userMessagesList;

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        mauth = FirebaseAuth.getInstance();

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout, parent, false);

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {

        currUser = mauth.getCurrentUser().getUid();  // message senderID

        Messages messages = userMessagesList.get(position);

        String fromUserId = messages.getFrom();
        String fromMesstype = messages.getType();

        userReference = FirebaseDatabase.getInstance().getReference().child("User").child(fromUserId);

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("User_Image")) {
                    String receiverImage = dataSnapshot.child("User_Image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.default_image).into(holder.userImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        holder.receiverText.setVisibility(View.INVISIBLE);
//        holder.userImage.setVisibility(View.INVISIBLE);
//        holder.senderText.setVisibility(View.INVISIBLE);
//        holder.messageSenderPicture.setVisibility(View.GONE);
//        holder.messageReceiverPicture.setVisibility(View.GONE);

        if (fromMesstype.equals("text")) {
            Log.d("text", "onBindViewHolder: " + "text");

            holder.receiverText.setVisibility(View.INVISIBLE);
            holder.userImage.setVisibility(View.INVISIBLE);
            holder.senderText.setVisibility(View.INVISIBLE);
            holder.messageSenderPicture.setVisibility(View.GONE);
            holder.messageReceiverPicture.setVisibility(View.GONE);

            if (fromUserId.equals(currUser)) {
                Log.d("incurr", "onBindViewHolder: " + "text");

                holder.senderText.setVisibility(View.VISIBLE);

                holder.senderText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderText.setTextColor(Color.BLACK);
                holder.senderText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());

            } else {
                Log.d("inrec", "onBindViewHolder: " + "text");

                holder.receiverText.setVisibility(View.VISIBLE);
                holder.userImage.setVisibility(View.VISIBLE);

                holder.receiverText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverText.setTextColor(Color.BLACK);
                holder.receiverText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());

            }
        } else if(fromMesstype.equals("image")) {
            holder.receiverText.setVisibility(View.INVISIBLE);
            holder.userImage.setVisibility(View.INVISIBLE);
            holder.senderText.setVisibility(View.INVISIBLE);
            holder.messageSenderPicture.setVisibility(View.INVISIBLE);
            holder.messageReceiverPicture.setVisibility(View.INVISIBLE);

            Log.d("image", "onBindViewHolder: " + "image");

            if (fromUserId.equals(currUser)) {
                Log.d("incurr", "onBindViewHolder: " + "incurr");
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);

            }else{
                Log.d("inrec", "onBindViewHolder: " + "inrec");

                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                holder.userImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.profile_image).into(holder.messageReceiverPicture);

            }
        }else {
            if (fromUserId.equals(currUser)) {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                holder.messageSenderPicture.setBackgroundResource(R.drawable.file);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);

                    }
                });

            }else{
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                holder.messageSenderPicture.setVisibility(View.VISIBLE);

                holder.messageReceiverPicture.setBackgroundResource(R.drawable.file);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);

                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {

        return userMessagesList.size();
    }

    public  class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView receiverText,senderText;
        public CircleImageView userImage;
        public ImageView messageSenderPicture, messageReceiverPicture;

        public MessageViewHolder(View view){

            super(view);

            receiverText = view.findViewById(R.id.receiver_message_text);
            senderText = view.findViewById(R.id.sender_message_text);
            userImage = view.findViewById(R.id.message_profile_image);
            messageReceiverPicture = view.findViewById(R.id.message_receiver_image);
            messageSenderPicture = view.findViewById(R.id.message_sender_image);

        }

    }
}
