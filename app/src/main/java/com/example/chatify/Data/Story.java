package com.example.chatify.Data;

public class Story {

    private String imageurl;
    private long timestart;
    private long timeend;
    private  String storyID;
    private  String userID;

    public Story() {
    }

    public Story(String imageurl, long timestart, long timeend, String storyID, String userID) {
        this.imageurl = imageurl;
        this.timestart = timestart;
        this.timeend = timeend;
        this.storyID = storyID;
        this.userID = userID;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public long getTimestart() {
        return timestart;
    }

    public void setTimestart(long timestart) {
        this.timestart = timestart;
    }

    public long getTimeend() {
        return timeend;
    }

    public void setTimeend(long timeend) {
        this.timeend = timeend;
    }

    public String getStoryID() {
        return storyID;
    }

    public void setStoryID(String storyID) {
        this.storyID = storyID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
