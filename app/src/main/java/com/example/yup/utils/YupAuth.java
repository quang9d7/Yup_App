package com.example.yup.utils;

import androidx.annotation.Nullable;

import com.example.yup.models.TokenPair;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

public class YupAuth implements Authenticator {

    private SessionManager sessionManager;
    private static YupAuth INSTANCE;

    private YupAuth(SessionManager sessionManager){
        this.sessionManager = sessionManager;
    }

    static synchronized YupAuth getInstance(SessionManager sessionManager){
        if(INSTANCE == null){
            INSTANCE = new YupAuth(sessionManager);
        }
        return INSTANCE;
    }


    @Nullable
    @Override
    public Request authenticate(Route route, Response response) throws IOException {

        if (responseCount(response) >= 3) {
            sessionManager.deleteToken();
            return null;
        }

        TokenPair token = sessionManager.getToken();

        ApiService service = Client.createService(ApiService.class);
        Call<TokenPair> call = service.refresh(token.getRefreshToken());
        retrofit2.Response<TokenPair> res = call.execute();

        if (res.isSuccessful()) {
            TokenPair newToken = res.body();
            sessionManager.saveToken(newToken);

            return response.request().newBuilder().header("Authorization", "Bearer " + res.body().getAccessToken()).build();
        }
        else {
            return null;
        }
    }

    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

}
