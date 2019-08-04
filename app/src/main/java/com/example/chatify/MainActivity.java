package com.example.chatify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.viewpager.widget.ViewPager;

import com.example.chatify.Adapters.TabsAccessorAdapter;
import com.example.chatify.Login.LoginActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;

    FirebaseAuth mauth;
    private DatabaseReference databaseReference;
    String currentUser;
    FirebaseUser user;
    int flag = 0;
    int flag2 = 0;boolean isInBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mtoolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Chatify");

        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        myTabLayout = findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

        mauth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        user = mauth.getCurrentUser();

        if(user != null){
            updateUserStatus("Online");
        }

        RunningAppProcessInfo myProcess = new RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(myProcess);
        isInBackground = myProcess.importance != RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
        if(isInBackground) {
            Log.d("Tutorialspoint.com","Your application is in background state");
        }else{
            Log.d("Tutorialspoint.com","application is in forground state");

        }

    }



    @Override
    public void onStart() {
        super.onStart();
        Log.d("main", "onStart: " + currentUser);

        if(user == null){
            Log.d("user", "onStart: " + "null");
            sendUserToLoginActivity();
           // currentUser = mauth.getCurrentUser().getUid();
        }else {

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

        Log.d("sttp", "onUserLeaveHint: "+"sttp");
        if(user!=null){
            if(flag == 0) {
                updateUserStatus("Offline");
                Log.d("see", "onStop: " + "see");

            }
            else{
                Log.d("stop", "onStop: " + flag);
                updateUserStatus("Online");
                flag = 0;

            }
        }
    }





//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if(user!=null){
//            updateUserStatus("Offline");
//
//        }
//    }

    private void verifyUserExistance() {
       currentUser = mauth.getCurrentUser().getUid();

        databaseReference.child("User").child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("User_Name").exists()){
                    Log.d("check", "onDataChange: " + "check");
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }else{
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.main_menu_logout:
                logout();
                updateUserStatus("Offline");
                return true;

            case R.id.main_menu_account:
                sendUserToSettingActivity();
                flag = 1;
                return true;

            case R.id.main_menu_Users:
                Intent intent1 = new Intent(MainActivity.this,AllUserActivity.class);
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
                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Plz write group name", Toast.LENGTH_SHORT).show();
                }else{
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
                        if(task.isSuccessful()){
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

    private void updateUserStatus(String state){

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


}
