package com.example.chatify.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.R;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private Context context;
    private List<String> list;

    public SearchAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
        Log.d("userlist", "SearchAdapter: " + list);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_display_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String name = list.get(position);

            holder.name.setText(name);
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView name;
        public ViewHolder(View itemView){
            super(itemView);
            name = itemView.findViewById(R.id.all_user_name);
        }
    }

    public void updateList(List<String> newList){
        list = new ArrayList<>();
        list.addAll(newList);
        notifyDataSetChanged();

    }
}
