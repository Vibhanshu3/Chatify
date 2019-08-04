package com.example.chatify;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.Data.AllUsers;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUserActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

private Toolbar allUserToolbar;
private RecyclerView recyclerView;

private DatabaseReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_user);

        allUserToolbar = findViewById(R.id.allUser_toolbar);
        setSupportActionBar(allUserToolbar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerView = findViewById(R.id.allUser_RecView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userReference = FirebaseDatabase.getInstance().getReference().child("User");
        userReference.keepSynced(true);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<AllUsers> options = new FirebaseRecyclerOptions.Builder<AllUsers>()
                .setQuery(userReference, AllUsers.class)
                .build();

        FirebaseRecyclerAdapter<AllUsers,AllUserViewHolder> firebaseRecyclerAdapter =
           new FirebaseRecyclerAdapter<AllUsers, AllUserViewHolder>(options) {

               @Override
               protected void onBindViewHolder(@NonNull final AllUserViewHolder allUserViewHolder, final int i, @NonNull final AllUsers allUsers) {
                   allUserViewHolder.userName.setText(allUsers.getUser_Name());
                   allUserViewHolder.userstatus.setText(allUsers.getUser_Status());

                   Picasso.get().load(allUsers.getUser_Image()).placeholder(R.drawable.default_image).into(allUserViewHolder.userImage);

                   //tap to go to user profile...
                   allUserViewHolder.mview.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           String visited_user = getRef(i).getKey();
                           Intent profileIntent = new Intent(AllUserActivity.this,ProfileActivity.class);
                           profileIntent.putExtra("visited user",visited_user);
                           startActivity(profileIntent);
                       }
                   });
               }

               @NonNull
               @Override
               public AllUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent,false);
                    AllUserViewHolder allUserViewHolder = new AllUserViewHolder(view);
                    return allUserViewHolder;
               }


           };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public static class AllUserViewHolder extends RecyclerView.ViewHolder{

        View mview;
        TextView userName,userstatus;
        CircleImageView userImage;

        public AllUserViewHolder(@NonNull View itemView) {
            super(itemView);
            mview = itemView;
            userName = itemView.findViewById(R.id.all_user_name);
            userstatus = itemView.findViewById(R.id.all_user_status);
            userImage = itemView.findViewById(R.id.all_user_image);

        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        getMenuInflater().inflate(R.menu.search_menu, menu);
//
//        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
//        SearchView searchView = (SearchView) menuItem.getActionView();
//        searchView.setOnQueryTextListener(this);
//
//        return true;
//    }


}
