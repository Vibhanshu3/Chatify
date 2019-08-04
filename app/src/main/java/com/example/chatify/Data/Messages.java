package com.example.chatify.Data;

public class Messages {

    String message, type, from;
    Boolean seen;
    Long time;

    public Messages() {
    }

    public Messages(String message, String type, Boolean seen, Long time, String from) {
        this.message = message;
        this.type = type;
        this.seen = seen;
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
