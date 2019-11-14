package com.example.chatify.presenter;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.chatify.model.Group;
import com.example.chatify.model.GroupMember;
import com.example.chatify.model.User;
import com.example.chatify.view.MainActivityView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.chatify.utils.AppConst.DB_GROUPS_KEY;
import static com.example.chatify.utils.AppConst.DB_USERS_GROUPS;
import static com.example.chatify.utils.AppConst.DB_USERS_KEY;
import static com.example.chatify.utils.AppConst.LOG_COMMUNITY;
import static com.example.chatify.utils.AppConst.LOG_MAIN;
import static com.example.chatify.utils.AppConst.STORAGE_COMMUNITY_IMAGE;
import static com.example.chatify.utils.AppConst.STORAGE_GROUP_IMAGE;
import static com.example.chatify.utils.AppConst.STORAGE_GROUP_THUMB_IMAGE;
import static com.example.chatify.utils.AppConst.STORAGE_IMAGE_TYPE;

public class MainActivityPresenter {
    private MainActivityView view;

    private DatabaseReference databaseReference;
    private DatabaseReference group;

    private StorageReference storageReference;

    public MainActivityPresenter(MainActivityView view) {
        this.view = view;

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    public void createGroup(String createdBy, String groupName, Uri groupImage, Bitmap bitmap, List<GroupMember> members) {
        if (groupName == null || groupName.equals("")) {
            view.error("Group name can't be empty!");
            return;
        }

        if (!(members.size() > 1)) {
            view.error("Please select atleast one contact");
            return;
        }

        group = databaseReference.child(DB_GROUPS_KEY).push();

        if (groupImage != null) {
            uploadImage(createdBy, groupName, groupImage, bitmap, members);
        } else {
            uploadGroup(createdBy, groupName, null,null, members);
        }
    }

    private void uploadImage(String createdBy, String groupName, Uri groupImage, Bitmap bitmap, List<GroupMember> members) {
        StorageReference upload = storageReference.child(STORAGE_GROUP_IMAGE).child(group.getKey() + STORAGE_IMAGE_TYPE);

        upload.putFile(groupImage)
                .continueWithTask(task -> {
                    if (!task.isSuccessful() && task.getException() != null) {
                        Log.e(LOG_MAIN, task.getException().getMessage());
                    }

                    return upload.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        uploadThumb(createdBy, groupName, task.getResult().toString(), bitmap, members);
                    } else {
                        if (task.getException() != null) {
                            Log.e(LOG_COMMUNITY, task.getException().getMessage());
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(LOG_COMMUNITY, e.getMessage()));
    }

    private void uploadThumb(String createdBy, String groupName, String groupImage, Bitmap bitmap, List<GroupMember> members) {
        StorageReference upload = storageReference.child(STORAGE_GROUP_THUMB_IMAGE).child(group.getKey() + STORAGE_IMAGE_TYPE);

        ByteArrayOutputStream bias = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bias);
        byte[] thumbByte = bias.toByteArray();

        upload.putBytes(thumbByte)
                .continueWithTask(task -> {
                    if (!task.isSuccessful() && task.getException() != null) {
                        Log.e(LOG_MAIN, task.getException().getMessage());
                    }

                    return upload.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        uploadGroup(createdBy, groupName, groupImage, task.getResult().toString(), members);
                    } else {
                        if (task.getException() != null) {
                            Log.e(LOG_COMMUNITY, task.getException().getMessage());
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(LOG_COMMUNITY, e.getMessage()));
    }

    private void uploadGroup(String createdBy, String groupName, String groupImage, String thumb, List<GroupMember> members) {
        Group g = new Group(group.getKey(), createdBy, groupName, groupImage, thumb, members);

        group.setValue(g)
                .addOnFailureListener(e -> {
                    Log.e(LOG_MAIN, e.getMessage());
                    view.error("Something went wrong");
                })
                .addOnSuccessListener(aVoid -> {
                    for (GroupMember member : members) {
                        DatabaseReference user = databaseReference.child(DB_USERS_KEY).child(member.getMember());
                        user.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    User u = dataSnapshot.getValue(User.class);

                                    List<String> groups;

                                    if (u != null && u.getGroups() != null) {
                                        groups = u.getGroups();
                                    } else {
                                        groups = new ArrayList<>();
                                    }

                                    groups.add(group.getKey());

                                    DatabaseReference reference = databaseReference.child(DB_USERS_KEY).child(Objects.requireNonNull(dataSnapshot.getKey())).child(DB_USERS_GROUPS);

                                    reference
                                            .setValue(groups)
                                            .addOnFailureListener(e -> {
                                                Log.e(LOG_MAIN, e.getMessage());
                                                view.error("Something went wrong");
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e(LOG_MAIN, databaseError.getMessage());
                                view.error("Something went wrong");
                            }
                        });
                    }
                    view.groupAdded(g);
                });
    }
}
