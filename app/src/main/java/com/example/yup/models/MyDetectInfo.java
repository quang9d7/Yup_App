package com.example.yup.models;

import java.util.List;

public class MyDetectInfo {

    public String _id;
    public List<List<List<Float>>>boxes;
    public String raw_image;
    public List<Float>scores;
    public List<String>texts;

    public MyDetectInfo(String _id, List<List<List<Float>>> boxes, String raw_image, List<Float> scores, List<String> texts) {
        this._id = _id;
        this.boxes = boxes;
        this.raw_image = raw_image;
        this.scores = scores;
        this.texts = texts;
    }

    public MyDetectInfo() {
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public List<List<List<Float>>> getBoxes() {
        return boxes;
    }

    public void setBoxes(List<List<List<Float>>> boxes) {
        this.boxes = boxes;
    }

    public String getRaw_image() {
        return raw_image;
    }

    public void setRaw_image(String raw_image) {
        this.raw_image = raw_image;
    }

    public List<Float> getScores() {
        return scores;
    }

    public void setScores(List<Float> scores) {
        this.scores = scores;
    }

    public List<String> getTexts() {
        return texts;
    }

    public void setTexts(List<String> texts) {
        this.texts = texts;
    }
}
