package com.example.yup.models;

import com.squareup.moshi.Json;

import java.util.List;

public class UserInfo {
    @Json(name="uid")
    private String uid;
    @Json(name="name")
    private String name;
    @Json(name="email")
    private String email;
    @Json(name="images")
    private List<String> images;

    public UserInfo() {

    }

    public UserInfo(String uid, String name, String email, List<String> images) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.images = images;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

}
