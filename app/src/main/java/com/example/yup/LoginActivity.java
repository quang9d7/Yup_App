package com.example.yup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yup.databinding.ActivityLoginBinding;
import com.example.yup.models.ErrorMessage;
import com.example.yup.models.TokenPair;
import com.example.yup.models.UserCredentials;
import com.example.yup.utils.ApiService;
import com.example.yup.utils.Client;
import com.example.yup.utils.SessionManager;
import com.google.common.hash.Hashing;
import com.squareup.moshi.Moshi;

import java.nio.charset.StandardCharsets;


import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {


    // declare binding
    ActivityLoginBinding binding;
    TextView sign_up_tv;
    Button login_btn;



    // service
    ApiService service;
    SessionManager sessionManager;
    Call<TokenPair> call;
    Moshi moshi = new Moshi.Builder().build();
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get binding components
        sign_up_tv = binding.btnSignUp;
        login_btn=binding.btnLogin;

        service = Client.createService(ApiService.class);



        sharedPreferences = getSharedPreferences(getResources().getString(R.string.yup_sp), MODE_PRIVATE);
        sessionManager = SessionManager.getInstance(sharedPreferences);
        // if exists registration in progress
        if (sharedPreferences.contains("CR_UID")) {
            goToRegister();

        }

        // Neu da dang nhap
        if (sessionManager.getToken().getAccessToken() != null) {
            Log.w("da dang nhap","OK");
            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
            finish();
        }


        // onClick listener
        sign_up_tv.setOnClickListener(v -> {
            goToRegister();
        });

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

     void goToRegister() {
         Intent intent=new Intent(getApplicationContext(),RegisterActivity.class);
         startActivity(intent);
         finish();
    }

    void login(){


//            showLoading();

            String username = binding.username.getText().toString();
            String password = binding.password.getText().toString();

            String hash = Hashing.sha256()
                    .hashString(password, StandardCharsets.UTF_8)
                    .toString();

            UserCredentials userCredentials = new UserCredentials(username, hash);

            String jsonCredentials = moshi.adapter(UserCredentials.class).toJson(userCredentials);

            RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), jsonCredentials);

            call = service.login(requestBody);
            call.enqueue(new Callback<TokenPair>() {
                @Override
                public void onResponse(Call<TokenPair> call, Response<TokenPair> response) {

                    Log.w("YupLogin", "onResponse: " + response);

                    if (response.isSuccessful()) {
                        sessionManager.saveToken(response.body());
                        sharedPreferences.edit().putString("uid", username);
                        sharedPreferences.edit().apply();
                        startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                        finish();
                    } else {
                        if (response.code() == 401) {
                            ErrorMessage apiError = ErrorMessage.convertErrors(response.errorBody());
                            Toast.makeText(LoginActivity.this, apiError.getMessage(), Toast.LENGTH_LONG).show();
                        }
//                        showForm();
                    }

                }

                @Override
                public void onFailure(Call<TokenPair> call, Throwable t) {
                    Log.w("YellLogin", "onFailure: " + t.getMessage());
//                    showForm();
                }
            });
    }


}