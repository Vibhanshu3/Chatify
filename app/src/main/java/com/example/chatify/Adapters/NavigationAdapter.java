package com.example.chatify.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.Data.Group;
import com.example.chatify.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.AccessControlContext;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NavigationAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<Group> groupList;


    private FirebaseUser maAuth = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference groupReference = FirebaseDatabase.getInstance().getReference().child("Groups").child(maAuth.getUid());

//    FirebaseRecyclerOptions<Group> options = new FirebaseRecyclerOptions.Builder<Group>()
//            .setQuery(groupReference, Group.class)
//            .build();
//
//    FirebaseRecyclerAdapter<Group, NavigationAdapter.GroupViewHolder> firebaseRecyclerAdapter =
//            new FirebaseRecyclerAdapter<Group, GroupViewHolder>(options) {
//                @Override
//                protected void onBindViewHolder(@NonNull GroupViewHolder groupViewHolder, int i, @NonNull Group group) {
//
//                }
//
//                @NonNull
//                @Override
//                public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_navigation_layout,parent,false);
//                    NavigationAdapter.GroupViewHolder groupViewHolder = new NavigationAdapter.GroupViewHolder(view);
//                    return groupViewHolder;
//                }
//
//                @Override
//                public int getItemCount() {
//                    return 0;
//                }
//            };

    public NavigationAdapter(List<Group> groupList) {
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_navigation_layout,parent,false);
        NavigationAdapter.GroupViewHolder groupViewHolder = new NavigationAdapter.GroupViewHolder(view);
        return groupViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }


    public static class GroupViewHolder extends RecyclerView.ViewHolder{

        View mview;
        TextView groupName;
        CircleImageView groupImage;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            mview = itemView;
            groupName = itemView.findViewById(R.id.group_name);
            groupImage = itemView.findViewById(R.id.group_image);

        }
    }
}
