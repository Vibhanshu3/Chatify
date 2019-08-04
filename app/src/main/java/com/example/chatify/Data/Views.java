package com.example.chatify.Data;

public class Views {

    boolean seen;

    public Views(boolean seen) {
        this.seen = seen;
    }

    public Views() {
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
