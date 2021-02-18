package com.realsil.api;

import com.lunzn.tool.log.LogUtil;
import com.major.http.api.interceptor.HttpLoggingInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiRetrofit {

//    public static final String BASE_URL = "http://192.168.30.129:9550/public-upgrade/";
//    public static final String BASE_URL = "http://192.168.30.253/public-upgrade/";
    public static final String BASE_URL = "http://lzmx.lunzn.com/public-upgrade/";
//    public static final String BASE_URL = "http://120.236.15.10:8787/public-upgrade/";

    private static final class HOLDER {
        private static final ApiRetrofit sInstance = new ApiRetrofit();
    }

    public static ApiRetrofit getInstance() {
        return HOLDER.sInstance;
    }

    private Retrofit mRetrofit;

    private ApiRetrofit() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                LogUtil.e("http_log", message);
            }
        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build();

        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                // RxJava2CallAdapterFactory
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }

    public <T> T getService(Class<T> clazz) {
        return mRetrofit.create(clazz);
    }
}
