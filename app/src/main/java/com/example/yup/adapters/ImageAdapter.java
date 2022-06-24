package com.example.yup.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yup.R;
import com.example.yup.TempActivity;
import com.example.yup.models.DownloadImage;
import com.squareup.picasso.Picasso;


import java.io.FileDescriptor;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private ArrayList<Uri> images;
    private Context context;
    private ArrayList<String> detect_ids;


    public ImageAdapter(ArrayList<Uri> images, Context context) {
        this.images = images;
        this.context = context;
    }

    public ImageAdapter(ArrayList<String> detect_ids, ArrayList<Uri> images, Context context) {
        this.images = images;
        this.context = context;
        this.detect_ids = detect_ids;
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
        String detect_id = this.detect_ids.get(position);
        Bitmap myImg = null;
        Log.d("uri is ", uri.toString());
        if (uri.toString().startsWith("http")) {
            try {

                DownloadImage downloadImage = new DownloadImage(uri.toString());
                Thread thread = new Thread(downloadImage);
                thread.start();
                ;
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
                holder.status.setText("Done");
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

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TempActivity.class);
                intent.putExtra("detect_id", detect_id);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView imageView;
        TextView status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            status = itemView.findViewById(R.id.status);
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
