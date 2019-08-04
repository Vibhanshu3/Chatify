package com.example.chatify.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.chatify.MainActivity;
import com.example.chatify.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;


import java.util.Arrays;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import java.util.List;


public class LoginActivity extends AppCompatActivity {

    public static final int RC_SIGN_IN = 1;

    FirebaseAuth mauth;
    private FirebaseUser currentUser;
    DatabaseReference userReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mauth = FirebaseAuth.getInstance();
      //  currentUser = mauth.getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference();


    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mauth.getCurrentUser() == null) {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.PhoneBuilder().build()
                    //  new AuthUI.IdpConfig.GoogleBuilder().build(),
                    //  new AuthUI.IdpConfig.FacebookBuilder().build(),
                    //  new AuthUI.IdpConfig.TwitterBuilder().build()
            );

            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setLogo(R.drawable.chatifyname)      // Set logo drawable
                            // Set theme
                            .build(),
                    RC_SIGN_IN);
            // [END auth_fui_create_intent]
        }

        if(mauth.getCurrentUser()!=null){
          //  userReferenceO.child("Online").setValue("true");
        }


    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mauth.getCurrentUser()!=null){
            //userReferenceO.child("Online").setValue(ServerValue.TIMESTAMP);
        }
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
                if(mauth.getCurrentUser()!=null){
                  //  userReferenceO.child("Online").setValue(ServerValue.TIMESTAMP);
                }
                logout();
                return true;

            case R.id.main_menu_account:
//                Intent intent = new Intent(LoginActivity2.this, SettingActivity.class);
//                startActivity(intent);
                  return true;

            case R.id.main_menu_Users:
//                Intent intent1 = new Intent(LoginActivity2.this,AllUserActivity.class);
//                startActivity(intent1);
                  return true;

            default:
                return false;
        }
    }

    private void logout() {

        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(LoginActivity.this, "Sign Out", Toast.LENGTH_SHORT).show();
                        onStart();
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                 currentUser = FirebaseAuth.getInstance().getCurrentUser();
                 userReference = FirebaseDatabase.getInstance().getReference();

             //    userReference.child("User").child(currentUser.getUid()).child("Online").setValue("true");

                //  flag =1;
                // online_user = user.getUid();

                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                //String online_userO = mauth.getCurrentUser().getUid();

                userReference.child("User").child(mauth.getCurrentUser().getUid()).child("Device_Token").setValue(deviceToken)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //
                            }
                        });

                Intent settingIntent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(settingIntent);
                finish();

                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...

                String e = response.getError().toString();
                Toast.makeText(this, "e" + e, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
