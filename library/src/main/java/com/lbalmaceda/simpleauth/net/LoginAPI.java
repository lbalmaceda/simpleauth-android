package com.lbalmaceda.simpleauth.net;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by lbalmaceda on 12/12/15.
 */
public interface LoginAPI {
    @POST("oauth/ro")
    Call<EPLoginResponse> emailLogin(@Body EPRequest data);
}