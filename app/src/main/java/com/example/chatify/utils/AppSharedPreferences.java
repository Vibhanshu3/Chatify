package com.example.chatify.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.chatify.model.User;
import com.google.gson.Gson;

import static android.content.Context.MODE_PRIVATE;
import static com.example.chatify.utils.AppConst.PREF_FILE;
import static com.example.chatify.utils.AppConst.PREF_USER;

public class AppSharedPreferences {
    public static void setUser(Context context, User user) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_FILE, MODE_PRIVATE).edit();
        editor.putString(PREF_USER, new Gson().toJson(user));
        editor.apply();
    }

    public static User getUser(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        return new Gson().fromJson(pref.getString(PREF_USER, ""), User.class);
    }
}
