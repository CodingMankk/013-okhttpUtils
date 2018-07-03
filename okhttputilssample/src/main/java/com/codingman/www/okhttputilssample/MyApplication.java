package com.codingman.www.okhttputilssample;

import android.app.Application;

import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * @function
 */

public class MyApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化设置一个okhttpClient
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)  //读时间不能过短，否则请求读取无法成功；
                .build();

        OkHttpUtils.initClient(okHttpClient);
    }
}
