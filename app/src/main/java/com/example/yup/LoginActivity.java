package com.example.yup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.widget.TextView;

import com.example.yup.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private TextView sign_up_tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding=ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sign_up_tv = binding.btnSignUp;

        sign_up_tv.setOnClickListener(v -> {
            Intent intent=new Intent(getApplicationContext(),RegisterActivity.class);
            startActivity(intent);
        });
    }
}