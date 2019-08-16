package com.example.chatify.Community.Search;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.chatify.R;
import com.example.chatify.Utils.BottomNavigationHelper;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class SearchActivity extends AppCompatActivity {

    private int ACTIVITY_NUM = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setupBottomNavigationView();

    }

    private void setupBottomNavigationView(){
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottom_nav_view_bar);
        BottomNavigationHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationHelper.enableNavigation(this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);


    }
}
