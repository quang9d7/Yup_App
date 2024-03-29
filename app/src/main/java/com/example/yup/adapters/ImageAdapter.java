package com.example.yup.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yup.EditorActivity;
import com.example.yup.R;
import com.example.yup.models.DownloadImage;
import com.example.yup.models.MyDetectImage;
import com.example.yup.utils.ApiService;
import com.example.yup.utils.Client;
import com.example.yup.utils.SessionManager;


import java.io.FileDescriptor;
import java.io.FileNotFoundException;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private ArrayList<Uri> images;
    private Context context;
    private ArrayList<String> image_ids;
    private ApiService service;
    SessionManager sessionManager;
    SharedPreferences sharedPreferences;


    public ImageAdapter(ArrayList<Uri> images, Context context) {
        this.images = images;
        this.context = context;
    }

    public ImageAdapter(ArrayList<String> image_ids, ArrayList<Uri> images, ApiService service, Context context) {
        this.images = images;
        this.context = context;
        this.image_ids = image_ids;
        sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.yup_sp), 0);
        sessionManager = SessionManager.getInstance(sharedPreferences);
        this.service = Client.createServiceWithAuth(ApiService.class, sessionManager);

    }

    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.image_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder holder, int position) {

        Uri uri = this.images.get(position);
        Bitmap myImg = null;
        Log.d("uri is ", uri.toString());
        String image_id = this.image_ids.get(position);
//        Log.d("image_id is",image_id);
        if (uri.toString().startsWith("http")) {

            try {

                DownloadImage downloadImage = new DownloadImage(uri.toString());
                Thread thread = new Thread(downloadImage);
                thread.start();
                thread.join();
                myImg = downloadImage.getBm();
                DisplayMetrics displayMetrics = new DisplayMetrics();
                ((Activity) context).getWindowManager().getDefaultDisplay()
                        .getMetrics(displayMetrics);
                float dpWidth = (displayMetrics.widthPixels / displayMetrics.density - 50) / 2;
                int pxWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpWidth, displayMetrics);

                holder.imageView.getLayoutParams().height = (pxWidth * myImg.getHeight()) / myImg.getWidth();
                holder.imageView.setImageBitmap(scaleBitmapWidth(myImg, pxWidth));
                holder.imageView.requestLayout();
                holder.status.setText("Hoàn thành");
                holder.status.setTextColor(context.getColor(R.color.completeColor));
                holder.statusIcon.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.completeColor)));
                holder.statusIcon.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_stars_24));
            } catch (Exception e) {
                Log.d("bitmap error", "err");
                e.getStackTrace();
                e.printStackTrace();
            }


        } else {
            ParcelFileDescriptor parcelFileDescriptor = null;
            try {
                parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

            myImg = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        }


        if (myImg != null) {
            Log.d("URL not null is", uri.toString());
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay()
                    .getMetrics(displayMetrics);
            float dpWidth = (displayMetrics.widthPixels / displayMetrics.density - 50) / 2;
            int pxWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpWidth, displayMetrics);

            holder.imageView.getLayoutParams().height = (pxWidth * myImg.getHeight()) / myImg.getWidth();
            holder.imageView.setImageBitmap(scaleBitmapWidth(myImg, pxWidth));
            holder.imageView.requestLayout();
        } else {
            Log.d("no_bitmap", "a");
        }


        if (holder.status.getText().toString().equals("Hoàn thành")) {
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, EditorActivity.class);
                    intent.putExtra("image_id", image_id);

                    context.startActivity(intent);
                }
            });

        } else {
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("image_id dang xu ly",image_id);
                    Call<MyDetectImage> call_de = service.getDetectId(image_id);
                    call_de.enqueue(new Callback<MyDetectImage>() {
                        @Override
                        public void onResponse(Call<MyDetectImage> call, Response<MyDetectImage> response) {
                            if (response.isSuccessful()) {

                                String status_now = response.body().getStatus();
                                if (status_now.equals("Done")) {
                                    holder.status.setText("Hoàn thành");
                                    holder.status.setTextColor(context.getColor(R.color.completeColor));
                                    holder.statusIcon.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.completeColor)));
                                    holder.statusIcon.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_stars_24));

                                    Intent intent = new Intent(context, EditorActivity.class);
                                    intent.putExtra("image_id", image_id);
                                    context.startActivity(intent);

                                } else {
                                    Toast.makeText(context, "Hình ảnh đang được xử lý, vui lòng chờ trong giây lát", Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyDetectImage> call, Throwable t) {
                            t.getStackTrace();
                        }
                    });
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView imageView;
        TextView status;
        ImageView statusIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            status = itemView.findViewById(R.id.status);
            statusIcon = itemView.findViewById(R.id.statusIcon);
        }
    }

    public static Bitmap scaleBitmapWidth(Bitmap bitmap, int width) {
        return scaleBitmap(bitmap, width, (width * bitmap.getHeight())
                / bitmap.getWidth());
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int width, int height) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, width, height,
                false);
        return newBitmap;
    }
}
