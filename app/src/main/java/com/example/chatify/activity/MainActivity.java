package com.example.chatify.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.chatify.AllUserActivity;
import com.example.chatify.GroupActivity;
import com.example.chatify.R;
import com.example.chatify.RequestActivity;
import com.example.chatify.SettingActivity2;
import com.example.chatify.adapters.ContactAdapter;
import com.example.chatify.adapters.GroupsAdapter;
import com.example.chatify.adapters.SearchAdapter;
import com.example.chatify.adapters.TabsAccessorAdapter;
import com.example.chatify.fragments.CommunityFragment;
import com.example.chatify.model.Contact;
import com.example.chatify.model.Group;
import com.example.chatify.model.GroupMember;
import com.example.chatify.model.User;
import com.example.chatify.presenter.MainActivityPresenter;
import com.example.chatify.utils.AppUtils;
import com.example.chatify.view.MainActivityView;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.chatify.utils.AppConst.DB_CONTACTS_KEY;
import static com.example.chatify.utils.AppConst.DB_GROUPS_ROLE_ADMIN;
import static com.example.chatify.utils.AppConst.DB_GROUPS_ROLE_MEMBER;
import static com.example.chatify.utils.AppConst.DB_USERS_GROUPS;
import static com.example.chatify.utils.AppConst.DB_USERS_KEY;

//FixMe: check of user is null

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, View.OnClickListener, MainActivityView, ContactAdapter.ClickListener, GroupsAdapter.ClickListener {
    @BindView(R.id.main_activity_navigation_groups)
    RecyclerView groupsRecyclerView;

    private List<GroupMember> groupContacts;

    private EditText dialogGroupName;
    private Dialog dialog;

    private FirebaseUser user;
    private DatabaseReference databaseReference;

    private MainActivityPresenter presenter;
    private User userDetails;

    private Toolbar mtoolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;

    //navigation drawer.
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;

    FirebaseAuth mauth;
    String currentUser;
    int flag = 0;

    List<String> list;
    private SearchAdapter searchAdapter;
    private RelativeLayout relativeLayout;

    private RecyclerView userRecView;
    int tag =0;

    private TextView badgecounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mtoolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        //setting navigation drawer.
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

        }else {

        }

        mauth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        user = mauth.getCurrentUser();

        list = new ArrayList<>();
        relativeLayout = findViewById(R.id.search_relative_layout);

        if (user != null) {
            init();
            updateUserStatus("Online");

            //for search.
            databaseReference.child("Contacts").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                         String contactID = postSnapshot.getRef().getKey();

                        databaseReference.child("User").child(contactID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String username = dataSnapshot.child("User_Name").getValue().toString();
                                list.add(username);
                                Log.d("listlist", "onDataChange: " + list);

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
//            userRecView.setLayoutManager(new LinearLayoutManager(this));

        }
    }

    private void init() {
        presenter = new MainActivityPresenter(this);

        GroupsAdapter groupsAdapter = new GroupsAdapter(new FirebaseRecyclerOptions
                .Builder<String>()
                .setQuery(databaseReference.child(DB_USERS_KEY).child(user.getUid()).child(DB_USERS_GROUPS), String.class)
                .build(), this);

        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupsRecyclerView.setAdapter(groupsAdapter);
        groupsAdapter.startListening();
    }

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
            verifyUserExistence();
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

    private void verifyUserExistence() {
        currentUser = mauth.getCurrentUser().getUid();

        databaseReference.child(DB_USERS_KEY).child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //FixME: what this code is doing?
                if (dataSnapshot.child("User_Name").exists()) {
                    Log.d("check", "onDataChange: " + "check");
//                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
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

        MenuItem menuItem1 = menu.findItem(R.id.main_menu_request);

        databaseReference.child("Chat Request").child(currentUser)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("badgecount", "onDataChange: " + dataSnapshot.getChildrenCount());
                        String count = String.valueOf(dataSnapshot.getChildrenCount());
                        if(!count.equals("0")) {
                            menuItem1.setActionView(R.layout.request_badge);
                            View view = menuItem1.getActionView();
                            badgecounter = view.findViewById(R.id.badge_counter);
                            badgecounter.setText(count);
                            view.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RequestActivity.class)));
                        }
                        else
                            menuItem1.setActionView(null);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.main_menu_search:
                tag=1;
                relativeLayout.setVisibility(View.VISIBLE);
                userRecView = findViewById(R.id.user_rec_view);
                userRecView.setHasFixedSize(true);
                searchAdapter = new SearchAdapter(MainActivity.this, list);
                userRecView.setAdapter(searchAdapter);
                userRecView.setLayoutManager(new LinearLayoutManager(this));
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
                addGroup();
                flag = 1;
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if(fragment instanceof CommunityFragment) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
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
                .addOnCompleteListener(task -> {
                    Toast.makeText(MainActivity.this, "Sign Out", Toast.LENGTH_SHORT).show();
                    Log.d("us", "onComplete: " + "logout");
                    sendUserToLoginActivity();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                dialog.dismiss();
                break;
            case R.id.create:
                presenter.createGroup(user.getUid(), dialogGroupName.getText().toString(), "", groupContacts);
                break;
            case R.id.dialog_add_group_icon:
                //add image


        }
    }

    @Override
    public void onContactSelected(String id, View view) {
        GroupMember member = AppUtils.checkGroupMemberExist(id, groupContacts);

        if (member == null) {
            groupContacts.add(new GroupMember(id, DB_GROUPS_ROLE_MEMBER));
            view.setBackgroundColor(Color.GREEN);
        } else {
            groupContacts.remove(member);
            view.setBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public void error(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void groupAdded(Group group) {
        dialog.dismiss();
        Toast.makeText(this, "goto chat screen", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGroupSelected(Group group) {
        Toast.makeText(this, "goto chat screen", Toast.LENGTH_SHORT).show();
        Intent groupIntent = new Intent(MainActivity.this, GroupActivity.class);
        groupIntent.putExtra("group",new Gson().toJson(group));
        startActivity(groupIntent);
    }

    @OnClick(R.id.main_activity_add_group)
    public void addGroup() {
        groupContacts = new ArrayList<>();
        groupContacts.add(new GroupMember(user.getUid(), DB_GROUPS_ROLE_ADMIN));

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_group);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        Window window = dialog.getWindow();

        Objects.requireNonNull(window).setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);

        dialog.findViewById(R.id.cancel).setOnClickListener(this);
        dialog.findViewById(R.id.create).setOnClickListener(this);
        dialog.findViewById(R.id.dialog_add_group_icon).setOnClickListener(this);

        dialogGroupName = dialog.findViewById(R.id.dialog_add_group_name);

        ContactAdapter adapter = new ContactAdapter(new FirebaseRecyclerOptions
                .Builder<Contact>()
                .setQuery(databaseReference.child(DB_CONTACTS_KEY).child(user.getUid()), Contact.class)
                .build(), this);

        RecyclerView dialogContactsList = dialog.findViewById(R.id.dialog_group_contacts_list);
        dialogContactsList.setLayoutManager(new LinearLayoutManager(this));
        dialogContactsList.setAdapter(adapter);

        adapter.startListening();

        dialog.show();
    }
}


