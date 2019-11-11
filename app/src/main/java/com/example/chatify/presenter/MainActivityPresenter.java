package com.example.chatify.presenter;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.chatify.utils.AppConst.DB_GROUPS_KEY;
import static com.example.chatify.utils.AppConst.DB_USERS_GROUPS;
import static com.example.chatify.utils.AppConst.DB_USERS_KEY;
import static com.example.chatify.utils.AppConst.LOG_MAIN;

public class MainActivityPresenter {
    private MainActivityView view;
    private DatabaseReference databaseReference;

    public MainActivityPresenter(MainActivityView view) {
        this.view = view;

        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void createGroup(String createdBy, String groupName, String groupImage, List<GroupMember> members) {
        if (groupName == null || groupName.equals("")) {
            view.error("Group name can't be empty!");
            return;
        }

        if (!(members.size() > 1)) {
            view.error("Please select atleast one contact");
            return;
        }

        DatabaseReference group = databaseReference.child(DB_GROUPS_KEY).push();

        group
                .setValue(new Group(createdBy, groupName, groupImage, members))
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
                    view.groupAdded();
                });
    }
}
