package com.example.yup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yup.databinding.ActivityRegisterBinding;
import com.example.yup.models.ErrorMessage;
import com.example.yup.models.InfoMessage;
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

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private TextView sign_in_tv;

    Button register_btn;


    ApiService service;
    Moshi moshi = new Moshi.Builder().build();
    SessionManager sessionManager;
    Call<InfoMessage> call;

    private String currentUsername = null, currentHash = null, currentEmail = null;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityRegisterBinding.inflate(getLayoutInflater());
        sign_in_tv=binding.tvSignIn;
        register_btn=binding.registerBtn;
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences(getResources().getString(R.string.yup_sp), MODE_PRIVATE);
        sessionManager = SessionManager.getInstance(sharedPreferences);

        service = Client.createService(ApiService.class);

        if (sharedPreferences.contains("CR_UID")) {
            loadPrevInfo();
//            confirmEmail();
        }


        // set onClickListener
        sign_in_tv.setOnClickListener(v->{
            Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
        });

        register_btn.setOnClickListener(v->{
            register();
        });


    }


    private void loadPrevInfo() {
        currentUsername = sharedPreferences.getString("CR_UID", null);
        currentHash = sharedPreferences.getString("CR_HSH", null);
        currentEmail = sharedPreferences.getString("CR_EML", null);
    }

    private void savePrevInfo() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("CR_UID", currentUsername);
        editor.putString("CR_HSH", currentHash);
        editor.putString("CR_EML", currentEmail);
        editor.apply();
    }


    private void removePrevInfo() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        currentEmail = null;
        currentUsername = null;
        currentHash = null;
        editor.remove("CR_UID");
        editor.remove("CR_HSH");
        editor.remove("CR_EML");
        editor.apply();
    }

    private void login(boolean stay) {
//        showLoading();

        UserCredentials userCredentials = new UserCredentials(currentUsername, currentHash);

        String jsonCredentials = moshi.adapter(UserCredentials.class).toJson(userCredentials);

        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), jsonCredentials);

        Call<TokenPair> loginCall;
        loginCall = service.login(requestBody);
        loginCall.enqueue(new Callback<TokenPair>() {
            @Override
            public void onResponse(Call<TokenPair> call, Response<TokenPair> response) {

                Log.w("YellLogin", "onResponse: " + response);

                if (response.isSuccessful()) {
                    sessionManager.saveToken(response.body());
                    sharedPreferences.edit().putString("uid",currentUsername);
                    sharedPreferences.edit().apply();
                    removePrevInfo();
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                } else {
                    if (stay) {
                        Toast.makeText(RegisterActivity.this, "Email chưa được xác thực. Vui lòng thử lại", Toast.LENGTH_LONG).show();
//                        showConfForm();
                    }
                    else {
                        removePrevInfo();
                        gotoLogin();
                        Toast.makeText(RegisterActivity.this, "Email chưa được xác thực. Vui lòng thử đăng nhập lại", Toast.LENGTH_LONG).show();
                    }
                }

            }

            @Override
            public void onFailure(Call<TokenPair> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Lỗi khi kết nối với server", Toast.LENGTH_LONG).show();
//                showForm();
            }
        });
    }

    private void gotoLogin() {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();

    }


    private void register() {
        currentUsername = binding.username.getText().toString();
        String password = binding.password.getText().toString();
        String name = binding.edName.getText().toString();
        currentEmail = binding.edEmail.getText().toString();

        currentHash = Hashing.sha256()
                .hashString(password, StandardCharsets.UTF_8)
                .toString();

        UserCredentials userCredentials = new UserCredentials(currentUsername, currentHash, currentEmail, name);
        Log.w("user info register uid",userCredentials.getUid().toString());
        Log.w("user info register password",userCredentials.getHash().toString());
        Log.w("user info register name",userCredentials.getName().toString());
        Log.w("user info register email",userCredentials.getEmail().toString());
        String jsonCredentials = moshi.adapter(UserCredentials.class).toJson(userCredentials);

        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), jsonCredentials);

//        showLoading();
        try {
            call = service.register(requestBody);
        }
        catch (Exception e)
        {
            Log.e("YellSignup", e.toString());
        }
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {

                Log.w("YellSignup", "onResponse: " + response);

                if (response.isSuccessful()) {
//                    confirmEmail();
                    gotoLogin();
                    Toast.makeText(RegisterActivity.this, "dang ky thanh cong", Toast.LENGTH_LONG).show();
                }
                else {
                    if (response.code() == 401) {
                        ErrorMessage apiError = ErrorMessage.convertErrors(response.errorBody());
                        Toast.makeText(RegisterActivity.this, apiError.getMessage(), Toast.LENGTH_LONG).show();
                    }
//                    showForm();
                }

            }

            @Override
            public void onFailure(Call<InfoMessage> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Lỗi khi kết nối với server", Toast.LENGTH_LONG).show();
//                showForm();
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentUsername != null)
            savePrevInfo();
    }


}