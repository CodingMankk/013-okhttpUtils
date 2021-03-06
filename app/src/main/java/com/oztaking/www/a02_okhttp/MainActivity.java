package com.oztaking.www.a02_okhttp;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.oztaking.www.a02_okhttp.okHttpUtils.progressRequestBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;

public class MainActivity extends AppCompatActivity {

    private static final String url1 = "\"https://github.com/hongyangAndroid\"";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Logger.init("MainActivity");

//        okHttpCancelCall();
        okHttpTimeout();


    }


    /**
     * [1]GET请求
     */
    private void okHttpGet(String url) throws IOException {
        //1,创建okHttpClient对象
        OkHttpClient client = new OkHttpClient();

        HashMap<String, String> urlParams = new HashMap<>();
        HashMap<String, String> urlHeaders = new HashMap<>();

        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        if (urlParams != null) {
            for (String key : urlParams.keySet()) {
                urlBuilder.setQueryParameter(key, urlParams.get(key));
            }
        }

        //2,创建一个Request
        //可以通过Request.Builder设置更多的参数比如： header、 method等
        Request request = new Request.Builder()
//              .url(url)
                .url(urlBuilder.build())
                .headers(urlHeaders == null ? new Headers.Builder().build() : Headers.of(urlHeaders))
                .get()  //get请求；
                .build();

        //3,新建一个call对象
        Call call = client.newCall(request);
        //[4-1]同步请求-会阻塞，使用很少；
        call.execute();
        //[4-2]异步请求-请求是在线程，更新数据需要使用handler等；
        //请求加入调度，这里是异步Get请求回调
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //可以判断response返回的状态：
                if (response.isSuccessful()) {
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
                }

            }
        });
    }

    /**
     * [2]post 提交键值对
     */
    private void okHttpPostWithParam(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();

        /*okhttp2 的写法；
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("username", "CodingMankk");*/

        //okhttp3的写法
        FormBody formBody = new FormBody.Builder()
                .add("username", "admin")
                .add("password", "admin")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Call call = okHttpClient.newCall(request);

        //[1]同步请求
        /*try {
            call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        //[2]异步请求
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //请求失败的回调
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //请求成功的回调；
                //                response 是请求到的数据；
            }
        });
    }


    /**
     * [3]post http文件上传
     * 最主要的还是创建合适的RequestBody
     */
    private void okHttpPostFile(String url) {

        OkHttpClient okHttpClient = new OkHttpClient();

        //读取外部的sd卡的文件上传；
        File file = new File(Environment.getExternalStorageDirectory(), "mimi.mp4");
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"),
                file);

        Request request = new Request.Builder()
                .url(url)
                .post(fileBody)
                .build();

        Call call = okHttpClient.newCall(request);

        //[1]同步请求；
       /* try {
            call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        //[2]异步请求
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    /**
     * [多个文件的上传]
     */

    private void postMultiFile(String url){

        HashMap<String, String> urlHeaders = new HashMap<>();

        OkHttpClient client = new OkHttpClient();

        // form 表单形式上传,MultipartBody的内容类型是表单格式，multipart/form-data
        MultipartBody.Builder multiPartBodyBuilder= new MultipartBody
                .Builder()
                .setType(MultipartBody.FORM);

        //参数
        HashMap<String,String> params = new HashMap<>();
        if (params != null) {
            for (String key : params.keySet()) {
                if (params.get(key)!=null){
                    //增加一系列的参数；
                    multiPartBodyBuilder.addFormDataPart(key, params.get(key));
                }
            }
        }

        //需要上传的文件，需要携带上传的文件（小型文件 不建议超过500K）
        HashMap<String,String> files= new HashMap<>();
        if (files != null) {
            for (String key : files.keySet()) {
                //重点：RequestBody create(MediaType contentType, final File file)构造文件请求体RequestBody
                RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), files.get(key));
                multiPartBodyBuilder.addFormDataPart(key, files.get(key).getClass().getName(),requestBody);
            }
        }

        //构造请求request
        Request request = new Request.Builder()
                .headers(urlHeaders == null ? new Headers.Builder().build() : Headers.of(urlHeaders))
                .url(url)
                .post(multiPartBodyBuilder.build())
                .build();

        //异步执行请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //非主线程
                if (response.isSuccessful()) {
                    String str = response.body().string();
                    Log.i("tk", response.message() + " , body " + str);

                } else {
                    Log.i("tk" ,response.message() + " error : body " + response.body().string());
                }
            }
        });
    }






    /**
     * [4-1]post 提交表单 使用 FormEncodingBuilder（2.0）FormBody（3.0） 来构建和HTML <form> 标签相同效果的请求体。
     * <p>
     * <p>
     * 用户注册的情况,需要你输入用户名,密码,还有上传头像,这其实就是一个表单,
     * 主要的区别就在于构造不同的RequestBody传递给post方法即可
     * <p>
     * 表单数据的提交需要：compile 'com.squareup.okio:okio:1.11.0'
     * <p>
     * MuiltipartBody,是RequestBody的一个子类,提交表单利用MuiltipartBody来构建一个RequestBody,
     * 下面的代码是发送一个包含用户民、密码、头像的表单到服务端
     * <p>
     * 【注意】
     * 【1】如果提交的是表单,一定要设置setType(MultipartBody.FORM)这一句
     * 【2】 addFormDataPart("avatar","avatar.png",requestBodyFile)
     * 参数1：类似于键值对的键,是供服务端使用的,就类似于网页表单里面的name属性，<input type="file" name="myfile">
     * 参数2：本地文件的名字
     * 参数3：第三个参数是RequestBody,里面包含了我们要上传的文件的路径以及MidiaType
     * 【3】本地sd卡的读写权限的添加：<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
     */

    private void okHttpPostFormData(String url) {
        //构造图片文件;
        File file = new File(Environment.getExternalStorageDirectory(), "avatar.png");
        if (!file.exists()) {
            Log.d("okHttpPostFormData", "文件不存在");
            return;
        }

        RequestBody requestBodyFile =
                RequestBody.create(MediaType.parse("application/octet-stream"), file);

        MultipartBody multipartBody = new MultipartBody.Builder()
                //一定要设置这句
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", "admin")
                .addFormDataPart("password", "admin")
                .addFormDataPart("avatar", "avatar.png", requestBodyFile)
                .build();

        //POST参数构造MultipartBody.Builder，表单提交
        HashMap<String, String> params = new HashMap<>();
        MultipartBody.Builder MultipartBodyBuilder  = new MultipartBody.Builder()
                                              .setType(MultipartBody.FORM);
        if (params != null) {
            for (String key : params.keySet()) {
                if (params.get(key) != null) {
                    MultipartBodyBuilder.addFormDataPart(key, params.get(key));
                }
            }
        }

        // 构造Request->call->执行
        Request request = new Request.Builder()
                .url(url)
                .post(MultipartBodyBuilder.build())//参数放在body体里
                .build();


//        Request request = new Request.Builder()
//                .url(url)
//                .post(multipartBody)
//                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        Call call = okHttpClient.newCall(request);
        //[1]同步请求；
       /* try {
            call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        //[2]异步请求
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });

    }


    /**
     * [4-2] Post方式提交分块请求
     * MultipartBuilder 可以构建复杂的请求体，与HTML文件上传形式兼容。
     * 多块请求体中每块请求都是一个请求体，可以定义自己的请求头。
     * 这些请求头可以用来描述这块请求，例如他的 Content-Disposition 。
     * 如果 Content-Length 和 Content-Type 可用的话，他们会被自动添加到请求头中
     *
     * @param url
     * @throws Exception 【注意】上面的描述是基于okhttp2.0;代码是基于okhttp3.0；
     */
    private void okHttpPostBlockData(String url) throws Exception {

        final String IMGUR_CLIENT_ID = "...";
        final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
        OkHttpClient client = new OkHttpClient();

        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType((MultipartBody.FORM))
                .addPart(Headers.of("Content-Disposition", "form-data; name=\"title\""),
                        RequestBody.create(null, "Square Logo"))
                .addPart(Headers.of("Content-Disposition", "form-data; name=\"image\""),
                        RequestBody.create(MEDIA_TYPE_PNG, new File("website/static/logo-square" +
                                ".png")))
                .build();

        Request request = new Request.Builder()
                .header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                .url("")
                .post(multipartBody)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        System.out.println(response.body().string());
    }


    /**
     * [5]post json 请求；
     */
    private void okHttpPostJSon(String url, String json) throws IOException {

        OkHttpClient okHttpClient = new OkHttpClient();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, json);

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        Call call = okHttpClient.newCall(request);
        //[1]同步请求;
        call.execute();
        //[2]异步请求;
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });

    }

    /**
     * [6]大文件上传
     *
     * 服务端的接口：
     *
     * url：domain/sync/img/upload
     * method: POST
     * //请求参数
     * data = {
     *  'img_md5': 'dddddsds',
     *  'total': 10,  #总的分片数
     *  'index': 5,   #该分片所在的位置, start by 1
     *  }
     * 请求返回值json:
     * {
     *  'status': 206/205/400/409/500,
     *  'msg': '分片上传成功/上传图片成功/参数错误/上传数据重复/上传失败'
     *  'data': { # 205时有此字段
     *  'img_url': 'https://foo.jpg',
     *  }
     * }
     *
     * 只需要图片的md5,总的分片数，该分片的位置，当一块传输成功时返回206，当全部块
     * 传完成是返回206，并返回该图片在服务器的url
     *
     */

    private static class okHttpBigFileUpLoad{
        //[1]文件路径
        String path = "xxx.jpg";

        public static final int FILE_BLOCK_SIZE = 500 * 1024;//500k

        /**
         * [2]文件块对象
         * 文件块描述
         * */
        public static class FileBlock {
            public long start;//起始字节位置
            public long end;//结束字节位置
            public int index;//文件分块索引
        }

        //[3]计算切块,存储在数组
        final SparseArray<FileBlock> blockArray = splitFile(path, FILE_BLOCK_SIZE);
        /**
         * 文件分块
         *
         * @param filePath 文件路径
         * @param blockSize 块大小
         *
         * @return 分块描述集合 文件不存在时返回空
         */
        public static SparseArray<FileBlock> splitFile(String filePath, long blockSize) {
            File file = new File(filePath);
            if (!file.exists()) {
                return null;
            }

            SparseArray<FileBlock> blockArray = new SparseArray<>();
            int i = 0;
            int start = 0;
            while (start < file.length()) {
                i++;
                FileBlock fileBlock = new FileBlock();
                fileBlock.index = i;
                fileBlock.start = start;
                start += blockSize;
                fileBlock.end = start;
                blockArray.put(i, fileBlock);
            }
            blockArray.get(i).end = file.length();
            return blockArray;
        }

        /**
         * 对文件块分块多线程异步上传
         */





    }




    /**
     * [6]使用Gson来解析JSON响应
     * <p>
     * Gson是一个在JSON和Java对象之间转换非常方便的api。这里我们用Gson来解析Github API的JSON响应。
     * 注意： ResponseBody.charStream() 使用响应头 Content-Type 指定的字符集来解析响应体。默认是UTF-8。
     */
    private void ParseGson(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();

        final Gson gson = new Gson();

        //默认是get请求；
        final Request request = new Request.Builder()
                .url("https://api.github.com/gists/c2a7c39532239ff261be")
                .build();

        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);
                Gist gist = gson.fromJson(response.body().charStream(), Gist.class);
                for (Map.Entry<String, GistFile> entry : gist.files.entrySet()) {
                    System.out.println(entry.getKey());
                    System.out.println(entry.getValue().content);
                }
            }
        });


    }

    private static class Gist {
        Map<String, GistFile> files;
    }

    private static class GistFile {
        String content;
    }


    /**
     * [6] get请求文件下载
     * 此处的实例是下载图片
     */
    private void okHttpGetDownLoadFile(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .get()
                .url(url)
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //拿到图片的字节流,然后保存为了本地的一张图片
                if (response.isSuccessful()) {
                    //获取数据流；
                    InputStream is = response.body().byteStream();

                    //读到文件中;
                    int len = 0;
                    File file = new File(Environment.getExternalStorageDirectory(), "downLoadFile" +
                            ".png");
                    FileOutputStream fos = new FileOutputStream(file);

                    byte[] buf = new byte[1024];
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                    fos.close();
                    is.close();
                }


           /*   直接将图片设置给了ImagView；
                final Bitmap bitmap = BitmapFactory.decodeStream(is);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                    }
                });
                is.close();*/
            }
        });


    }

    /**
     * [7] 给文件的下载加上进度条
     * 进度的获取是在回调函数onResponse()中去获取
     * (1)使用response.body().contentLength()拿到文件总大小
     * (2)在while循环中每次递增我们读取的buf的长度
     */

    private void okHttpDownLoadWithProgress(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .get()
                .url(url)
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //拿到图片的字节流,然后保存为了本地的一张图片
                if (response.isSuccessful()) {
                    //获取数据流；
                    InputStream is = response.body().byteStream();

                    //每次下载的实时更新的进度；
                    long sum = 0;
                    //下载的文件总长度；
                    final long total = response.body().contentLength();

                    //读到文件中;
                    int len = 0;
                    File file = new File(Environment.getExternalStorageDirectory(), "downLoadFile" +
                            ".png");
                    FileOutputStream fos = new FileOutputStream(file);

                    byte[] buf = new byte[1024];
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);

                        sum += len;
                        final long finalSum = sum;
                        Log.d("okHttp", "下载进度：" + finalSum + "/" + total);

                        //将进度设置到主线程显示
                       /* runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //将进度设置到TextView中
                                contentTv.setText(finalSum + "/" + total);
                            }
                        });*/

                    }
                    fos.flush();
                    fos.close();
                    is.close();
                }
            }

        });

    }


    /**
     * [8]文件的上传增加进度
     * 对于上传的进度的处理会比较麻烦,因为具体的上传过程是在RequestBody中由OkHttp帮我们处理上传,
     * 而且OkHttp并没有给我们提供上传进度的接口,这里做法是自定义类继承RequestBody,
     * 然后重写其中的方法,将其中的上传进度通过接口回调暴露出来供使用
     * 详见：包下的progressRequestBody类；
     * 在原有的RequestBody上包装了一层,最后在我们的使用中在post()方法中传入我们的CountingRequestBody对象即可
     * <p>
     * 最主要的还是创建合适的RequestBody
     */
    private void okHttpPostFileWithProgress(String url) {

        OkHttpClient okHttpClient = new OkHttpClient();

        //读取外部的sd卡的文件上传；
        File file = new File(Environment.getExternalStorageDirectory(), "mimi.mp4");
        //        RequestBody fileBody = RequestBody.create(MediaType.parse
        // ("application/octet-stream"),file);

        //*****此处使用的RequestBody是经过修改；
        RequestBody fileBody = progressRequestBody.create(MediaType.parse
                ("application/octet-stream"), file);

        Request request = new Request.Builder()
                .url(url)
                .post(fileBody)
                .build();

        Call call = okHttpClient.newCall(request);

        //[1]同步请求；
       /* try {
            call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        //[2]异步请求
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    /**
     * [9] 同步get
     * 下载一个文件，打印他的响应头，以string形式打印响应体。
     * 响应体的 string() 方法对于小文档来说十分方便、高效。但是如果响应体太大（超过1MB），应避免适应
     * string() 方法 ，因为他会将把整个文档加载到内存中。
     * 对于超过1MB的响应body，应使用流的方式来处理body。
     */
    private void okHttpGetSyncHeader(String url) throws IOException {

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = okHttpClient.newCall(request);
        //同步请求；
        Response response = call.execute();
        if (!response.isSuccessful()) {
            throw new IOException("response error" + response);
        }
        //获取到response的头部；
        Headers responseHeaders = response.headers();
        int len = responseHeaders.size();
        for (int i = 0; i < len; i++) {
            //response的头部的每个信息
            System.out.println(responseHeaders.name(i) + ":" + responseHeaders.value(i));
        }
        //打印头部的所有信息；
        System.out.println(response.body().string());
    }


    /**
     * [10]异步get
     *
     * @param url
     */
    private void okHttpGetASyncHeader(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                //获取到response的头部；
                Headers headers = response.headers();
                int len = headers.size();
                for (int i = 0; i < len; i++) {
                    //response的头部的每个信息
                    System.out.println("responseHeaders:" + headers.name(i) + ":" + headers.value
                            (i));
                }
                //打印头部的所有信息；
                System.out.println(response.body().string());
            }
        });
    }


    /**
     * [11] 提取响应头
     * 典型的HTTP头 像是一个 Map<String, String> :每个字段都有一个或没有值。
     * 但是一些头允许多个值，像Guava的Multimap。
     * 例如：HTTP响应里面提供的 Vary 响应头，就是多值的。OkHttp的api试图让这些情况都适用。
     * 【header(name, value)】 可以设置唯一的name、value。
     * 如果已经有值，旧的将被移除，然后添加新的。
     * 【addHeader(name, value)】 可以添加多值（添加，不移除已有的）。
     * 当读取响应头时，使用 header(name) 返回最后出现的name、value。
     * 通常情况这也是唯一的name、value。
     * 如果没有值，那么 header(name) 将返回null。
     * 如果想读取字段对应的所有值，使用 headers(name) 会返回一个list（如上例）。
     * 为了获取所有的Header，Headers类支持按index访问。
     */

    private void addHeaders(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "OkHttp Headers.java")
                .addHeader("Accept", "application/json:q=0.5")
                .addHeader("Accept", "application/vnd.github.v3+json")
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(response.header("Server"));
                System.out.println(response.header("Date"));
                System.out.println(response.headers("Vary"));
            }
        });
    }

    /**
     * [11]post 提交String
     * 提交了一个markdown文档到web服务，以HTML方式渲染markdown。
     * 因为整个请求体都在内存中，因此避免使用此api提交大文档（大于1MB）。
     */

    private void okHttpPostString(String url) {
        MediaType mediaType = MediaType.parse("text/x-markdown; charset=utf-8");
        OkHttpClient okHttpClient = new OkHttpClient();
        String postBody = ""
                + "Releases\n"
                + "--------\n"
                + "\n"
                + " * _1.0_ May 6, 2013\n"
                + " * _1.1_ June 15, 2013\n"
                + " * _1.2_ August 11, 2013\n";

        //创建请求体
        RequestBody requestBody = RequestBody.create(mediaType, postBody);

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);
                System.out.println(response.body().string());
            }
        });

    }

    /**
     * [12]post 方式提交流
     * <p>
     * 以流的方式POST提交请求体。请求体的内容由流写入产生。
     * 这个例子是流直接写入Okio的BufferedSink。
     * 你的程序可能会使用 OutputStream ，你可以使用 BufferedSink.outputStream()来获取
     */

    private void okHttpPostStream(String url) {

        final MediaType mediaType = MediaType.parse("text/x-markdown; charset=utf-8");

        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody requestBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                /**
                 * 关键的方法：sink.writeUtf8
                 */
                sink.writeUtf8("Numbers\n");
                for (int i = 2; i <= 997; i++) {
                    sink.writeUtf8(String.format(" * %s = %s\n", i, factor(i)));
                }
            }

            private String factor(int n) {
                for (int i = 2; i < n; i++) {
                    int x = n / i;
                    if (x * i == n) return factor(x) + " × " + i;
                }
                return Integer.toString(n);
            }
        };

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                System.out.println(response.body().string());
            }
        });
    }


    /**
     * [13]响应缓存----没有弄明白；
     * 【注意】只支持get请求的缓存；
     * <p>
     * 缓存响应：需要建立可以读写的缓存目录和限定缓存大小。
     * 建立的缓存目录应该是私有的，不信任的程序应不能读取缓存内容。
     * 一个缓存目录不能同时拥有多个缓存访问。
     * 大多数程序只需要调用一次 new OkHttp() ，在第一次调用时配置好缓存，然后其他使用到的地方只需要调用这个实例。
     * 否则两个缓存示例互相干扰，破坏响应缓存，而且有可能会导致程序崩溃。
     * 响应缓存使用HTTP头作为配置。
     * 你可以在请求头中添加 Cache-Control: max-stale=3600 ,OkHttp缓存会支持。
     * 你的服务通过响应头确定响应缓存多长时间，例如使用 Cache-Control: max-age=9600
     */

    private void okHttCacheResponse(File cacheDirector) {

        //使用OkHttp的Cache，首先需要指定缓存的路径和大小
        int cacheSize = 10 * 1024 * 1024;
        Cache cache = new Cache(cacheDirector, cacheSize);

        OkHttpClient okHttpClient
                = new OkHttpClient()
                .newBuilder()
                .cache(cache)
                .build();

        Request request = new Request.Builder()
                .url("http://publicobject.com/helloworld.txt")
                .build();

        Call call1 = okHttpClient.newCall(request);
        call1.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response1) throws IOException {
                if (!response1.isSuccessful())
                    throw new IOException("Unexpected code " + response1);

                String s = response1.body().string();
                Logger.d("response1:" + s);
                Logger.d("response1 Cache response:" + response1.cacheResponse());
                Logger.d("response1 network response:" + response1.networkResponse());
            }
        });

        Call call2 = okHttpClient.newCall(request);
        call2.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response2) throws IOException {
                if (!response2.isSuccessful())
                    throw new IOException("Unexpected code " + response2);

                String s = response2.body().string();
                Logger.d("response2:" + s);
                Logger.d("response2 Cache response:" + response2.cacheResponse());
                Logger.d("response2 network response:" + response2.networkResponse());
            }
        });

