package com.example.chatify.view;

import com.example.chatify.model.User;

public interface CommunityView {
    void postError(int error);
    void postSuccess(User user);
}
