package com.example.chatify.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.R;
import com.example.chatify.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupProfileAdapter extends RecyclerView.Adapter<GroupProfileAdapter.ViewHolder> {

    private Context context;
    private List<User> list;

    public GroupProfileAdapter(Context context, List<User> list) {
        this.context = context;
        this.list = list;
        Log.d("userlist", "SearchAdapter: " + list);
    }

    @NonNull
    @Override
    public GroupProfileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_display_layout, parent, false);
        GroupProfileAdapter.ViewHolder viewHolder = new GroupProfileAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = list.get(position);
        Log.d("hihihi", "onBindViewHolder: " + "hi");
        Log.d("userinadapter", "aaaa " + user);
        holder.userName.setText(user.getUser_Name());
        holder.userstatus.setText(user.getUser_Status());

        Picasso.get().load(user.getUser_Image()).placeholder(R.drawable.default_image).into(holder.userImage);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userstatus;
        CircleImageView userImage;
        public ViewHolder(View itemView){
            super(itemView);
            userName = itemView.findViewById(R.id.all_user_name);
            userstatus = itemView.findViewById(R.id.all_user_status);
            userImage = itemView.findViewById(R.id.all_user_image);
        }
    }

    public void updateList(List<User> newList){
        list = new ArrayList<>();
        list.addAll(newList);
        notifyDataSetChanged();

    }
}
