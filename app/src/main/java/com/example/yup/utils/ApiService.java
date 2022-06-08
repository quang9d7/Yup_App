package com.example.yup.utils;

import com.example.yup.models.InfoMessage;
import com.example.yup.models.TokenPair;
import com.example.yup.models.UserAccount;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // dang ky user
    @POST("users")
    @Headers("Content-Type: application/json")
    Call<InfoMessage> register(@Body RequestBody body);

    // dang nhap user ( saui nay nho doi thanh GET("users")
    @GET("users")
    @Headers("Content-Type: application/json")
    Call<TokenPair> login(@Body RequestBody body);

    @GET("auth")
    Call<TokenPair> refresh(@Header("Authorization") String refreshToken);

    @GET("users")
    Call<UserAccount> getUserProfile(@Query("fetch") String fetch);

//    @GET("users")
//    Call<UserAccountFull> getUserFull(@Query("fetch") String fetch);

    // update user
    @PATCH("users")
    @Headers("Content-Type: application/json")
    Call<InfoMessage> updateUser(@Body RequestBody body);




    // dang xuat
    @DELETE("auth")
    Call<InfoMessage> logout();

    @POST("users/verify")
    @Headers("Content-Type: application/json")
    Call<InfoMessage> verify(@Body RequestBody body);

    @POST("users/verify/resend")
    @Headers("Content-Type: application/json")
    Call<InfoMessage> resendVerification(@Body RequestBody body);

}
