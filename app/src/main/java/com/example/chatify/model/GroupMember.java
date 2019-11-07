package com.example.chatify.model;

public class GroupMember {
    private String member;
    private String role;

    public GroupMember() {
    }

    public GroupMember(String member, String role) {
        this.member = member;
        this.role = role;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
