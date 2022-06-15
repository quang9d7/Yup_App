package com.example.yup.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yup.R;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private ArrayList<Uri> images;
    private WindowManager windowManager;

    public ImageAdapter(ArrayList<Uri> images, WindowManager windowManager) {
        this.images = images;
        this.windowManager = windowManager;
    }
    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.image_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder holder, int position) {
        Uri uri = this.images.get(position);
        Bitmap myImg = BitmapFactory.decodeFile(uri.getPath());
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay()
                .getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        holder.imageView.setImageBitmap(scaleBitmapWidth(myImg,width));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
        }
    }
    public static Bitmap scaleBitmapWidth(Bitmap bitmap, int width) {
        float ratio = bitmap.getWidth() / bitmap.getHeight();
        return scaleBitmap(bitmap, width, (width * bitmap.getHeight())
                / bitmap.getWidth());
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int width, int height) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, width, height,
                false);
        return newBitmap;
    }
}
