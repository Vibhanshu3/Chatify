package com.example.chatify.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.R;
import com.example.chatify.model.Comment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private List<Comment> comments = new ArrayList<>();

    public CommentAdapter(List<Comment> comments) {
        if (comments != null) {
            this.comments = comments;
        }
    }

    public void add(Comment comment) {
        comments.add(comment);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);

        holder.name.setText(comment.getFromName());
        holder.time.setText(String.format("%s %s", comment.getDate(), comment.getTime()));
        holder.comment.setText(comment.getMessage());

        Picasso.get().load(comment.getFromImage()).placeholder(R.drawable.profile_image).into(holder.avatar);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.comment_image)
        CircleImageView avatar;

        @BindView(R.id.comment_name)
        TextView name;
        @BindView(R.id.comment_time)
        TextView time;
        @BindView(R.id.comment_msg)
        TextView comment;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
