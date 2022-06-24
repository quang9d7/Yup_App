package com.example.yup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.yup.R;

public class TempActivity extends AppCompatActivity {

    TextView tx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);
        Intent intent=getIntent();
        String detect_id=intent.getStringExtra("detect_id");
        Log.d("detect_id in temp Activity",detect_id);
        tx=findViewById(R.id.temp_text);
        tx.setText(detect_id.toString());
    }
}