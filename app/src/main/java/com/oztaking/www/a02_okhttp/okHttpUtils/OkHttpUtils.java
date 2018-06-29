package com.oztaking.www.a02_okhttp.okHttpUtils;

import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;

/**
 * @function:
 * 【参考文章】
 * 【1】OkHttp使用详解：https://www.jianshu.com/p/9aa969dd1b4d
 * 【2】OkHttp使用进阶 译自OkHttp GitHub官方教程：http://www.cnblogs.com/ct2011/p/3997368.html
 *
 */

public class OkHttpUtils {


    /**
     * [1]GET请求
     */
    private void okHttpGet(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        //可以通过Request.Builder设置更多的参数比如： header、 method等

        Request request = new Request.Builder()
                .get()  //get请求；
                .url(url)
                .build();

        Call call = client.newCall(request);
        //[1]同步请求-会阻塞，使用很少；
        call.execute();
//        [2]异步请求-请求是在线程，更新数据需要使用handler等；
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
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);

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
     * [4-1]post 提交表单 使用 FormEncodingBuilder（2.0）FormBody（3.0） 来构建和HTML <form> 标签相同效果的请求体。
     *
     *
     * 用户注册的情况,需要你输入用户名,密码,还有上传头像,这其实就是一个表单,
     * 主要的区别就在于构造不同的RequestBody传递给post方法即可

     * 表单数据的提交需要：compile 'com.squareup.okio:okio:1.11.0'

     * MuiltipartBody,是RequestBody的一个子类,提交表单利用MuiltipartBody来构建一个RequestBody,
     * 下面的代码是发送一个包含用户民、密码、头像的表单到服务端

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

        Request request = new Request.Builder()
                .url(url)
                .post(multipartBody)
                .build();

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
     * @throws Exception
     *
     * 【注意】上面的描述是基于okhttp2.0;代码是基于okhttp3.0；
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
                        RequestBody.create(MEDIA_TYPE_PNG, new File("website/static/logo-square.png")))
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
     * [6]使用Gson来解析JSON响应
     *
     * Gson是一个在JSON和Java对象之间转换非常方便的api。这里我们用Gson来解析Github API的JSON响应。
     * 注意： ResponseBody.charStream() 使用响应头 Content-Type 指定的字符集来解析响应体。默认是UTF-8。
     *
     */
    private void ParseGson(String url){
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
                for (Map.Entry<String,GistFile> entry:gist.files.entrySet()){
                    System.out.println(entry.getKey());
                    System.out.println(entry.getValue().content);
                }
            }
        });




    }

    private static class Gist{
        Map<String,GistFile> files;
    }

    private static class GistFile{
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
                    File file = new File(Environment.getExternalStorageDirectory(), "downLoadFile.png");
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
                    File file = new File(Environment.getExternalStorageDirectory(), "downLoadFile.png");
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
     * @param url 下载连接
     * @param saveDir 储存下载文件的SDCard目录
     * @param params url携带参数
     * @param extraHeaders 请求携带其他的要求的headers
     * @param listener 下载监听
     */
    public void download(final String url, final String saveDir,
                         HashMap<String,String> params, HashMap<String,String> extraHeaders,
                                 final OnDownloadListener listener) {

        OkHttpClient okHttpClient = new OkHttpClient();
        //构造请求Url
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        if (params != null) {
            for (String key : params.keySet()) {
                if (params.get(key)!=null){
                    urlBuilder.setQueryParameter(key, params.get(key));//非必须
                }
            }
        }
        //构造请求request
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .headers(extraHeaders == null ? new Headers.Builder().build() : Headers.of(extraHeaders))//headers非必须
                .get()
                .build();

        //异步执行请求
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 下载失败
                listener.onDownloadFailed();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //非主线程
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                // 储存下载文件的目录
                String savePath = isExistDir(saveDir);
                try {
                    //获取响应的字节流
                    is = response.body().byteStream();
                    //文件的总大小
                    long total = response.body().contentLength();
                    File file = new File(savePath);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    //循环读取输入流
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        // 下载中
                        if(listener != null){
                            listener.onDownloading(progress);
                        }

                    }
                    fos.flush();
                    // 下载完成
                    if(listener != null){
                        listener.onDownloadSuccess();
                    }

                } catch (Exception e) {
                    if(listener != null){
                        listener.onDownloadFailed();
                    }

                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
    }

    private String isExistDir(String saveDir) {
        return null;
    }

    public interface OnDownloadListener{
        public void onDownloadFailed();
        public void onDownloadSuccess();
        public void onDownloading(int progress);
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
//        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"),file);

        //*****此处使用的RequestBody是经过修改；
        RequestBody fileBody = progressRequestBody.create(MediaType.parse("application/octet-stream"), file);

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
                    System.out.println("responseHeaders:" + headers.name(i) + ":" + headers.value(i));
                }
                //打印头部的所有信息；
                System.out.println(response.body().string());
            }
        });
    }


    /**
     * [11] 提取响应头
     *
     * 典型的HTTP头 像是一个 Map<String, String> :每个字段都有一个或没有值。
     * 但是一些头允许多个值，像Guava的Multimap。
     *
     * 例如：HTTP响应里面提供的 Vary 响应头，就是多值的。OkHttp的api试图让这些情况都适用。
     * 【header(name, value)】 可以设置唯一的name、value。
     * 如果已经有值，旧的将被移除，然后添加新的。
     *
     * 【addHeader(name, value)】 可以添加多值（添加，不移除已有的）。
     *
     * 当读取响应头时，使用 header(name) 返回最后出现的name、value。
     * 通常情况这也是唯一的name、value。
     * 如果没有值，那么 header(name) 将返回null。
     * 如果想读取字段对应的所有值，使用 headers(name) 会返回一个list（如上例）。
     * 为了获取所有的Header，Headers类支持按index访问。
     */

    private void addHeaders(String url){
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
     *
     * 提交了一个markdown文档到web服务，以HTML方式渲染markdown。
     * 因为整个请求体都在内存中，因此避免使用此api提交大文档（大于1MB）。
     */

    private void okHttpPostString(String url){
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
     *
     * 以流的方式POST提交请求体。请求体的内容由流写入产生。
     * 这个例子是流直接写入Okio的BufferedSink。
     * 你的程序可能会使用 OutputStream ，你可以使用 BufferedSink.outputStream()来获取
     */

    private void okHttpPostStream(String url){

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
                for (int i=2; i<=997; i++){
                    sink.writeUtf8(String.format(" * %s = %s\n",i,factor(i)));
                }
            }

            private String factor(int n){
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
     * [13]响应缓存
     *
     * 为了缓存响应，你需要一个你可以读写的缓存目录，和缓存大小的限制。
     * 这个缓存目录应该是私有的，不信任的程序应不能读取缓存内容。
     * 一个缓存目录同时拥有多个缓存访问是错误的。
     * 大多数程序只需要调用一次 new OkHttp() ，在第一次调用时
     * 配置好缓存，然后其他地方只需要调用这个实例就可以了。否则两个缓存示例互相干扰，破坏响应缓存，而且有
     * 可能会导致程序崩溃。
     * 响应缓存使用HTTP头作为配置。你可以在请求头中添加 Cache-Control: max-stale=3600 ,OkHttp缓存会支
     * 持。你的服务通过响应头确定响应缓存多长时间，例如使用 Cache-Control: max-age=9600 。
     *
     */











}