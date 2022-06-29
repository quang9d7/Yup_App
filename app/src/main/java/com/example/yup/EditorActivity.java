package com.example.yup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.FileProvider;

import com.example.yup.models.DownloadImage;
import com.example.yup.models.InfoMessage;
import com.example.yup.models.MyDetectImage;
import com.example.yup.models.MyDetectInfo;
import com.example.yup.utils.ApiService;
import com.example.yup.utils.Client;
import com.example.yup.utils.ImageDrawView;
import com.example.yup.utils.Point;
import com.example.yup.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditorActivity extends AppCompatActivity {
    AppCompatImageButton saveImageBtn;
    AppCompatImageButton shareBtn;
    AppCompatImageButton deleteBtn;
    ImageDrawView imageDrawView;
    ProgressBar progressBar;
    ApiService service;
    SharedPreferences sharedPreferences;
    SessionManager sessionManager;

    void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Intent intent = getIntent();
        String image_id = intent.getStringExtra("image_id");

        sharedPreferences = getSharedPreferences(getResources().getString(R.string.yup_sp), MODE_PRIVATE);
        sessionManager = SessionManager.getInstance(sharedPreferences);
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);

        Log.d("image_id in temp Activity", image_id);
        imageDrawView = findViewById(R.id.mainImage);
        saveImageBtn = findViewById(R.id.saveImageBtn);
        shareBtn = findViewById(R.id.shareBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        progressBar = findViewById(R.id.progressBar);
        showProgressBar();
        Call<MyDetectImage> call_detect = service.getDetectId(image_id);
        call_detect.enqueue(new Callback<MyDetectImage>() {
            @Override
            public void onResponse(Call<MyDetectImage> call, Response<MyDetectImage> response) {
                if (response.isSuccessful()) {
                    MyDetectImage myDetectImage = response.body();
                    String id_detect = myDetectImage.getDetect_id();
                    String url = myDetectImage.getUrl();
                    DownloadImage downloadImage = new DownloadImage(Uri.parse(url).toString());
                    Thread thread = new Thread(downloadImage);
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Bitmap myImg = downloadImage.getBm();
                    Call<MyDetectInfo> call_details = service.getDetailDetect(id_detect);
                    call_details.enqueue(new Callback<MyDetectInfo>() {
                        @Override
                        public void onResponse(Call<MyDetectInfo> call, Response<MyDetectInfo> response) {
                            if (response.isSuccessful()) {
                                MyDetectInfo myDetectInfo = response.body();
                                List<List<List<Float>>> rawBoxes = myDetectInfo.getBoxes();
                                Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                                mPaint.setColor(Color.BLUE);
                                mPaint.setStrokeWidth(5);
                                mPaint.setStyle(Paint.Style.STROKE);
                                imageDrawView.drawBitmapWithBoundingBoxes(myImg, transformToPoints(rawBoxes), mPaint);
                                imageDrawView.setLabels(myDetectInfo.getTexts());
                                imageDrawView.setScores(myDetectInfo.getScores());
                                hideProgressBar();
                            }
                        }

                        @Override
                        public void onFailure(Call<MyDetectInfo> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<MyDetectImage> call, Throwable t) {

            }
        });
        saveImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saveFile() != null)
                    // Notify that the image saved successfully
                    Toast.makeText(EditorActivity.this, "Đã lưu ảnh thành công", Toast.LENGTH_SHORT).show();
            }
        });
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = saveFile();
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/jpeg");
                startActivity(intent);
            }
        });
    }
    Uri saveFile() {
        Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(5);
        mPaint.setStyle(Paint.Style.STROKE);
        Bitmap processedBitmap = imageDrawView.getFullSizeBitmap(mPaint);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            FileOutputStream fOut = new FileOutputStream(image);
            processedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();
            return FileProvider.getUriForFile(
                    EditorActivity.this,
                    "com.example.yup.provider",
                    image);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    List<List<Point>> transformToPoints(List<List<List<Float>>> raw) {
        List<List<Point>> boxes = new ArrayList<>();
        for (int i=0; i<raw.size(); ++i) {
            List<List<Float>> points = raw.get(i);
            List<Point> box = new ArrayList<>();
            for (int p=0; p<points.size(); ++p) {
                List<Float> rawPoint = points.get(p);
                Point point = new Point(rawPoint.get(0), rawPoint.get(1));
                box.add(point);
            }
            boxes.add(box);
        }
        return boxes;
    }
}
