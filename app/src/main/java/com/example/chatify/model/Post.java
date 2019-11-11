package com.example.chatify.model;

public class Post {
    private String title;
    private String description;
    private String date;
    private String time;
    private String image;
    private String userId;
    private String userName;
    private String userImage;

    public Post(String title, String description, String date, String time, String image, String userId, String userName, String userImage) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.image = image;
        this.userId = userId;
        this.userName = userName;
        this.userImage = userImage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
