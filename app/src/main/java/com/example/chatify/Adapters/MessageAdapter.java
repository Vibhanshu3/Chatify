package com.example.chatify.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {

        currUser = mauth.getCurrentUser().getUid();  // message senderID

        Messages messages = userMessagesList.get(position);

        String fromUSerId = messages.getFrom();
        String fromMesstype = messages.getType();

        userReference = FirebaseDatabase.getInstance().getReference().child("User").child(fromUSerId);

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("User_Image")){
                    String receiverImage = dataSnapshot.child("User_Image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.default_image).into(holder.userImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(fromMesstype.equals("text")){

            holder.receiverText.setVisibility(View.INVISIBLE);
            holder.userImage.setVisibility(View.INVISIBLE);
            holder.senderText.setVisibility(View.INVISIBLE);


            if(fromUSerId.equals(currUser)){

                  holder.senderText.setVisibility(View.VISIBLE);

                  holder.senderText.setBackgroundResource(R.drawable.sender_messages_layout);
                  holder.senderText.setTextColor(Color.BLACK);
                  holder.senderText.setText(messages.getMessage());
            }else{
                holder.receiverText.setVisibility(View.VISIBLE);
                holder.userImage.setVisibility(View.VISIBLE);

                holder.receiverText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverText.setTextColor(Color.BLACK);
                holder.receiverText.setText(messages.getMessage());

            }
        }
       /* if(fromUSerId.equals(currUser)){

            holder.messageText.setBackgroundResource(R.drawable.message_text_background_two);

            holder.messageText.setGravity(Gravity.RIGHT);

            holder.messageText.setTextColor(Color.BLACK);
        }else{
            holder.messageText.setBackgroundResource(R.drawable.message_text_background);

            holder.messageText.setGravity(Gravity.LEFT);

            holder.messageText.setTextColor(Color.WHITE);

        }
        holder.messageText.setText(messages.getMessage());*/
    }

    @Override
    public int getItemCount() {

        return userMessagesList.size();
    }

    public  class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView receiverText,senderText;
        public CircleImageView userImage;

        public MessageViewHolder(View view){

            super(view);

            receiverText = view.findViewById(R.id.receiver_message_text);
            senderText = view.findViewById(R.id.sender_message_text);
            userImage = view.findViewById(R.id.message_profile_image);

        }

    }
}
