package com.example.yup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.example.yup.adapters.ImageAdapter;
import com.example.yup.models.MyDetectImage;
import com.example.yup.models.MyDetectInfo;
import com.example.yup.models.MyImage;
import com.example.yup.models.RealmImage;
import com.example.yup.models.UserInfo;
import com.example.yup.utils.ApiService;
import com.example.yup.utils.Client;
import com.example.yup.utils.SessionManager;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {


    ArrayList<Uri> images = new ArrayList<>();
    ArrayList<String>detect_ids=new ArrayList<>();
    RecyclerView imageCollection;
    FloatingActionButton pickImageButton;
    ImageAdapter imageAdapter;
    AppCompatImageButton logout_btn, loadImage_btn;
    BottomAppBar bottomAppBar;
    NestedScrollView galleryScrollView;
    LinearLayoutCompat pickImageContextMenu;
    ExtendedFloatingActionButton pickImgFromStorageBtn, takeImageBtn;
    String abs_path_storage = "storage/emulated/0";
    ApiService service;
    Call<MyImage> call;
    private static final int READ_PERMISSION = 101;
    SessionManager sessionManager;
    SharedPreferences sharedPreferences;
    Realm backgroundThreadRealm;


    private static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_temp);

        Realm.init(this);

        RealmConfiguration config = new RealmConfiguration.Builder()
                .allowWritesOnUiThread(true)
                .build();
        backgroundThreadRealm = Realm.getInstance(config);


        Realm.getInstanceAsync(config, new Realm.Callback() {
            @Override
            public void onSuccess(Realm realm) {
                Log.v(
                        "EXAMPLE",
                        "Successfully opened a realm with reads and writes allowed on the UI thread."
                );
                realm.executeTransaction(transactionRealm -> {
                    RealmQuery<RealmImage> query = transactionRealm.where(RealmImage.class);
                    RealmResults<RealmImage> results = query.findAll();
                    Log.d("result_Image", results
                            .toString());
                });
            }
        });


        pickImageButton = findViewById(R.id.fab);
        logout_btn = findViewById(R.id.logout_btn);
        loadImage_btn = findViewById(R.id.loadImage_btn);
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
                if (dy > 0 && bottomAppBar.getFabAlignmentMode() == BottomAppBar.FAB_ALIGNMENT_MODE_CENTER) {
                    bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
                } else if (dy < 0 && bottomAppBar.getFabAlignmentMode() == BottomAppBar.FAB_ALIGNMENT_MODE_END) {
                    bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
                }
            }
        });

        imageAdapter = new ImageAdapter(detect_ids,images, this);
        imageCollection.setAdapter(imageAdapter);

        sharedPreferences = getSharedPreferences(getResources().getString(R.string.yup_sp), MODE_PRIVATE);
        sessionManager = SessionManager.getInstance(sharedPreferences);
        service = Client.createService(ApiService.class);
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);

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
        loadImage_btn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {

                loadUserStorage();

            }
        });

        pickImgFromStorageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select images"), 1);
                pickImageButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_image_24));
                pickImageButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.fabButtonColor)));
                pickImageContextMenu.setVisibility(View.INVISIBLE);
            }
        });

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionManager.deleteToken();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    public void loadUserStorage() {

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
        Call<UserInfo> call = service.getUserInfo();
        call.enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful()) {
                    List<String> ids = response.body().getImages();
                    for (String id : ids) {
                        Call<MyDetectImage> call_detect = service.getDetectId(id);
                        call_detect.enqueue(new Callback<MyDetectImage>() {
                            @Override
                            public void onResponse(Call<MyDetectImage> call, Response<MyDetectImage> res) {

                                if (res.isSuccessful()) {
                                    MyDetectImage myDetectImage = res.body();
                                    String id_detect = myDetectImage.getDetect_id();
                                    String url = myDetectImage.getUrl();

                                    RealmImage obj = new RealmImage(id, id_detect, url, "Done");
                                    backgroundThreadRealm.executeTransactionAsync(transactionRealm -> {
                                        transactionRealm.insert(obj);
                                    });
                                    images.add(Uri.parse(url));
                                    detect_ids.add(id_detect);
                                    imageAdapter.notifyDataSetChanged();

                                } else {
                                    Log.d("Error while getting access to ", id);
                                }

                            }

                            @Override
                            public void onFailure(Call<MyDetectImage> call, Throwable t) {
                                t.printStackTrace();
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {
                int x = data.getClipData().getItemCount();
                for (int i = 0; i < x; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    images.add(uri);
                    imageAdapter.notifyDataSetChanged();
                }

            } else if (data.getData() != null) {

                String imageUrl = data.getData().getPath();
                String deletedStr = "/external_files";
                imageUrl = imageUrl.substring(deletedStr.length());
                imageUrl = abs_path_storage + imageUrl;

                Log.d("ABS path storage ", imageUrl);


                images.add(data.getData());
                imageAdapter.notifyDataSetChanged();
                File file = new File(imageUrl);


                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

                MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
                synchronized (this) {
                    call = service.uploadImage(body);
                    call.enqueue(new Callback<MyImage>() {
                        @Override
                        public void onResponse(Call<MyImage> call, Response<MyImage> response) {
                            Log.w("Yup upload image", "onResponse" + response);
                            MyImage info = response.body();
                            List<String> ids = new ArrayList<>();
                            if (response.isSuccessful()) {
                                ids = info.getId();
                            }

                            int count = 0;

                            for (String id : ids) {
                                // get detect image
                                Log.d("id" + Integer.toString(count++), id);
                            }
                        }


                        @Override
                        public void onFailure(Call<MyImage> call, Throwable t) {
                            Log.d("upload image on failure", t.getMessage());
                        }
                    });
                }
            }
        }
    }


    private Bitmap drawRectOnBitMap(Bitmap bm, float left, float top, float right, float bottom) {
        Bitmap bm1 = bm.copy(bm.getConfig(), true);
        Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(30);
        mPaint.setStyle(Paint.Style.STROKE);
        Canvas canvas = new Canvas(bm1);
        canvas.drawRect(left, top, right, bottom, mPaint);

        return bm1;
    }


    public class Point {

        public float x = 0;
        public float y = 0;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void setPoint(Float x, Float y) {
            this.x = x;
            this.y = y;
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


    public Bitmap drawBoundingBox(Bitmap bm, List<List<List<Float>>> boxes) {
        Bitmap bm1 = bm.copy(bm.getConfig(), true);
        Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(30);
        mPaint.setStyle(Paint.Style.STROKE);
        Canvas canvas = new Canvas(bm1);

        int n = boxes.size();
        for (int i = 0; i < n; i++) {
            int m = boxes.get(i).size();
            Point points[] = new Point[m];
            for (int j = 0; j < m; j++) {
                points[i].setPoint(boxes.get(i).get(j).get(0), boxes.get(i).get(j).get(1));

            }
            drawPoly(canvas, 0xFF5555ee, points);

        }
        return bm1;
    }

    public String bitmap2Base64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public Bitmap base642Bitmap(String b64) {
        byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }


}