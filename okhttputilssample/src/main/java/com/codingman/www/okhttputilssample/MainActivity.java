package com.codingman.www.okhttputilssample;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;

import okhttp3.Call;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity {

    private TextView mTv;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Logger.init("okHttp-sample");

        mTv = (TextView) findViewById(R.id.tv);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_progressBar);
        mProgressBar.setMax(100);
    }


    /**
     * [1]异步get请求使用
     */
    public void okHttpGet(View view) {
        String url0 = "http://www.imooc.com/";
        String url1 = "http://www.391k.com/api/xapi.ashx/info.json?key=bd_hyrzjjfb4modhj&size=10&page=1";
        OkHttpUtils.get()
                .url(url0)
                .build()
                .execute(new TCallBack());
    }


    /**
     * [2] getfile 文件下载
     */
    public void okHttpDownLoadFile(View view){
        String url = "http://txt.99dushuzu.com/download-txt/3/21068.txt";
        String url1 = "https://github.com/hongyangAndroid/okhttp-utils/blob/master/okhttputils-2_4_1.jar?raw=true";
        OkHttpUtils.get()
                .url(url)
                .build()
                .execute(new FileCallBack(Environment.getExternalStorageDirectory().getAbsolutePath(),
                        "21068.txt") {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Logger.e("onError :" + e.getMessage());
                    }

                    @Override
                    public void onResponse(File response, int id) {
                        Logger.e(response.getAbsolutePath());
                    }

                    @Override
                    public void inProgress(float progress, long total, int id) {
                        mProgressBar.setProgress((int) (progress*100));
                    }
                });
    }







    /**
     * callBack的创建
     */
    private class TCallBack extends StringCallback {

        @Override
        public void onBefore(Request request, int id) {
            setTitle("Loading");
        }

        @Override
        public void onAfter(int id) {
            setTitle("Completed");
        }

        @Override
        public void onError(Call call, Exception e, int id) {
            mTv.setText(e.getMessage());
        }

        @Override
        public void onResponse(String response, int id) {
            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
            //TextView显示response数据；
            mTv.setText(response);
        }

        //文件下载时进度使用；
        @Override
        public void inProgress(float progress, long total, int id) {
            mProgressBar.setProgress((int) (progress*100));
        }
    }

}
