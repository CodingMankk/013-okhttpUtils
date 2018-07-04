package com.codingman.www.okhttputilssample;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity {

    private TextView mTv;
    private ProgressBar mProgressBar;
    private ImageView mIv;

    private String mBaseUrl = "http://192.168.0.12:8080/okhttpDemo/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Logger.init("okHttp-sample");

        mTv = (TextView) findViewById(R.id.tv);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_progressBar);
        mProgressBar.setMax(100);

        mIv = (ImageView)findViewById(R.id.iv);
    }


    /**
     * [1]异步get请求使用
     */
    public void okHttpGet(View view) {
        String url0 = "http://www.imooc.com/";
        String url1 = "http://www.391k.com/api/xapi.ashx/info" +
                ".json?key=bd_hyrzjjfb4modhj&size=10&page=1";
        OkHttpUtils.get()
//                .url(url0)
                .url(mBaseUrl+"login?username=OzTaking&password=8888") //本地服务器请求
                .build()
                .execute(new TCallBack());
    }


    /**
     * [2] getfile 文件下载
     *
     * 【说明】
     * 【1】在android6.0 手机找不到文件；
     * 【2】在android 4.4 经测试：下载到了 /storage/sdcard1/21068.txt目录下
     * 【3】在模拟器上android 8.0 需要动态申请权限，会报以下错误：
     *        /storage/emulated/0 (Permission denied) 权限已开，写入sd卡仍报错的解决办法
     *  [模拟器解决办法]打开虚拟机的Setting–>Apps–>找到你的应用–>点击Permissions–>将需要的权限手动打开，问题解决。
     */
    public void okHttpDownLoadFile(View view) {
        String url = "http://txt.99dushuzu.com/download-txt/3/21068.txt";
        String mBaseUrl1 = "http://192.168.0.12:8080/1.txt"; //本地tomcat服务器文件；
        String mBaseUrl2 = "http://192.168.0.12:8080/1.jpg"; //本地tomcat服务器图片；
        String mBaseUrl3 = "http://down.360safe.com/360Root/360RootSetup.exe"; //网络文件；
        String mBaseUrl4 = "https://qqrjtu.xy1758.com/mirrors/win10/8060/windows_10_ultimate_x64_2018.iso"; //网络文件；大文件

        OkHttpUtils.get()
                .url(mBaseUrl4)
//                .url(mBaseUrl3)
//                .url(mBaseUrl2)
//                .url(mBaseUrl1)
//                .url(url)
                .build()
//                .execute(new FileCallBack(Environment.getExternalStorageDirectory()
//                        .getAbsolutePath(),
//                        "okHttpDownLoadFile-测试文件.txt")
                        .execute(new FileCallBack(Environment.getExternalStorageDirectory()
                                .getAbsolutePath(),
                                "windows_10_ultimate_x64_2018.iso")
                        {
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
                        mProgressBar.setProgress((int) (progress * 100));
                    }
                });
    }

    /**
     * [3] getImage下载并加载
     */
    public void okHttpGetImage(View view){
        mTv.setText("");
        String url ="http://b.hiphotos.baidu.com/image/pic/item/0ff41bd5ad6eddc4f8daa30935dbb6fd52663306.jpg";
        String mBaseUrlPic1 = "http://192.168.0.12:8080/1.jpg"; //本地tomcat服务器图片；
        String mBaseUrlPic2 = "http://192.168.0.12:8080/2.jpg"; //本地tomcat服务器图片；
        String mBaseUrlPic3 = "http://192.168.0.12:8080/3.jpg"; //本地tomcat服务器图片；

        OkHttpUtils.get()
//                .url(url)
                .url(mBaseUrlPic3)
//                .url(mBaseUrlPic2)
//                .url(mBaseUrlPic1)
                .tag(this)
                .build()
                .connTimeOut(2000)  //默认是10s，单位MILLISECONDS
                .readTimeOut(2000)
                .writeTimeOut(2000)
                .execute(new BitmapCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        mTv.setText(e.getMessage());
                    }

                    @Override
                    public void onResponse(Bitmap response, int id) {
                        mIv.setImageBitmap(response);
                        mTv.setText("Bitmap load succeed...");
                    }

                    @Override
                    public void onBefore(Request request, int id) {
                        setTitle("Loading");
                    }

                    @Override
                    public void onAfter(int id) {
                        setTitle("Completed");
                    }

                });
    }


    /**
     * [4]postJson
     *  将数据转化为json然后post到服务器；
     */
    public void okHttpPostString(View view){
        String url = mBaseUrl + "postString";
        OkHttpUtils.postString()
                .url(url)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .content(new Gson().toJson(new User("OzTaking","12345")))
                .build()
                .execute(new TCallBack());
    }

    /**
     * [5]postFile
     * fileName0:post一张图片
     * fileName1:osot一个exe文件；
     */

    public void okHttpPostImg(View view){

        String fileName0="1.jeg";
        String fileName1="360RootSetup.exe";

        mProgressBar.setProgress(0);

        String url = mBaseUrl +"postFile";

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName0);
        File file2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName1);
        Logger.d(Environment.getExternalStorageDirectory());
        if (!file2.exists()){
            Toast.makeText(getApplicationContext(),"PostImg文件不存在",Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        OkHttpUtils.postFile()
                .url(url)
                .file(file2)
                .build()
                .execute(new TCallBack());
    }


    /**
     * [6]Post表单形式上传文件:
     * [header+params+文件]
     * [注意]服务器端代码不知道该怎么写？？？
     */

    public void postFormHeaderParamsFile(View view){
        String url = mBaseUrl + "okHttpPostString";

        String FileName = "360RootSetup.exe";
        File file = new File(Environment.getExternalStorageDirectory(), FileName);
        if (!file.exists()){
            Toast.makeText(getApplicationContext(),"postForm文件不存在",Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        //params
        Map<String, String> params = new HashMap<>();
        params.put("username","OzTaking");
        params.put("password","88888888");

        //headers
        Map<String, String> headers = new HashMap<>();
        headers.put("APP-Key","APP-Secret888");
        headers.put("APP-Secret","APP-Secret666");

        OkHttpUtils.post()
                .addFile("androidFile",FileName,file)
                .url(url)
                .params(params)
                .headers(headers)
                .build()
                .execute(new TCallBack());
    }


    /**
     * [7]https的验证
     *对于Https
     *依然是通过配置即可，框架中提供了一个类HttpsUtils
     *
     *设置可访问所有的https网站
     *HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);
     *OkHttpClient okHttpClient = new OkHttpClient.Builder()
     *        .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
     *         //其他配置
     *         .build();
     *OkHttpUtils.initClient(okHttpClient);

     *设置具体的证书
     *HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(证书的inputstream, null, null);
     *OkHttpClient okHttpClient = new OkHttpClient.Builder()
     *        .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager))
     *         //其他配置
     *         .build();
     *OkHttpUtils.initClient(okHttpClient);

     *双向认证
     *HttpsUtils.getSslSocketFactory(
     *	证书的inputstream,
     *	本地证书的inputstream,
     *	本地证书的密码)
     *同样的，框架中只是提供了几个实现类，你可以自行实现SSLSocketFactory，传入sslSocketFactory即可
     */

    public void getHttpsHtml(View view){
        String url = "http://www.12306.cn/mormhweb/";
        String url1 = "https://kyfw.12306.cn/otn/";
        OkHttpUtils
                .get()
                .url(url1)
                .build()
                .connTimeOut(2000)
                .readTimeOut(2000)
                .writeTimeOut(2000)
                .execute(new TCallBack());

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

        //文件get/post时进度都会显示-某些情况下，因为速度比较快，进度条显示一闪而过；
        @Override
        public void inProgress(float progress, long total, int id) {
           mProgressBar.setProgress((int) (progress * 100));
        }
    }

}
