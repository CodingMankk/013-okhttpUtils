package com.codingman.www.okhttputilssample;

import android.app.Application;

import com.zhy.http.okhttp.OkHttpUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.OkHttpClient;

/**
 * @function
 */

public class MyApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            OkHttpUtils.initClient(setCertificates(getAssets().open("srca.cer")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * 当客户端进行SSL连接时，就可以根据我们的证书去决定是否信任服务端的证书
     *
     * @param certificates 证书输入流
     */
    public OkHttpClient setCertificates(InputStream... certificates){

        try {

            //构造CertificateFactory对象，通过它的 generateCertificate(is) 方法得到Certificate
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);

            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                //将得到的Certificate放入到keyStore中
                keyStore.setCertificateEntry(certificateAlias,
                        certificateFactory.generateCertificate(certificate));

                if (certificate != null) {
                    certificate.close();
                }
            }

            //利用keyStore去初始化TrustManagerFactory
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            //由trustManagerFactory.getTrustManagers获得TrustManager[]初始化SSLContext
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());

//            //最后设置mOkHttpClient.setSslSocketFactory
//            OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                    .sslSocketFactory(sslContext.getSocketFactory())
//                    .build();

            //初始化设置一个okhttpClient
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory())
                    .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                    .readTimeout(10000L, TimeUnit.MILLISECONDS)  //读时间不能过短，否则请求读取无法成功；
                    .build();

            return okHttpClient;



//            Response execute = OkHttpUtils.get()
//                    .url("https://kyfw.12306.cn/otn/")
//                    .build()
//                    .execute();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }
}
