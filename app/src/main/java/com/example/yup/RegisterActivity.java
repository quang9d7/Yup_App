package com.example.yup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.yup.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private TextView sign_in_tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityRegisterBinding.inflate(getLayoutInflater());
        sign_in_tv=binding.tvSignIn;
        setContentView(binding.getRoot());

        sign_in_tv.setOnClickListener(v->{
            Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
        });
    }
}