package com.example.chatify.Data;

public class AllUsers {

    private String User_Name;
    private String User_Image;
    private String User_Status;
    private String User_Thumb_Image;

    public AllUsers() {
    }

    public String getUser_Thumb_Image() {
        return User_Thumb_Image;
    }

    public void setUser_Thumb_Image(String user_Thumb_Image) {
        User_Thumb_Image = user_Thumb_Image;
    }

    public AllUsers(String user_Thumb_Image) {
        User_Thumb_Image = user_Thumb_Image;
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

    public AllUsers(String user_Name, String user_Image, String user_Status) {
        User_Name = user_Name;
        User_Image = user_Image;
        User_Status = user_Status;
    }
}
