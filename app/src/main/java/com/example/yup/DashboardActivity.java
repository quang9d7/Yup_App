package com.example.yup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    ArrayList<String>image_ids=new ArrayList<>();
    RecyclerView imageCollection;
    FloatingActionButton pickImageButton;
    ImageAdapter imageAdapter;
    BottomAppBar bottomAppBar;
    NestedScrollView galleryScrollView;
    LinearLayoutCompat pickImageContextMenu;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    TextView fullNameTextview;
    ProgressBar progressBar;
    ExtendedFloatingActionButton pickImgFromStorageBtn, takeImageBtn;
    ApiService service;
    Call<MyImage> call;
    private static final int READ_PERMISSION = 101;
    SessionManager sessionManager;
    SharedPreferences sharedPreferences;
    Realm backgroundThreadRealm;
    Uri tempImageUri;


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
        bottomAppBar = findViewById(R.id.bottom_app_bar);
        galleryScrollView = findViewById(R.id.galleryScrollView);
        pickImageContextMenu = findViewById(R.id.pickImageContextMenu);
        pickImgFromStorageBtn = findViewById(R.id.pickImageFromStorageBtn);
        takeImageBtn = findViewById(R.id.takeImageBtn);
        progressBar = findViewById(R.id.progressBar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        fullNameTextview = header.findViewById(R.id.fullname);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_signout) {
                    sessionManager.deleteToken();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_sync) {
                    loadUserStorage();
                    return true;
                }
                return false;
            }
        });

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

        imageAdapter = new ImageAdapter(image_ids,images,service, this);
        imageCollection.setAdapter(imageAdapter);

        sharedPreferences = getSharedPreferences(getResources().getString(R.string.yup_sp), MODE_PRIVATE);
        sessionManager = SessionManager.getInstance(sharedPreferences);
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

        loadUserStorage();

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

        takeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri);

                startActivityForResult(intent, 2);
            }
        });
        bottomAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout.isOpen()) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                }
                else {
                    drawerLayout.openDrawer(Gravity.LEFT);
                }
            }
        });
    }

    void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        imageCollection.setVisibility(View.VISIBLE);
    }

    void showProgressBar() {
        imageCollection.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void loadUserStorage() {

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
        Call<UserInfo> call = service.getUserInfo();
        showProgressBar();
        call.enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful()) {
                    List<String> ids = response.body().getImages();
                    fullNameTextview.setText(response.body().getName());
                    int lastItem = ids.size() - 1;
                    for (String id : ids) {
                        Call<MyDetectImage> call_detect = service.getDetectId(id);
                        call_detect.enqueue(new Callback<MyDetectImage>() {
                            @Override
                            public void onResponse(Call<MyDetectImage> call, Response<MyDetectImage> res) {

                                if (res.isSuccessful()) {
                                    MyDetectImage myDetectImage = res.body();
                                    String id_image = myDetectImage.get_id();
                                    String url = myDetectImage.getUrl();

                                    RealmImage obj = new RealmImage(id, id_image, url, "Done");
                                    backgroundThreadRealm.executeTransactionAsync(transactionRealm -> {
                                        transactionRealm.insert(obj);
                                    });
                                    images.add(Uri.parse(url));
                                    image_ids.add(id_image);
                                    imageAdapter.notifyDataSetChanged();
                                    if (ids.indexOf(id) == lastItem) {
                                        hideProgressBar();
                                    }
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

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        tempImageUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName()+".provider", image);
        return image;
    }

    private String getRealPathFromURI(Uri contentUri, Context context) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(context, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();
        String result = cursor.getString(columnIndex);
        cursor.close();
        return result;
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
                    // insert null to reserve the corresponding images
                    image_ids.add(null);
                    imageAdapter.notifyDataSetChanged();
                }

            } else if (data.getData() != null) {
                ContentResolver cr = getContentResolver();
                Uri imageUri = data.getData();
                JSONObject jsonObject = new JSONObject();
                try {
                    Bitmap bitmap = android.provider.MediaStore.Images.Media
                            .getBitmap(cr, imageUri);
                    images.add(imageUri);
                    // insert null to reserve the corresponding images
             //       image_ids.add(null);
//                    imageAdapter.notifyDataSetChanged();
                    jsonObject.put("base64", bitmap2Base64(bitmap));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                RequestBody body = RequestBody.create(MediaType.parse("text/plain"), jsonObject.toString());
                synchronized (this) {
                    call = service.uploadImage(body);
                    call.enqueue(new Callback<MyImage>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onResponse(Call<MyImage> call, Response<MyImage> response) {
                            Log.w("Yup upload image", "onResponse" + response);
                            if(response.isSuccessful()){
                                MyImage info = response.body();
                                List<String> ids = info.getId();
                                for (String id : ids) {
                                    // get detect image
                                    Log.d("image_id is",id);
                                    image_ids.add(id);
                                    imageAdapter.notifyDataSetChanged();

                                }

                            }

                        }
                        @Override
                        public void onFailure(Call<MyImage> call, Throwable t) {
                            Log.d("upload image on failure", t.getMessage());
                        }
                    });
                }
            }
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            getContentResolver().notifyChange(tempImageUri, null);
            ContentResolver cr = getContentResolver();
            Bitmap bitmap;
            try {
                bitmap = android.provider.MediaStore.Images.Media
                        .getBitmap(cr, tempImageUri);
                images.add(tempImageUri);
                // insert null to reserve the corresponding images
                image_ids.add(null);
                imageAdapter.notifyDataSetChanged();

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("base64", bitmap2Base64(bitmap));
                RequestBody body = RequestBody.create(MediaType.parse("text/plain"), jsonObject.toString());
                synchronized (this) {
                    call = service.uploadImage(body);
                    call.enqueue(new Callback<MyImage>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onResponse(Call<MyImage> call, Response<MyImage> response) {
                            Log.w("Yup upload image", "onResponse" + response);

                            MyImage info = response.body();
                            List<String> ids = info.getId();
                            for (String id : ids) {
                                // get detect image
                                Log.d("image_id is",id);
                                image_ids.add(id);
                                imageAdapter.notifyDataSetChanged();

                            }
                        }


                        @Override
                        public void onFailure(Call<MyImage> call, Throwable t) {
                            Log.d("upload image on failure", t.getMessage());
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("Camera", e.toString());
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