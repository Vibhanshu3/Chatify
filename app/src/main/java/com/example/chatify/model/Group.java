package com.example.chatify.model;

import java.io.Serializable;
import java.util.List;

public class Group  implements Serializable {
    private String groupId;
    private String createdBy;
    private String groupName;
    private String groupImage;
    private List<GroupMember> members;

    public Group() {
    }

    public Group(String groupId, String createdBy, String groupName, String groupImage, List<GroupMember> members) {
        this.groupId = groupId;
        this.createdBy = createdBy;
        this.groupName = groupName;
        this.groupImage = groupImage;
        this.members = members;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupImage() {
        return groupImage;
    }

    public void setGroupImage(String groupImage) {
        this.groupImage = groupImage;
    }

    public List<GroupMember> getMembers() {
        return members;
    }

    public void setMembers(List<GroupMember> members) {
        this.members = members;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
