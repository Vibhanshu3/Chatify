package com.example.chatify.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.R;
import com.example.chatify.model.Group;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.chatify.AppUtils.AppConst.DB_GROUPS_KEY;
import static com.example.chatify.AppUtils.AppConst.LOG_MAIN_ACTIVITY;

public class GroupsAdapter extends FirebaseRecyclerAdapter<String, GroupsAdapter.GroupViewHolder> {
    private ClickListener clickListener;
    private DatabaseReference reference;

    public interface ClickListener {
        void onGroupSelected(Group group);
    }

    public GroupsAdapter(@NonNull FirebaseRecyclerOptions<String> options, ClickListener clickListener) {
        super(options);
        this.clickListener = clickListener;

        reference = FirebaseDatabase.getInstance().getReference().child(DB_GROUPS_KEY);
    }

    @Override
    protected void onBindViewHolder(@NonNull GroupViewHolder viewHolder, int i, @NonNull String s) {
        reference.child(s).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Group group = dataSnapshot.getValue(Group.class);

                    if (group != null) {
                        viewHolder.groupName.setText(group.getGroupName());
                        if (!group.getGroupImage().isEmpty()) {
                            Picasso.get().load(group.getGroupImage()).placeholder(R.drawable.default_image).into(viewHolder.groupImage);
                        }
                        viewHolder.container.setOnClickListener(v -> clickListener.onGroupSelected(group));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(LOG_MAIN_ACTIVITY, databaseError.getDetails());
            }
        });
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_navigation_group, parent, false);
        return new GroupViewHolder(view);
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout container;
        private TextView groupName;
        private CircleImageView groupImage;

        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.item_group_name);
            groupImage = itemView.findViewById(R.id.item_group_image);
            container = itemView.findViewById(R.id.item_group_container);
        }
    }
}
