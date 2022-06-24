package com.example.yup.models;

import android.graphics.Bitmap;

import com.squareup.picasso.Picasso;

import java.io.IOException;

public class DownloadImage implements Runnable{

    private Bitmap bm;
    private String url;

    public DownloadImage() {

    }

    public DownloadImage(String url) {
        this.url = url;
    }

    public DownloadImage(Bitmap bm, String url) {
        this.bm = bm;
        this.url = url;
    }

    public Bitmap getBm() {
        return bm;
    }

    public void setBm(Bitmap bm) {
        this.bm = bm;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void run() {
        Bitmap bitmap=null;
        try {
             bitmap= Picasso.get().load(url).get();
             this.bm=bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
