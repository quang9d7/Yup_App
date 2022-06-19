package com.example.yup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.yup.adapters.ImageAdapter;
import com.example.yup.adapters.UserAdapter;
import com.example.yup.models.ErrorMessage;
import com.example.yup.models.MyDetectImage;
import com.example.yup.models.MyDetectInfo;
import com.example.yup.models.MyImage;
import com.example.yup.models.TokenPair;
import com.example.yup.utils.ApiService;
import com.example.yup.utils.Client;
import com.example.yup.utils.SessionManager;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import kotlin.jvm.Synchronized;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    ArrayList<Uri> images = new ArrayList<>();
    RecyclerView imageCollection;
    FloatingActionButton pickImageButton;
    ImageAdapter imageAdapter;
    AppCompatImageButton logout_btn;
    BottomAppBar bottomAppBar;
    NestedScrollView galleryScrollView;
    LinearLayoutCompat pickImageContextMenu;
    ExtendedFloatingActionButton pickImgFromStorageBtn, takeImageBtn;
    String abs_path_storage="storage/emulated/0";
    ApiService service;
    Call<MyImage> call;
    Call<MyDetectImage>call_detect;
    Call<MyDetectInfo>call_detail_detect;
    MediaType MEDIA_TYPE_IMG = MediaType.parse("image/*");
    private static final int READ_PERMISSION = 101;
    SessionManager sessionManager;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_temp);

        pickImageButton = findViewById(R.id.fab);
        logout_btn=findViewById(R.id.logout_btn);
        bottomAppBar = findViewById(R.id.bottom_app_bar);
        galleryScrollView = findViewById(R.id.galleryScrollView);
        pickImageContextMenu = findViewById(R.id.pickImageContextMenu);
        pickImgFromStorageBtn = findViewById(R.id.pickImageFromStorageBtn);
        takeImageBtn = findViewById(R.id.takeImageBtn);

        imageCollection = findViewById(R.id.imageCollection);
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        imageCollection.setLayoutManager(gridLayoutManager);
        imageCollection.setItemAnimator(new DefaultItemAnimator());
        galleryScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                int dy = i1 - i3;
                if (dy > 0 && bottomAppBar.getFabAlignmentMode() == BottomAppBar.FAB_ALIGNMENT_MODE_CENTER)
                {
                    bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
                }
                else if (dy < 0 && bottomAppBar.getFabAlignmentMode() == BottomAppBar.FAB_ALIGNMENT_MODE_END)
                {
                    bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
                }
            }
        });

        imageAdapter = new ImageAdapter(images,this);
        imageCollection.setAdapter(imageAdapter);

        service = Client.createService(ApiService.class);

        sharedPreferences = getSharedPreferences(getResources().getString(R.string.yup_sp), MODE_PRIVATE);
        sessionManager = SessionManager.getInstance(sharedPreferences);



        if (ContextCompat.checkSelfPermission
                (DashboardActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DashboardActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSION);
        }

        pickImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FloatingActionButton fab = (FloatingActionButton) view;
                if (pickImageContextMenu.getVisibility() == View.INVISIBLE) {
                    pickImageContextMenu.setVisibility(View.VISIBLE);
                    fab.setImageDrawable(getDrawable(R.drawable.ic_baseline_close_24));
                    fab.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.red)));
                } else {
                    fab.setImageDrawable(getDrawable(R.drawable.ic_baseline_image_24));
                    fab.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.fabButtonColor)));
                    pickImageContextMenu.setVisibility(View.INVISIBLE);
                }
            }
        });

        pickImgFromStorageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select images"),1);
                pickImageButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_image_24));
                pickImageButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.fabButtonColor)));
                pickImageContextMenu.setVisibility(View.INVISIBLE);
            }
        });

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionManager.deleteToken();
                Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
            }
        });
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {
                int x = data.getClipData().getItemCount();
                for (int i=0; i<x; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    images.add(uri);
                    imageAdapter.notifyDataSetChanged();
                }

            }
            else if (data.getData() != null) {

                    String imageUrl = data.getData().getPath();
                    String deletedStr="/external_files";
                    imageUrl=imageUrl.substring(deletedStr.length());
                    imageUrl=abs_path_storage+imageUrl;

                    Log.d("ABS path storage ",imageUrl);



                    images.add(data.getData());
                    imageAdapter.notifyDataSetChanged();
                    service=Client.createServiceWithAuth(ApiService.class,sessionManager);
                    File file = new File(imageUrl);
//                    File file = new File(data.getData().getPath());
//                    RequestBody requestBody = RequestBody.create(MEDIA_TYPE_IMG, file);



                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

                    MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
//                    synchronized (this){
//                    call = service.uploadImage(body);
//                    call.enqueue(new Callback<MyImage>() {
//                        @Override
//                        public void onResponse(Call<MyImage> call, Response<MyImage> response) {
//                            Log.w("Yup upload image","onResponse"+response);
//                            MyImage info=response.body();
//                            String img_message=info.getMessage();
//                            Log.d("Img_message",img_message);
//                            List<String>ids= info.getId();
//                            int count=0;
//S
//                            for (String id: ids){
//                                // get detect image
//                                Log.d("id"+Integer.toString(count++),id);
//                                try  {
//                                    synchronized (this){
//                                        this.wait(100000);
//                                        call_detect = service.getDetectId(id);
//                                        call_detect.enqueue(new Callback<MyDetectImage>() {
//                                            @Override
//                                            public void onResponse(Call<MyDetectImage> call, Response<MyDetectImage> response) {
//                                                MyDetectImage result_detect = response.body();
//                                                String result_id = result_detect.get_id();
//                                                String result_detect_id = result_detect.getDetect_id();
//                                                String result_status = result_detect.getStatus();
//                                                String result_url = result_detect.getUrl();
//
//                                                Log.d("_id", result_id);
//                                                Log.d("detect_id", result_detect_id);
//                                                Log.d("result_status", result_status);
//                                                Log.d("result_url", result_url);
//                                                call_detail_detect=service.getDetailDetect(result_detect_id);
//                                                call_detail_detect.enqueue(new Callback<MyDetectInfo>() {
//                                                    @Override
//                                                    public void onResponse(Call<MyDetectInfo> call, Response<MyDetectInfo> response) {
//                                                            MyDetectInfo detail=response.body();
//                                                            List<List<List<Float>>>boxes=detail.getBoxes();
//                                                            List<String>texts=detail.getTexts();
//                                                            int n=boxes.size();
//                                                            for(int i=0;i<n;i++){
//                                                                for(int j=0;j<boxes.get(i).size();j++){
//                                                                    Float x_coord=boxes.get(i).get(j).get(0);
//                                                                    Float y_coord=boxes.get(i).get(j).get(1);
//                                                                    Log.d("(x,y)=","("+Float.toString(x_coord)+","+Float.toString(y_coord)+")");
//                                                                    // draw bitmap
//
//                                                                }
//                                                            }
//
//                                                    }
//
//                                                    @Override
//                                                    public void onFailure(Call<MyDetectInfo> call, Throwable t) {
//
//                                                    }
//                                                });
//                                            }
//
//                                            @Override
//                                            public void onFailure(Call<MyDetectImage> call, Throwable t) {
//                                                Log.d("detect error", t.toString());
//                                            }
//                                        });
//
//
//                                    }
//
//
//                                    }catch(Exception e){
//                                        e.getStackTrace();
//                                    }
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<MyImage> call, Throwable t) {
//                            Log.d("upload image on failure",t.getMessage());
//                        }
//                    });
//            }
        }}
    }


    private Bitmap drawRectOnBitMap(Bitmap bm, float left, float top, float right, float bottom){
        Bitmap bm1 = bm.copy(bm.getConfig(),true);
        Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(30);
        mPaint.setStyle(Paint.Style.STROKE);
        Canvas canvas = new Canvas(bm1);
        canvas.drawRect(left,top,right,bottom,mPaint);

        return bm1;
    }

    public class Point {

        public float x = 0;
        public float y = 0;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void setPoint(Float x,Float y){
            this.x=x;
            this.y=y;
        }
    }

    private void drawPoly(Canvas canvas, int color, Point[] points) {
        // line at minimum...
        if (points.length < 2) {
            return;
        }

        // paint
        Paint polyPaint = new Paint();
        polyPaint.setColor(color);
        polyPaint.setStyle(Paint.Style.FILL);

        // path
        Path polyPath = new Path();
        polyPath.moveTo(points[0].x, points[0].y);
        int i, len;
        len = points.length;
        for (i = 0; i < len; i++) {
            polyPath.lineTo(points[i].x, points[i].y);
        }
        polyPath.lineTo(points[0].x, points[0].y);

        // draw
        canvas.drawPath(polyPath, polyPaint);
    }


    public Bitmap drawBoundingBox(Bitmap bm,List<List<List<Float>>>boxes){
        Bitmap bm1 = bm.copy(bm.getConfig(),true);
        Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(30);
        mPaint.setStyle(Paint.Style.STROKE);
        Canvas canvas = new Canvas(bm1);

        int n=boxes.size();
        for(int i=0;i<n;i++){
            int m=boxes.get(i).size();
            Point points[]=new Point[m];
            for(int j=0;j<m;j++){
                points[i].setPoint(boxes.get(i).get(j).get(0),boxes.get(i).get(j).get(1));

            }
            drawPoly(canvas,0xFF5555ee,points);

        }
        return bm1;
    }

    public String bitmap2Base64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public Bitmap base642Bitmap(String b64) {
        byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }


}