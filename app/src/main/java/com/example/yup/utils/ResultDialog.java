package com.example.yup.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageButton;

import com.example.yup.R;
import com.google.android.material.button.MaterialButton;


public class ResultDialog extends AlertDialog implements View.OnClickListener {
    TextView textView;
    AppCompatImageButton copyBtn, blurBtn;
    MaterialButton backBtn;
    Context context;
    String label;

    public ResultDialog(Context context, String label) {
        super(context);
        this.context = context;
        this.label = label;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_result);
        textView = findViewById(R.id.predictLabel);
        textView.setText(label);
        copyBtn = findViewById(R.id.copyBtn);
        blurBtn = findViewById(R.id.blurBtn);
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
    }
}
