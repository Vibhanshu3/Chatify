package com.example.chatify.model;

import java.util.List;

public class User {
    private String Device_Token;
    private String User_Name;
    private String User_Image;
    private String User_Status;
    private String User_Thumb_Image;
    private UserState User_State;
    private List<String> groups;
    private List<String> posts;

    public User() {
    }

    public User(String User_Thumb_Image) {
        this.User_Thumb_Image = User_Thumb_Image;
    }

    public User(String User_Name, String User_Image, String User_Status) {
        this.User_Name = User_Name;
        this.User_Image = User_Image;
        this.User_Status = User_Status;
    }

    public String getDevice_Token() {
        return Device_Token;
    }

    public void setDevice_Token(String device_Token) {
        Device_Token = device_Token;
    }

    public String getUser_Name() {
        return User_Name;
    }

    public void setUser_Name(String user_Name) {
        User_Name = user_Name;
    }

    public String getUser_Image() {
        return User_Image;
    }

    public void setUser_Image(String user_Image) {
        User_Image = user_Image;
    }

    public String getUser_Status() {
        return User_Status;
    }

    public void setUser_Status(String user_Status) {
        User_Status = user_Status;
    }

    public String getUser_Thumb_Image() {
        return User_Thumb_Image;
    }

    public void setUser_Thumb_Image(String user_Thumb_Image) {
        User_Thumb_Image = user_Thumb_Image;
    }

    public UserState getUser_State() {
        return User_State;
    }

    public void setUser_State(UserState user_State) {
        User_State = user_State;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public List<String> getPosts() {
        return posts;
    }

    public void setPosts(List<String> posts) {
        this.posts = posts;
    }
}

