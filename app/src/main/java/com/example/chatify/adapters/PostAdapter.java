package com.example.chatify.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.R;
import com.example.chatify.model.Post;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.VISIBLE;

public class PostAdapter extends FirebaseRecyclerAdapter<Post, PostAdapter.ViewHolder> {
    public PostAdapter(@NonNull FirebaseRecyclerOptions<Post> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder view, int i, @NonNull Post post) {
        Picasso.get().load(post.getUserImage()).placeholder(R.drawable.default_image).into(view.avatar);
        view.author.setText(post.getUserName());
        view.title.setText(post.getTitle());
        view.description.setText(post.getDescription());
        view.time.setText(String.format("%s %s", post.getDate(), post.getTime()));

        if (post.getImage() != null) {
            view.image.setVisibility(VISIBLE);
            Picasso.get().load(post.getUserImage()).placeholder(R.drawable.fb_image).into(view.image);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.post_author_avatar)
        CircleImageView avatar;

        @BindView(R.id.post_author_name)
        TextView author;

        @BindView(R.id.post_title)
        TextView title;

        @BindView(R.id.post_description)
        TextView description;

        @BindView(R.id.post_image)
        ImageView image;

        @BindView(R.id.post_time)
        TextView time;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
