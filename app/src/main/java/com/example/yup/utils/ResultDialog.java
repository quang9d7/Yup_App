package com.example.yup.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageButton;

import com.example.yup.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;


public class ResultDialog extends AlertDialog implements View.OnClickListener {
    TextView textView;
    AppCompatImageButton copyBtn, blurBtn;
    MaterialButton backBtn;
    Context context;
    String label;
    List<Point> curBox;
    ImageDrawView parent;

    public ResultDialog(Context context, String label, List<Point> box, ImageDrawView parent) {
        super(context);
        this.context = context;
        this.label = label;
        this.curBox = box;
        this.parent = parent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_result);
        textView = findViewById(R.id.predictLabel);
        textView.setText(label);
        copyBtn = findViewById(R.id.copyBtn);
        blurBtn = findViewById(R.id.blurBtn);
        blurBtn.setOnClickListener(this);
        copyBtn.setOnClickListener(this);
        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.copyBtn) {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Predict Result", textView.getText());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Đã copy nội dung vào clipboard", Toast.LENGTH_SHORT).show();
        } else if (view.getId() == R.id.backBtn) {
            this.dismiss();
        }
        else if (view.getId() == R.id.blurBtn) {
            Path path = new Path();
            Bitmap bm = parent.getCurBitmap().copy(parent.getCurBitmap().getConfig(),true);
            Canvas canvas = new Canvas(bm);
            path.moveTo(curBox.get(0).x, curBox.get(0).y);
            for (int j = 1; j < curBox.size(); j++) {
                path.lineTo(curBox.get(j).x, curBox.get(j).y);
            }
            path.lineTo(curBox.get(0).x, curBox.get(0).y);
            BlurMaskFilter h = new BlurMaskFilter(10, BlurMaskFilter.Blur.INNER);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.BLACK);
            paint.setMaskFilter(h);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(path, paint);
            parent.setImageBitmap(bm);
        }
    }
}
