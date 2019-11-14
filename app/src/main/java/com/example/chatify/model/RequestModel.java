package com.example.chatify.model;

import java.util.List;

public class RequestModel {

    private String Device_Token;
    private String User_Name;
    private String User_Image;
    private String User_Status;
    private String User_Thumb_Image;
    private UserState User_State;
    private List<String> groups;
    private List<String> posts;
    private String reqType;
    private String userId;

    public RequestModel(String device_Token, String user_Name, String user_Image, String user_Status, String user_Thumb_Image, UserState user_State, List<String> groups, List<String> posts, String reqType, String userId) {
        Device_Token = device_Token;
        User_Name = user_Name;
        User_Image = user_Image;
        User_Status = user_Status;
        User_Thumb_Image = user_Thumb_Image;
        User_State = user_State;
        this.groups = groups;
        this.posts = posts;
        this.reqType = reqType;
        this.userId = userId;
    }

    public RequestModel() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getReqType() {
        return reqType;
    }

    public void setReqType(String reqType) {
        this.reqType = reqType;
    }
}
