package com.example.yup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class EditorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Intent intent = getIntent();
        String detect_id = intent.getStringExtra("detect_id");
        Log.d("detect_id in temp Activity", detect_id);
        TextView textView = findViewById(R.id.temp_text);
        textView.setText(detect_id.toString());
    }
}
