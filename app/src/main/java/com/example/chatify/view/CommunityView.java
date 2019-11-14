package com.example.chatify.view;

import com.example.chatify.model.Comment;
import com.example.chatify.model.User;

public interface CommunityView {
    void hideLoader();
    void error(int error);
    void postSuccess(User user);
    void commentSuccess(Comment comment);
}
