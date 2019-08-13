package com.example.chatify.Data;

public class Group {

    private String Group_Name;
    private String Group_Image;

    public Group(String group_Name, String group_Image) {
        Group_Name = group_Name;
        Group_Image = group_Image;
    }

    public Group() {
    }

    public String getGroup_Name() {
        return Group_Name;
    }

    public void setGroup_Name(String group_Name) {
        Group_Name = group_Name;
    }

    public String getGroup_Image() {
        return Group_Image;
    }

    public void setGroup_Image(String group_Image) {
        Group_Image = group_Image;
    }
}
