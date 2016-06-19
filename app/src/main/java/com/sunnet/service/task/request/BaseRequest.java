package com.sunnet.service.task.request;

import com.sunnet.service.log.Log;
import com.sunnet.service.util.ConfigApi;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public abstract class BaseRequest {

    protected OkHttpClient okHttpClient;
    protected Retrofit restAdapter;
    private boolean isHostUpload = false;

    public void execute() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.readTimeout(ConfigApi.TIME_OUT, TimeUnit.SECONDS);
        builder.writeTimeout(ConfigApi.TIME_OUT, TimeUnit.SECONDS);
        builder.networkInterceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request().newBuilder()
                        .addHeader("x-track-app-key", ConfigApi.HEADER_API).build();

                return chain.proceed(request);
            }
        });

        okHttpClient = builder.addInterceptor(interceptor).build();

        String url_host = ConfigApi.URL_HOST;
        if (isHostUpload)
            url_host = ConfigApi.URL_HOST_UPLOAD;

        restAdapter = new Retrofit.Builder()
                .baseUrl(url_host)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        Log.i("URL: " + url_host + getUrl());
        Log.i("Param: " + getStringSender());
    }

    protected abstract String getUrl();

    protected abstract String getStringSender();

    public void setHostUpload(boolean hostUpload) {
        isHostUpload = hostUpload;
    }
}
