package com.example.yup.utils;

import com.example.yup.models.InfoMessage;
import com.example.yup.models.MyDetectImage;
import com.example.yup.models.MyImage;
import com.example.yup.models.TokenPair;
import com.example.yup.models.UserAccount;

import java.util.Observable;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface ApiService {

    // dang ky user
    @POST("users")
    @Headers("Content-Type: application/json")
    Call<InfoMessage> register(@Body RequestBody body);

    // dang nhap user ( saui nay nho doi thanh GET("users")
    @POST("auth")
    @Headers("Content-Type: application/json")
    Call<TokenPair> login(@Body RequestBody body);

    @GET("auth")
    Call<TokenPair> refresh(@Header("Authorization") String refreshToken);


    @Multipart
    @POST("detect")
    Call<MyImage> uploadImage(@Part MultipartBody.Part image);



    @GET("images/{id}")
    @Headers("Content-Type: application/json")
    Call<MyDetectImage>getDetectId(@Path("id") String id);


}
