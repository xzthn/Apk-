package com.realsil.api;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import rx.Observable;

public interface ApiService {

    /**
     *
     * @param body
     * @return
     */
    @Headers({"Content-Type: application/json","Accept: application/json"})
    @POST("upgrade/check")
    Observable<UpgradeResp> getUpgrade(@Body RequestBody body);



}
