package com.oztaking.www.a02_okhttp.okHttpUtils;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;


/**
 * [8]文件的上传增加进度
 * 对于上传的进度的处理会比较麻烦,因为具体的上传过程是在RequestBody中由OkHttp帮我们处理上传,
 * 而且OkHttp并没有给我们提供上传进度的接口,这里做法是自定义类继承RequestBody,
 * 然后重写其中的方法,将其中的上传进度通过接口回调暴露出来供使用
 * 实质：在原有的RequestBody上包装了一层,最后在我们的使用中在post()方法中传入我们的CountingRequestBody对象即可
 */

public class progressRequestBody extends RequestBody {

    //实际要使用的requestBody
    private ResponseBody mDelegate;
    //回调监听
    private okHttpUpLoadProgressListener mListener;
    private CountingSink countingSink;

    public progressRequestBody(ResponseBody mDelegate, okHttpUpLoadProgressListener mListener) {
        this.mDelegate = mDelegate;
        this.mListener = mListener;
    }

    @Override
    public MediaType contentType() {
        return mDelegate.contentType();
    }

    /* 返回文件总的字节大小
     * 如果文件大小获取失败则返回-1*/
    @Override
    public long contentLength() {
        return mDelegate.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        countingSink = new CountingSink(sink);
        //将CountingSink转化为BufferedSink供writeTo()使用
        BufferedSink bufferedSink = Okio.buffer(countingSink);
        bufferedSink.flush();
    }


    protected final class CountingSink extends ForwardingSink {

        private long byteWritten;

        public CountingSink(Sink delegate) {
            super(delegate);
        }

        /**
         * **
         * 上传时调用该方法,在其中调用回调函数将上传进度暴露出去,
         * 该方法提供了缓冲区的自己大小
         * @param source
         * @param byteCount
         * @throws IOException
         */

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            byteWritten += byteCount;
            mListener.onRequestProgress(byteWritten,contentLength());
        }
    }


    public static interface okHttpUpLoadProgressListener {
        /**
         * 暴露出上传进度
         *
         * @param byteWritten   已经上传的字节大小
         * @param contentLength 文件的总字节大小
         */
        void onRequestProgress(long byteWritten, long contentLength);
    }
}
