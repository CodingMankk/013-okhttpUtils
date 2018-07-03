package com.oztaking.www.a02_okhttp.okhttp_others;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * @function 对okhttp的封装，是基于2.4.0 封装；
 * 参考文章：https://blog.csdn.net/lmj623565791/article/details/47911083
 * <p>
 * compile 'com.squareup.okhttp:okhttp:2.4.0'
 * compile 'com.squareup.okio:okio:1.5.0'
 */

public class OkHttpClientManager {

    private static OkHttpClientManager mInstance;
    private OkHttpClient mOkHttpClient;
    private Handler mDelivery;
    private Gson mGson;
    private static final String TAG = "OkHttpClientManager";

    private OkHttpClientManager() {
        mOkHttpClient = new OkHttpClient(); //建立OkHttpClient类；
        /*
        【1】使能cookie enabled
          三种请求：
          ACCEPT_ALL ：accepts all cookies
          ACCEPT_NONE：accepts no cookies
          ACCEPT_ORIGINAL_SERVER ：only accepts cookies from original server
         */
        mOkHttpClient.setCookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER));
        mDelivery = new Handler(Looper.getMainLooper()); //对子线程的的网路请求；
        mGson = new Gson(); //Gson对象；
    }

    /**
     * 【2】单例
     *
     * @return
     */
    public static OkHttpClientManager getInstance() {
        if (mInstance == null) {
            synchronized (OkHttpClientManager.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpClientManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 同步Get请求
     * <p>
     * 说明：okhttp默认是get请求。
     *
     * @param url
     * @return Response
     */
    private Response _getAsyn(String url) throws IOException {

        final Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = mOkHttpClient.newCall(request);
        Response execute = call.execute();
        return execute;
    }

    /**
     * 同步的Get请求
     *
     * @param url
     * @return 字符串
     * <p>
     * /直接调用了返回原始response方法，然后解析为string；
     */
    private String _getAsString(String url) throws IOException {
        Response execute = _getAsyn(url);
        return execute.body().string();
    }

    /**
     * 异步get请求
     *
     * @param url
     * @param callback 封转之后的callback
     */
    private void _getAsyn(String url, final ResultCallback callback) {
        final Request request = new Request.Builder()
                .url(url)
                .build();
        deliveryResult(callback, request); //封装了异步请求；
    }

    /**
     * 同步的Post请求
     *
     * @param url
     * @param params post的参数
     * @return 返回原始响应数据
     */
    private Response _post(String url, Param... params) throws IOException {
        Request request = buildPostRequest(url, params); //对post的Request的封装；
        Response response = mOkHttpClient.newCall(request).execute();
        return response;  //返回原始响应数据
    }

    /**
     * 同步的Post请求--返回String
     *
     * @param url
     * @param params post的参数
     * @return 字符串
     */
    private String _postAsString(String url, Param... params) throws IOException {
        Response response = _post(url, params); //调用上一个post请求，只是返回的参数进行了String提取；
        return response.body().string(); //返回String
    }

    /**
     * 异步post请求封装
     *
     * @param url
     * @param callback
     * @param params
     */
    private void _postAsyn(String url, final ResultCallback callback, Param... params) {
        Request request = buildPostRequest(url, params);
        deliveryResult(callback, request);
    }

    /**
     * 异步的post请求
     *
     * @param url
     * @param callback
     * @param params
     */
    private void _postAsyn(String url, final ResultCallback callback, Map<String, String> params) {
        Param[] paramsArr = map2Params(params);//params如果是map形式，则转化为数组；
        Request request = buildPostRequest(url, paramsArr);
        deliveryResult(callback, request);
    }

    /**
     * post同步文件上传
     *
     * @param params
     * @return
     */
    //file[]+fileKey[]+params
    private Response _post(String url, File[] files, String[] fileKeys, Param... params) throws IOException {
        Request request = buildMultipartFormRequest(url, files, fileKeys, params);
        return mOkHttpClient.newCall(request).execute();
    }

    //file+fileKey
    private Response _post(String url, File file, String fileKey) throws IOException {
        Request request = buildMultipartFormRequest(url, new File[]{file}, new String[]{fileKey}, null);
        return mOkHttpClient.newCall(request).execute();
    }

    //file+fileKey+params
    private Response _post(String url, File file, String fileKey, Param... params) throws IOException {
        Request request = buildMultipartFormRequest(url, new File[]{file}, new String[]{fileKey}, params);
        return mOkHttpClient.newCall(request).execute();
    }

    /**
     * 异步post文件上传
     *
     * @param url
     * @param callback
     * @param files
     * @param fileKeys
     * @throws IOException
     */
    private void _postAsyn(String url, ResultCallback callback, File[] files, String[] fileKeys, Param... params) throws IOException {
        Request request = buildMultipartFormRequest(url, files, fileKeys, params);
        deliveryResult(callback, request);
    }

    /**
     * 异步post文件上传，单文件不带参数上传
     *
     * @param url
     * @param callback
     * @param file
     * @param fileKey
     * @throws IOException
     */
    private void _postAsyn(String url, ResultCallback callback, File file, String fileKey) throws IOException {
        Request request = buildMultipartFormRequest(url, new File[]{file}, new String[]{fileKey}, null);
        deliveryResult(callback, request);
    }

    /**
     * 异步基于post的文件上传，单文件且携带其他form参数上传
     *
     * @param url
     * @param callback
     * @param file
     * @param fileKey
     * @param params
     * @throws IOException
     */
    private void _postAsyn(String url, ResultCallback callback, File file, String fileKey, Param... params) throws IOException {
        Request request = buildMultipartFormRequest(url, new File[]{file}, new String[]{fileKey}, params);
        deliveryResult(callback, request);
    }


    /**
     * 异步下载文件
     *
     * @param url         下载文件地址
     * @param destFileDir 下载文件保存路径
     * @param callback
     */

    private void _downloadAsyn(final String url, final String destFileDir, final ResultCallback callback) {
        final Request request = new Request.Builder()
                .url(url)
                .build();

        final Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(final Request request, final IOException e) {
                sendFailedStringCallback(request, e, callback);
            }

            @Override
            public void onResponse(Response response) {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream(); //获取字节流数据；
                    File file = new File(destFileDir, getFileName(url));
                    fos = new FileOutputStream(file);
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                    //如果下载文件成功，第一个参数为文件的绝对路径
                    sendSuccessResultCallback(file.getAbsolutePath(), callback);
                } catch (IOException e) {
                    sendFailedStringCallback(response.request(), e, callback);
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

    //一般的文件下载路径：
    //  http://down.360safe.com/360Root/3 6 0 R o o t S e t u p . e x e
    //  0                               32                            47
    //  separatorIndex = 31; path.length=47;
    // 32--47 len = 15; 32-47开始；
    private String getFileName(String path) {
        int separatorIndex = path.lastIndexOf("/"); //获取最后一个"/"的索引值；
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
    }

    /**
     * 下载图片并加载图片
     *
     * @param view       ：设置图片的ImageView
     * @param url        :下载的图片的地址；
     * @param errorResId ：下载失败之后的该ImageView显示的默认图片
     * @throws IOException
     */
    private void _displayImage(final ImageView view, final String url, final int errorResId) {
        final Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                setErrorResId(view, errorResId);
            }

            @Override
            public void onResponse(Response response) {
                InputStream is = null;
                try {
                    is = response.body().byteStream(); //reponse 字节流，第一次请求stream是为了计算图片的inSampleSize
                    ImageUtils.ImageSize actualImageSize = ImageUtils.getImageSize(is);
                    ImageUtils.ImageSize imageViewSize = ImageUtils.getImageViewSize(view);
                    //计算bitmpa的inSample比例
                    int inSampleSize = ImageUtils.calculateInSampleSize(actualImageSize, imageViewSize);
                    try {
                        is.reset();
                    } catch (IOException e) {
                        response = _getAsyn(url);
                        is = response.body().byteStream(); //reponse 字节流，第2次请求stream为了加载图片；
                    }

                    BitmapFactory.Options ops = new BitmapFactory.Options();
                    ops.inJustDecodeBounds = false; //计算结束之后设置为false，在ImageUtils时设置为了true；
                    ops.inSampleSize = inSampleSize;
                    final Bitmap bm = BitmapFactory.decodeStream(is, null, ops);
                    //网络请求为耗时操作，必须放在子线程操作，更新ui要放在主线程；
                    mDelivery.post(new Runnable() {
                        @Override
                        public void run() {
                            view.setImageBitmap(bm);
                        }
                    });
                } catch (Exception e) {
                    setErrorResId(view, errorResId);
                } finally {
                    if (is != null) try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void setErrorResId(final ImageView view, final int errorResId) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                view.setImageResource(errorResId);
            }
        });
    }

    //*************对外公布的方法************
    //[1]同步Get请求-返回原始response
    public static Response getAsyn(String url) throws IOException {
        return getInstance()._getAsyn(url);
    }
    //[2]同步Get请求-返回String
    public static String getAsString(String url) throws IOException {
        return getInstance()._getAsString(url);
    }
    //[3]异步Get请求-返回原始请求
    public static void getAsyn(String url, ResultCallback callback) {
        getInstance()._getAsyn(url, callback);
    }
    //[4]同步Post请求--返回原始response
    public static Response post(String url, Param... params) throws IOException {
        return getInstance()._post(url, params);
    }
    //[5]同步Post请求--返回string
    public static String postAsString(String url, Param... params) throws IOException {
        return getInstance()._postAsString(url, params);
    }

    //[6]异步post请求封装
    public static void postAsyn(String url, final ResultCallback callback, Param... params) {
        getInstance()._postAsyn(url, callback, params);
    }
    //[7]异步post请求封装,参数的形式是map
    public static void postAsyn(String url, final ResultCallback callback, Map<String, String> params) {
        getInstance()._postAsyn(url, callback, params);
    }
    //[8]post同步文件上传-file[]+fileKey[]+params
    public static Response post(String url, File[] files, String[] fileKeys, Param... params) throws IOException {
        return getInstance()._post(url, files, fileKeys, params);
    }
    //[9]file+fileKey
    public static Response post(String url, File file, String fileKey) throws IOException {
        return getInstance()._post(url, file, fileKey);
    }
    //[10]file+fileKey+params
    public static Response post(String url, File file, String fileKey, Param... params) throws IOException {
        return getInstance()._post(url, file, fileKey, params);
    }
    //[11]异步post文件上传
    public static void postAsyn(String url, ResultCallback callback, File[] files, String[] fileKeys, Param... params) throws IOException {
        getInstance()._postAsyn(url, callback, files, fileKeys, params);
    }
    //[12]异步post文件上传，单文件不带参数上传
    public static void postAsyn(String url, ResultCallback callback, File file, String fileKey) throws IOException {
        getInstance()._postAsyn(url, callback, file, fileKey);
    }
    //[13]异步post文件上传，单文件且携带其他form参数上传
    public static void postAsyn(String url, ResultCallback callback, File file, String fileKey, Param... params) throws IOException {
        getInstance()._postAsyn(url, callback, file, fileKey, params);
    }
    //[14]下载图片并加载图片，可以设置默认加载失败的图片
    public static void displayImage(final ImageView view, String url, int errorResId) throws IOException {
        getInstance()._displayImage(view, url, errorResId);
    }
    //[15]下载图片并加载图片
    public static void displayImage(final ImageView view, String url) {
        getInstance()._displayImage(view, url, -1);
    }
    //[16]异步下载文件
    public static void downloadAsyn(String url, String destDir, ResultCallback callback) {
        getInstance()._downloadAsyn(url, destDir, callback);
    }

    //****************************
    private Request buildMultipartFormRequest(String url, File[] files,
                                              String[] fileKeys, Param[] params) {
        params = validateParam(params);
        //构建MultipartBuilder
        MultipartBuilder builder = new MultipartBuilder()
                .type(MultipartBuilder.FORM);  //设置为表单数据
        //[1]params的requestBody的构建
        for (Param param : params) {
            builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + param.key + "\""),
                    RequestBody.create(null, param.value));
        }
        //[2]file的requestBody的构建
        if (files != null) {
            RequestBody fileBody = null;
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String fileName = file.getName();
                fileBody = RequestBody.create(MediaType.parse(guessMimeType(fileName)), file);
                //TODO 根据文件名设置contentType
                builder.addPart(Headers.of("Content-Disposition",
                        "form-data; name=\"" + fileKeys[i] + "\"; filename=\"" + fileName + "\""),
                        fileBody);
            }
        }
        RequestBody requestBody = builder.build();
        return new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
    }

    //获取文件的Content-Type(Mime-Type)值
    private String guessMimeType(String path) {
        //获取文件Content-Type(Mime-Type)
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream"; //默认为"application/octet-stream"
        }
        return contentTypeFor;
    }

    //增加对params为空的赋值
    private Param[] validateParam(Param[] params) {
        if (params == null)
            return new Param[0];
        else return params;
    }

    private Param[] map2Params(Map<String, String> params) {
        if (params == null)
            return new Param[0];

        int size = params.size();
        Param[] res = new Param[size];
        Set<Map.Entry<String, String>> entries = params.entrySet();
        int i = 0;
        for (Map.Entry<String, String> entry : entries) {
            res[i++] = new Param(entry.getKey(), entry.getValue());
        }
        return res;
    }

    private static final String SESSION_KEY = "Set-Cookie";
    private static final String mSessionKey = "JSESSIONID";

    private Map<String, String> mSessions = new HashMap<String, String>();

    private void deliveryResult(final ResultCallback callback, Request request) {
        mOkHttpClient.newCall(request).enqueue(new Callback() { //直接调用okhttp的异步请求
            @Override
            public void onFailure(final Request request, final IOException e) {
                sendFailedStringCallback(request, e, callback);
            }

            @Override
            public void onResponse(final Response response) {
                try {
                    final String string = response.body().string();
                    //如果结果是String类型，返回string
                    if (callback.mType == String.class) {
                        sendSuccessResultCallback(string, callback);
                    } else {
                        //否则，使用Gson解析返回json数据；
                        Object o = mGson.fromJson(string, callback.mType);
                        sendSuccessResultCallback(o, callback);
                    }
                } catch (IOException e) {
                    sendFailedStringCallback(response.request(), e, callback);//返回错误信息
                } catch (com.google.gson.JsonParseException e) {
                    sendFailedStringCallback(response.request(), e, callback);//返回json解析错误；
                }
            }
        });
    }

    private void sendFailedStringCallback(final Request request, final Exception e, final ResultCallback callback) {
        //使用handler在子线程中将错误response和error码返回
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null)
                    callback.onError(request, e);
            }
        });
    }

    private void sendSuccessResultCallback(final Object object, final ResultCallback callback) {
        //使用handler在子线程中将response结果返回
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onResponse(object);
                }
            }
        });
    }

    /**
     * 生成FormEncodingBuilder/FormBody,然后填充数据；
     *
     * @param url
     * @param params
     * @return
     */
    private Request buildPostRequest(String url, Param[] params) {
        if (params == null) {
            params = new Param[0];
        }

        /*
            okHttp3的写法；
        FormBody.Builder builder1 = new FormBody.Builder();
        for (Param param:params){
            builder1.add(param.key,param.value);
        }*/

        //创建一个fromBody--okHttp3的写法;
        FormEncodingBuilder builder = new FormEncodingBuilder();
        for (Param param : params) {
            builder.add(param.key, param.value);
        }
        RequestBody requestBody = builder.build();
        return new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
    }

    /**
     * 将callback封装成抽象类
     *
     * @param <T>
     */
    public static abstract class ResultCallback<T> {
        Type mType;  //返回的response数据具有不同的类型，此处必须判断当前的

        public ResultCallback() {
            mType = getSuperclassTypeParameter(getClass());
        }

        static Type getSuperclassTypeParameter(Class<?> subclass) {
            Type superclass = subclass.getGenericSuperclass();
            if (superclass instanceof Class) {
                throw new RuntimeException("Missing type parameter.");
            }
            //对应找到Gson的类型；
            ParameterizedType parameterized = (ParameterizedType) superclass;
            return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
        }

        //对应onFailuer方法；
        public abstract void onError(Request request, Exception e);

        //对应onResponse方法；
        public abstract void onResponse(T response);
    }

    public static class Param {
        public Param() {
        }

        public Param(String key, String value) {
            this.key = key;
            this.value = value;
        }

        String key;
        String value;
    }
}

