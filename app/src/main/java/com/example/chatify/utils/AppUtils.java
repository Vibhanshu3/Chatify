package com.example.chatify.utils;

import com.example.chatify.model.GroupMember;

import java.util.List;

public class AppUtils {
    public static GroupMember checkGroupMemberExist(String id, List<GroupMember> members) {
        for (GroupMember member : members) {
            if (member.getMember().equals(id)) {
                return member;
            }
        }
        return null;
    }
}
