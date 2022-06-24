package com.example.yup.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmImage extends RealmObject {

    @PrimaryKey
    private String id;
    private String detect_id;
    private String url;
    private RealmList<String>texts;
    private String status;

    public RealmImage() {

    }

    public RealmImage(String id, String detect_id, String url, String status) {
        this.id = id;
        this.detect_id = detect_id;
        this.url = url;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDetect_id() {
        return detect_id;
    }

    public void setDetect_id(String detect_id) {
        this.detect_id = detect_id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public RealmList<String> getTexts() {
        return texts;
    }

    public void setTexts(RealmList<String> texts) {
        this.texts = texts;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
