package com.example.chatify.presenter;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.chatify.R;
import com.example.chatify.model.Post;
import com.example.chatify.model.User;
import com.example.chatify.view.CommunityView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import static com.example.chatify.utils.AppConst.DB_POSTS_KEY;
import static com.example.chatify.utils.AppConst.DB_POSTS_LIKE;
import static com.example.chatify.utils.AppConst.DB_POSTS_LIKE_COUNT;
import static com.example.chatify.utils.AppConst.DB_USERS_KEY;
import static com.example.chatify.utils.AppConst.DB_USERS_POSTS;
import static com.example.chatify.utils.AppConst.LOG_COMMUNITY;
import static com.example.chatify.utils.AppConst.STORAGE_COMMUNITY_IMAGE;
import static com.example.chatify.utils.AppConst.STORAGE_IMAGE_TYPE;

public class CommunityPresenter {
    private FirebaseUser firebaseUser;

    private DatabaseReference databaseReference;
    private DatabaseReference postReference;

    private StorageReference storageReference;

    private CommunityView view;

    public CommunityPresenter(CommunityView view) {
        this.view = view;

        init();
    }

    private void init() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    public void like(String postKey) {
        DatabaseReference post = databaseReference.child(DB_POSTS_KEY).child(postKey);

        post.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Post p = dataSnapshot.getValue(Post.class);

                    if (p != null) {
                        List<String> users = p.getLike();
                        if (users == null) {
                            users = new ArrayList<>();
                        }
                        if (users.contains(firebaseUser.getUid())) {
                            users.remove(firebaseUser.getUid());
                        } else {
                            users.add(firebaseUser.getUid());
                        }

                        post.child(DB_POSTS_LIKE)
                                .setValue(users)
                                .addOnFailureListener(e -> {
                                    view.postError(R.string.error_something_wrong);
                                    Log.e(LOG_COMMUNITY, e.getMessage());
                                });

                        post.child(DB_POSTS_LIKE_COUNT)
                                .setValue(users.size())
                                .addOnFailureListener(e -> {
                                    view.postError(R.string.error_something_wrong);
                                    Log.e(LOG_COMMUNITY, e.getMessage());
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                view.postError(R.string.error_something_wrong);
                Log.e(LOG_COMMUNITY, databaseError.getDetails());
            }
        });
    }

    public void createPost(User user, String title, String description, String date, String time, Uri image) {
        if (title.isEmpty()) {
            view.postError(R.string.error_empty_title);
        } else if (description.isEmpty()) {
            view.postError(R.string.error_empty_description);
        } else {
            postReference = databaseReference.child(DB_POSTS_KEY).push();

            if (image != null) {
                uploadImage(user, title, description, date, time, image);
            } else {
                uploadPost(user, title, description, date, time, null);
            }
        }
    }

    private void uploadImage(User user, String title, String description, String date, String time, Uri image) {
        StorageReference upload = storageReference.child(STORAGE_COMMUNITY_IMAGE).child(postReference.getKey() + STORAGE_IMAGE_TYPE);

        upload.putFile(image)
                .continueWithTask(task -> {
                    if (!task.isSuccessful() && task.getException() != null) {
                        Log.e(LOG_COMMUNITY, task.getException().getMessage());
                    }

                    return upload.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        uploadPost(user, title, description, date, time, task.getResult().toString());
                    } else {
                        if (task.getException() != null) {
                            Log.e(LOG_COMMUNITY, task.getException().getMessage());
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(LOG_COMMUNITY, e.getMessage()));
    }

    private void uploadPost(User user, String title, String description, String date, String time, String image) {
        Post post = new Post(
                title,
                description,
                date,
                time,
                image,
                firebaseUser.getUid(),
                user.getUser_Name(),
                user.getUser_Thumb_Image(),
                null
        );

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
