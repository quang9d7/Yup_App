package com.example.yup.models;

import com.squareup.moshi.Json;

public class MyDetectImage {

    @Json(name="_id")
    public String _id;
    @Json(name="detect_id")
    public String detect_id;
    @Json(name="status")
    public String status;
    @Json(name="url")
    public String url;

    public MyDetectImage() {
        this._id = null;
        this.detect_id = null;
        this.status = null;
        this.url = null;
    }

    public MyDetectImage(String _id, String detect_id, String status, String url) {
        this._id = _id;
        this.detect_id = detect_id;
        this.status = status;
        this.url = url;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getDetect_id() {
        return detect_id;
    }

    public void setDetect_id(String detect_id) {
        this.detect_id = detect_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
