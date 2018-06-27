package com.oztaking.www.a02_okhttp;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import com.orhanobut.logger.Logger;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import okio.BufferedSource;

public class MainActivity extends AppCompatActivity {

    private static final String url1 = "\"https://github.com/hongyangAndroid\"";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Logger.init("MainActivity");





    }




    /**
     * [1]GET请求；execute：同步请求；
     */
    private Response okHttpGet(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .url(url)
                .build();
        Response response = null;

        Call call = client.newCall(request);
        //同步请求；
        call.execute();
        //异步请求
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//
//            }
//        });

            if (response.isSuccessful()){
                //response.body返回数据；
                //[1]
                String string = response.body().string();
//                [2]
                byte[] bytes = response.body().bytes();
//                [3]
                InputStream inputStream = response.body().byteStream();
//                [4]
                Reader reader = response.body().charStream();
//                [5]
                BufferedSource source = response.body().source();
                return response;
            }else{
                throw new IOException("Unexpected code "+request);

            }
    }

    /**
     * [2]post json 请求；execute：同步请求；
     */
    private String okHttpPostJSon(String url,String json) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()){
            return response.body().string();
        }else {
            throw  new IOException("Unexcepted code" + response);
        }

    }

    /**
     * [3]post 提交键值对
     */
    private String post(String url,String json) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody formBody = new FormEncodingBuilder()
                .add("platform", "android")
                .add("name", "bug")
                .add("subject", "xxxxxxxxx")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Response response = okHttpClient.newCall(request).execute();

        if (response.isSuccessful()){
            return response.body().string();
        }else{
            throw new IOException("Unexcepted code:" + response);
        }
    }

    /**
     * [4]post http文件上传
     */

    private void postFile(){
        File file = new File(Environment.getExternalStorageDirectory(), "mimi.mp4");
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"),
                file);

        new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addPart(Headers.of("Content-Disposition","form-data;name=\"usernam\""),
                        RequestBody.create(null,"小明"))
                .addPart()



    }



}
