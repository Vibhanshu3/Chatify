package com.example.chatify.presenter;

import android.util.Log;

import com.example.chatify.R;
import com.example.chatify.model.Post;
import com.example.chatify.model.User;
import com.example.chatify.view.CommunityView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import static com.example.chatify.utils.AppConst.DB_POSTS_KEY;
import static com.example.chatify.utils.AppConst.DB_USERS_KEY;
import static com.example.chatify.utils.AppConst.DB_USERS_POSTS;
import static com.example.chatify.utils.AppConst.LOG_COMMUNITY;

public class CommunityPresenter {
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    private CommunityView view;

    public CommunityPresenter(CommunityView view) {
        this.view = view;

        init();
    }

    private void init() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void createPost(User user, String title, String description, String date, String time, String image) {
        if (title.isEmpty()) {
            view.postError(R.string.error_empty_title);
        } else if (description.isEmpty()) {
            view.postError(R.string.error_empty_description);
        } else {
            Post post = new Post(
                    title,
                    description,
                    date,
                    time,
                    image,
                    firebaseUser.getUid(),
                    user.getUser_Name(),
                    user.getUser_Thumb_Image()
            );
            
            DatabaseReference postReference = databaseReference.child(DB_POSTS_KEY).push();

            postReference
                    .setValue(post)
                    .addOnFailureListener(e -> {
                        view.postError(R.string.error_something_wrong);
                        Log.e(LOG_COMMUNITY, e.getMessage());
                    }).addOnSuccessListener(aVoid -> {
                        List<String> posts;
                        if (user.getPosts() == null) {
                            posts = new ArrayList<>();
                        } else {
                            posts = user.getPosts();
                        }

                        posts.add(postReference.getKey());
                        user.setPosts(posts);

                        databaseReference
                                .child(DB_USERS_KEY)
                                .child(firebaseUser.getUid())
                                .child(DB_USERS_POSTS)
                                .setValue(posts)
                                .addOnSuccessListener(aVoid1 -> view.postSuccess(user))
                                .addOnFailureListener(e -> {
                                    view.postError(R.string.error_something_wrong);
                                    Log.e(LOG_COMMUNITY, e.getMessage());
                                });
            });
        }
    }
}