//        System.out.println("Response 2 equals Response 1? " +
//                response2.equals(response1));
    }

    /**
     * [14]取消一个Call--没有弄明白；
     * <p>
     * 使用 Call.cancel() 可以立即停止掉一个正在执行的call。
     * 如果一个线程正在写请求或者读响应，将会引发IOException 。
     * 当call没有必要的时候，使用这个api可以节约网络资源。
     * 例如当用户离开一个应用时。不管同步还是异步的call都可以取消。
     * 你可以通过tags来同时取消多个请求。
     * 当你构建一请求时，使用 RequestBuilder.tag(tag) 来分配一个标签。
     * 之后你就可以用 OkHttpClient.cancel(tag) 来取消所有带有这个tag的call
     */
    private void okHttpCancelCall() {

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://httpbin.org/delay/2")// This URL is served with a 2 second delay.
                .tag(1)
                .build();

        final long nanoTime = System.nanoTime();
        final Call call = okHttpClient.newCall(request);

        // Schedule a job to cancel the call in 1 second.
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                System.out.printf("%.2f Canceling +++++ call.%n", (System.nanoTime() - nanoTime) / 1e9f);
                call.cancel();
                System.out.printf("%.2f Canceled ------call.%n", (System.nanoTime() - nanoTime) / 1e9f);
            }
        }, 1, TimeUnit.SECONDS);

        System.out.printf("%.2f Canceling #### call.%n", (System.nanoTime() - nanoTime) / 1e9f);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.printf("%.2f Call was  $$$$$ bexpected to fail, but completed: %s%n",
                        (System.nanoTime() - nanoTime) / 1e9f, response);
            }
        });


    }

    /**
     * [15]超时
     * 没有响应时使用超时结束call。
     * 没有响应的原因可能是客户点链接问题、服务器可用性问题或者这之间的其他东西。
     * OkHttp支持连接，读取和写入超时。
     */

    private void okHttpTimeout() {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(1, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.SECONDS)
                .pingInterval(10, TimeUnit.SECONDS) //websocket 轮训间隔
                .build();

        Request request = new Request.Builder()
                .url("http://httpbin.org/delay/2") // This URL is served with a 2 second delay.
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("Response completed: " + response.body().string());
            }
        });
    }

    /**
     * [16]每个call的配置--clone
     * 使用 OkHttpClient ，所有的HTTP Client配置包括代理设置、超时设置、缓存设置。当你需要为单个call改变
     * 配置的时候，clone 一个 OkHttpClient 。这个api将会返回一个浅拷贝（shallow copy），你可以用来单独自
     * 定义。下面的例子中，我们让一个请求是500ms的超时、另一个是3000ms的超时。
     */


  /*  public void run() throws Exception {
        final OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://httpbin.org/delay/1") // This URL is served with a 1 second delay.
                .build();
        try {
            Response response = client.clone() // Clone to make a customized OkHttp for this
            request..setReadTimeout(500, TimeUnit.MILLISECONDS)
                    .newCall(request)
                    .execute();
            System.out.println("Response 1 succeeded: " + response);
        } catch (IOException e) {
            System.out.println("Response 1 failed: " + e);
        }
        try {
            Response response = client.clone() // Clone to make a customized OkHttp for this
            request..setReadTimeout(3000, TimeUnit.MILLISECONDS)
                    .newCall(request)
                    .execute();
            System.out.println("Response 2 succeeded: " + response);
        } catch (IOException e) {
            System.out.println("Response 2 failed: " + e);
        }
    }*/
    private void OkHttpCallConfigure() {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://httpbin.org/delay/1") // This URL is served with a 1 second delay
                .build();

        Call call = okHttpClient.newCall(request);
        Call clone = call.clone();

    }

    /**
     * [17]Cookie 保存
     * Request经常都要携带Cookie，上面说过request创建时可以通过header设置参数，Cookie也是参数之一
     * 然后可以从返回的response里得到新的Cookie，可能得想办法把Cookie保存起来。
     * 但是OkHttp可以不用我们管理Cookie，自动携带，保存和更新Cookie。
     * 方法是在创建OkHttpClient设置管理Cookie的CookieJar
     */
    private void OkHttpCookieSave(String url) {

        final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        // 保存cookie通常使用SharedPreferences
                        cookieStore.put(url.host(), cookies);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        // 从保存位置读取，注意此处不能为空，否则会导致空指针
                        List<Cookie> cookies = cookieStore.get(url.host());
                        return cookies != null ? cookies : new ArrayList<Cookie>();

                    }
                })
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Cookie", "xxx") //添加cookie；
                .build();
    }


    /**
     * [21]添加拦截器
     * okHttp3的实现使用的是链式的拦截器，同时也开放了自定义拦截器接口
     */

    private void okHttpAddInterceptor() {
        new OkHttpClient.Builder()
                // 此种拦截器将会在请求开始的时候调用
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        return null;
                    }
                })
                // 连接成功，读写数据前调用
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        return null;
                    }
                }).build();
    }

    /**
     * [22]https的支持添加
     * okhttp3完全支持https，设置证书即可
     * 未完成，报错；
     */
    private void okHttpHttps() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .sslSocketFactory(SSLSocketFactory(), X509TrustManager)
                .build();
    }


    /**
     * [23]Websocket支持
     * okhttp3支持websocket,简易推送，轮训都可以使用
     * websocket协议首先会发起http请求，握手成功后，转换协议保持长连接，类似心跳
     * <p>
     * 【说明】创建一个OkHttpClient，通常是单例，
     * 如果要自定义一些属性那就要用内部的Builder来构造
     * <p>
     * [参考文章]https://blog.csdn.net/xlh1191860939/article/details/75452342/
     */

    private void okHttpWebSocket(String url) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        okHttpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                // 连接成功
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                // 当收到文本消息
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
                // 收到字节消息，可转换为文本
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);

            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                // 连接被关闭
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                // 连接失败
            }
        });
    }


}
