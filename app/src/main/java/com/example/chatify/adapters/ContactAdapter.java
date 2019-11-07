package com.example.chatify.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.R;
import com.example.chatify.model.Contact;
import com.example.chatify.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.chatify.AppUtils.AppConst.DB_USERS_KEY;
import static com.example.chatify.AppUtils.AppConst.DB_USERS_STATE_ONLINE;
import static com.example.chatify.AppUtils.AppConst.LOG_MAIN_ACTIVITY;

public class ContactAdapter extends FirebaseRecyclerAdapter<Contact, ContactAdapter.ContactViewHolder> {
    private DatabaseReference userReference;
    private ClickListener clickListener;

    public interface ClickListener {
        void onContactSelected(String id, View view);
    }

    public ContactAdapter(@NonNull FirebaseRecyclerOptions<Contact> options, ClickListener clickListener) {
        super(options);
        this.clickListener =clickListener;
        userReference = FirebaseDatabase.getInstance().getReference().child(DB_USERS_KEY);
    }

    @Override
    protected void onBindViewHolder(@NonNull ContactViewHolder viewHolder, int i, @NonNull Contact contact) {
        userReference.child(Objects.requireNonNull(getRef(i).getKey())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);

                    if (user != null) {
                        if (user.getUser_State().getState().equals(DB_USERS_STATE_ONLINE)) {
                            viewHolder.onlineStatus.setVisibility(View.VISIBLE);
                        } else {
                            viewHolder.onlineStatus.setVisibility(View.INVISIBLE);
                        }

                        Picasso.get().load(user.getUser_Image()).placeholder(R.drawable.default_image).into(viewHolder.userImage);
                        viewHolder.userName.setText(user.getUser_Name());
                        viewHolder.userStatus.setText(user.getUser_Status());

                        viewHolder.container.setOnClickListener(v -> clickListener.onContactSelected(dataSnapshot.getKey(), v));
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
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
        return new ContactViewHolder(view);
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        private TextView userName;
        private TextView userStatus;
        private CircleImageView userImage;
        private ImageView onlineStatus;
        private CardView container;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.all_user_name);
            userStatus = itemView.findViewById(R.id.all_user_status);
            userImage = itemView.findViewById(R.id.all_user_image);
            onlineStatus = itemView.findViewById(R.id.online_user);
            container = itemView.findViewById(R.id.user_display_container);
        }
    }
}
