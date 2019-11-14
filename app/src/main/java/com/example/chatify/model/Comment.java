package com.example.chatify.model;

public class Comment {
    private String date;
    private String time;
    private String message;
    private String fromKey;
    private String fromName;
    private String fromImage;

    public Comment() {
    }

    public Comment(String date, String time, String message, String fromKey, String fromName, String fromImage) {
        this.date = date;
        this.time = time;
        this.message = message;
        this.fromKey = fromKey;
        this.fromName = fromName;
        this.fromImage = fromImage;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFromKey() {
        return fromKey;
    }

    public void setFromKey(String fromKey) {
        this.fromKey = fromKey;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromImage() {
        return fromImage;
    }

    public void setFromImage(String fromImage) {
        this.fromImage = fromImage;
    }
}
