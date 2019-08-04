package com.example.chatify.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.AddStoryActivity;
import com.example.chatify.Data.AllUsers;
import com.example.chatify.Data.Story;
import com.example.chatify.R;
import com.example.chatify.StoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {

    private Context mContext;
    private List<Story> mStory;

    public StoryAdapter(Context mContext, List<Story> mStory) {
        this.mContext = mContext;
        this.mStory = mStory;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0){
            View view = LayoutInflater.from(mContext).inflate(R.layout.add_story_item, parent, false);
            return new StoryAdapter.ViewHolder(view);

        }else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.story_item, parent, false);
            return new StoryAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        final Story story = mStory.get(position);

        userInfo(viewHolder, story.getUserID(), position);

        if (viewHolder.getAdapterPosition() != 0) {
            seenStory(viewHolder, story.getUserID());
        }

        if (viewHolder.getAdapterPosition() == 0){
            myStory(viewHolder.addStory_text, viewHolder.storyPlus, false);
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewHolder.getAdapterPosition() == 0){
                    myStory(viewHolder.addStory_text, viewHolder.storyPhoto, true);

                } else {
                    // TODO: go to story
                    Intent intent = new Intent(mContext, StoryActivity.class);
                    intent.putExtra("userid", story.getUserID());
                    mContext.startActivity(intent);

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mStory.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView storyPhoto, storyPlus, storyPhotoSeen;
        public TextView storyUsername, addStory_text;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            storyPhoto = itemView.findViewById(R.id.story_photo);
            storyPlus = itemView.findViewById(R.id.story_plus );
            storyPhotoSeen = itemView.findViewById(R.id.story_photo_seen);
            storyUsername = itemView.findViewById(R.id.story_username);
            addStory_text = itemView.findViewById(R.id.addstory_text);

        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){
            return 0;
        }
        return 1;
    }

    private void userInfo(final ViewHolder viewHolder, String userID, final int pos){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference().child("User").child(userID);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AllUsers user = dataSnapshot.getValue(AllUsers.class);
                Picasso.get().load(user.getUser_Image()).placeholder(R.drawable.profile_image).into(viewHolder.storyPhoto);
                if(pos != 0){
                    Picasso.get().load(user.getUser_Image()).placeholder(R.drawable.profile_image).into(viewHolder.storyPhotoSeen);
                    viewHolder.storyUsername.setText(user.getUser_Name());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void myStory(final TextView textView, final ImageView imageView, final boolean click){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference().child("Story").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                long timecurrent = System.currentTimeMillis();
                for(DataSnapshot dataSnapshot1 :dataSnapshot.getChildren()){
                    Story story = dataSnapshot.getValue(Story.class);
                    if(timecurrent >story.getTimestart() && timecurrent < story.getTimeend()){
                        count++;
                    }
                }

                if(click){
                    if(count < 0) {
                        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View Story",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //got to story.
                                        Intent intent = new Intent(mContext, StoryActivity.class);
                                        intent.putExtra("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        mContext.startActivity(intent);
                                        dialog.dismiss();

                                    }
                                });

                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //got to story.
                                        Intent intent = new Intent(mContext, AddStoryActivity.class);
                                        mContext.startActivity(intent);

                                    }
                                });

                    }else {
                        Intent intent = new Intent(mContext, AddStoryActivity.class);
                        mContext.startActivity(intent);

                    }
                }else{
                    if(count > 0){
                        textView.setText("My Story");
                        imageView.setVisibility(View.VISIBLE);

                    }else{
                        textView.setText("Add Story");
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenStory(final ViewHolder viewHolder, String userID){
        DatabaseReference reference  = FirebaseDatabase.getInstance().getReference("Story")
                .child(userID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if (!snapshot.child("Views")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .exists() && System.currentTimeMillis() < snapshot.getValue(Story.class).getTimeend()){
                        i++;
                    }
                }

                if ( i > 0){
                    viewHolder.storyPhoto.setVisibility(View.VISIBLE);
                    viewHolder.storyPhotoSeen.setVisibility(View.GONE);
                } else {
                    viewHolder.storyPhoto.setVisibility(View.GONE);
                    viewHolder.storyPhotoSeen.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}


