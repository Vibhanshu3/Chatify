package com.example.chatify.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.R;
import com.example.chatify.model.Comment;
import com.example.chatify.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.VISIBLE;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private ClickListener clickListener;
    private List<Post> posts = new ArrayList<>();

    public interface ClickListener {
        void like(String postKey);
        void comments(String postKey, List<Comment> comments);
    }

    public PostAdapter(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void update(List<Post> posts) {
        this.posts.clear();
        this.posts.addAll(posts);
        this.notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder view, int position) {
        Post post = posts.get(position);

        Picasso.get().load(post.getUserImage()).placeholder(R.drawable.default_image).into(view.avatar);
        view.author.setText(post.getUserName());
        view.title.setText(post.getTitle());
        view.description.setText(post.getDescription());
        view.time.setText(String.format("%s %s", post.getDate(), post.getTime()));
        view.likeIcon.setImageResource(R.drawable.ic_un_liked);

        String like = "0";
        if (post.getLike() != null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            like = String.valueOf(post.getLike().size());
            if (post.getLike().contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                view.likeIcon.setImageResource(R.drawable.ic_liked);
            }
        }

        String comment = "0";
        if (post.getComments() != null) {
            comment = String.valueOf(post.getComments().size());
        }

        view.likes.setText(like);
        view.comments.setText(comment);

        view.like.setOnClickListener(v -> clickListener.like(post.getKey()));
        view.comment.setOnClickListener(v -> clickListener.comments(post.getKey(), post.getComments()));

        if (post.getImage() != null) {
            view.image.setVisibility(VISIBLE);
            Picasso.get().load(post.getImage()).placeholder(R.drawable.fb_image).into(view.image);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.setIsRecyclable(false);

        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.post_author_avatar)
        CircleImageView avatar;

        @BindView(R.id.post_image)
        ImageView image;
        @BindView(R.id.post_like_icon)
        ImageView likeIcon;

        @BindView(R.id.post_like_click)
        LinearLayout like;
        @BindView(R.id.post_comments_click)
        LinearLayout comment;

        @BindView(R.id.post_author_name)
        TextView author;
        @BindView(R.id.post_title)
        TextView title;
        @BindView(R.id.post_description)
        TextView description;
        @BindView(R.id.post_time)
        TextView time;
        @BindView(R.id.post_likes_count)
        TextView likes;
        @BindView(R.id.post_comments_count)
        TextView comments;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
