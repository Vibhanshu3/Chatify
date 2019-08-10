package com.example.chatify;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.chatify.Adapters.SearchAdapter;
import com.example.chatify.Adapters.TabsAccessorAdapter;
import com.example.chatify.Data.AllUsers;
import com.example.chatify.Fragments.ChatsFragment;
import com.example.chatify.Login.LoginActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private Toolbar mtoolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;

    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;

    FirebaseAuth mauth;
    private DatabaseReference databaseReference;
    String currentUser;
    FirebaseUser user;
    int flag = 0;

    List<String> list;
    private SearchAdapter searchAdapter;

    private RecyclerView userRecView;
    int tag =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mtoolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Chatify");

        drawerLayout = findViewById(R.id.main_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, mtoolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);

        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });


        if(tag == 0) {
            myViewPager = findViewById(R.id.main_tabs_pager);
            myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
            myViewPager.setAdapter(myTabsAccessorAdapter);

            myTabLayout = findViewById(R.id.main_tabs);
            myTabLayout.setupWithViewPager(myViewPager);
        }

            userRecView = findViewById(R.id.user_rec_view);
            userRecView.setHasFixedSize(true);
            userRecView.setLayoutManager(new LinearLayoutManager(this));

            searchAdapter = new SearchAdapter(MainActivity.this, list);
            userRecView.setAdapter(searchAdapter);


        mauth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        user = mauth.getCurrentUser();







        list = new ArrayList<>();

        if (user != null) {
            updateUserStatus("Online");

            databaseReference.child("Contacts").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.e("Count " ,""+dataSnapshot.getChildrenCount());
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                         String contactID = postSnapshot.getRef().getKey();
                        Log.d("getData", "onDataChange: "+ postSnapshot.getRef());

                        databaseReference.child("User").child(contactID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String username = dataSnapshot.child("User_Name").getValue().toString();
                                Log.d("username", "onDataChange:  " + username);
                                list.add(username);
                                Log.d("list", "onDataChange:  " + list);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("The read failed: " ,databaseError.getMessage());

                }
            });

        }

        Log.d("listuser", "onCreate: " + list);


    }

//    @Override
//    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
//        super.onPostCreate(savedInstanceState, persistentState);
//        drawerToggle.syncState();
//
//
//    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("main", "onStart: " + currentUser);

        if (user == null) {
            Log.d("user", "onStart: " + "null");
            sendUserToLoginActivity();
            // currentUser = mauth.getCurrentUser().getUid();
        } else {

            updateUserStatus("Online");

            Log.d("main", "onStart: " + "in main");
            verifyUserExistance();
        }
        //   Log.d("main", "onStart: " + currentUser);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d("sttp", "onUserLeaveHint: " + "sttp");
        if (user != null) {
            if (flag == 0) {
                updateUserStatus("Offline");
                Log.d("see", "onStop: " + "see");

            } else {
                Log.d("stop", "onStop: " + flag);
                updateUserStatus("Online");
                flag = 0;

            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(user!=null){
            updateUserStatus("Offline");

        }
    }

    private void verifyUserExistance() {
        currentUser = mauth.getCurrentUser().getUid();

        databaseReference.child("User").child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("User_Name").exists()) {
                    Log.d("check", "onDataChange: " + "check");
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Go to settings", Toast.LENGTH_SHORT).show();
                    sendUserToSettingActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.main_menu_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.main_menu_search:
                tag=1;
                return true;

            case R.id.main_menu_logout:
                logout();
                updateUserStatus("Offline");
                return true;

            case R.id.main_menu_account:
                sendUserToSettingActivity();
                flag = 1;
                return true;

            case R.id.main_menu_Users:
                Intent intent1 = new Intent(MainActivity.this, AllUserActivity.class);
                startActivity(intent1);
                flag = 1;
                return true;

            case R.id.main_menu_Group:
                requestNewGroup();
                flag = 1;
                return true;

            default:
                return false;
        }
    }

    private void requestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name");


        final EditText groupNameField = new EditText(this);
        groupNameField.setHint("Coding");

        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    Toast.makeText(MainActivity.this, "Plz write group name", Toast.LENGTH_SHORT).show();
                } else {
                    creatNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });

        builder.show();
    }

    private void creatNewGroup(String groupName) {
        databaseReference.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Group is created", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void sendUserToSettingActivity() {
        Intent intent = new Intent(MainActivity.this, SettingActivity2.class);
        startActivity(intent);

    }

    private void logout() {

        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this, "Sign Out", Toast.LENGTH_SHORT).show();
                        Log.d("us", "onComplete: " + "logout");
                        sendUserToLoginActivity();
                    }
                });
    }

    private void updateUserStatus(String state) {

        String saveCurrTime, saveCurrDate;

        Calendar calender = Calendar.getInstance();

        SimpleDateFormat currDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrDate = currDate.format(calender.getTime());

        SimpleDateFormat currTime = new SimpleDateFormat("hh:mm a");
        saveCurrTime = currTime.format(calender.getTime());

        HashMap<String, Object> onlineState = new HashMap<>();
        onlineState.put("Time", saveCurrTime);
        onlineState.put("Date", saveCurrDate);
        onlineState.put("State", state);

        //currentUser = mauth.getCurrentUser().getUid();

        databaseReference.child("User").child(user.getUid()).child("User_State")
                .updateChildren(onlineState);


    }

//    public void updateList(List<String> newList){
//        name = new ArrayList<>();
//        names.addAll(newList);
//        notifyDataSetChanged();
//    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userInput = newText.toLowerCase();
        Log.d("list", "onQueryTextChange: " + list);
        Log.d("userinput", "onQueryTextChange: " + userInput);
        List<String> newList = new ArrayList<>();

        for(String names : list){
            if(names.toLowerCase().contains(userInput)){
                newList.add(names);
            }
        }
        Log.d("newlist", "onQueryTextChange: " + newList);

        searchAdapter.updateList(newList);


        return true;
    }

}


