package com.example.yup.models;

import com.squareup.moshi.Json;

import java.util.ArrayList;
import java.util.List;

public class MyImage {
    @Json(name="id")
    public List<String> id;
    @Json(name="message")
    public String message;

    public List<String> getId() {
        return id;
    }

    public void setId(List<String> id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
