package com.example.chatify.view;

import com.example.chatify.model.Group;

public interface MainActivityView {
    void error(String error);
    void groupAdded(Group group);
}
