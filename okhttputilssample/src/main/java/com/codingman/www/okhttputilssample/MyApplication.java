package com.codingman.www.okhttputilssample;

import android.app.Application;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.https.HttpsUtils;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.OkHttpClient;
import okio.Buffer;

/**
 * @function
 */

public class MyApplication extends Application{


    /**
     * [cmd命令]
     * keytool -printcert -rfc -file srca.cer
     */
    private String cer_12306 = "-----BEGIN CERTIFICATE-----\n"+
            "MIICmjCCAgOgAwIBAgIIbyZr5/jKH6QwDQYJKoZIhvcNAQEFBQAwRzELMAkGA1UEBhMCQ04xKTAn"+
            "BgNVBAoTIFNpbm9yYWlsIENlcnRpZmljYXRpb24gQXV0aG9yaXR5MQ0wCwYDVQQDEwRTUkNBMB4X"+
            "DTA5MDUyNTA2NTYwMFoXDTI5MDUyMDA2NTYwMFowRzELMAkGA1UEBhMCQ04xKTAnBgNVBAoTIFNp"+
            "bm9yYWlsIENlcnRpZmljYXRpb24gQXV0aG9yaXR5MQ0wCwYDVQQDEwRTUkNBMIGfMA0GCSqGSIb3"+
            "DQEBAQUAA4GNADCBiQKBgQDMpbNeb34p0GvLkZ6t72/OOba4mX2K/eZRWFfnuk8e5jKDH+9BgCb2"+
            "9bSotqPqTbxXWPxIOz8EjyUO3bfR5pQ8ovNTOlks2rS5BdMhoi4sUjCKi5ELiqtyww/XgY5iFqv6"+
            "D4Pw9QvOUcdRVSbPWo1DwMmH75It6pk/rARIFHEjWwIDAQABo4GOMIGLMB8GA1UdIwQYMBaAFHle"+
            "tne34lKDQ+3HUYhMY4UsAENYMAwGA1UdEwQFMAMBAf8wLgYDVR0fBCcwJTAjoCGgH4YdaHR0cDov"+
            "LzE5Mi4xNjguOS4xNDkvY3JsMS5jcmwwCwYDVR0PBAQDAgH+MB0GA1UdDgQWBBR5XrZ3t+JSg0Pt"+
            "x1GITGOFLABDWDANBgkqhkiG9w0BAQUFAAOBgQDGrAm2U/of1LbOnG2bnnQtgcVaBXiVJF8LKPaV"+
            "23XQ96HU8xfgSZMJS6U00WHAI7zp0q208RSUft9wDq9ee///VOhzR6Tebg9QfyPSohkBrhXQenvQ"+
            "og555S+C3eJAAVeNCTeMS3N/M5hzBRJAoffn3qoYdAO1Q8bTguOi+2849A=="+
            "-----END CERTIFICATE-----";

    private String bendiceshi = "-----BEGIN CERTIFICATE-----\n"+
            "MIIDQzCCAiugAwIBAgIEQFmaCjANBgkqhkiG9w0BAQsFADBRMQswCQYDVQQGEwJjbjEKMAgGA1UE"+
            "CBMBczEKMAgGA1UEBxMBczEKMAgGA1UEChMBczEKMAgGA1UECxMBczESMBAGA1UEAxMJbG9jYWxo"+
            "b3N0MCAXDTE4MDcwNDE0NDgzM1oYDzIxMTgwNjEwMTQ0ODMzWjBRMQswCQYDVQQGEwJjbjEKMAgG"+
            "A1UECBMBczEKMAgGA1UEBxMBczEKMAgGA1UEChMBczEKMAgGA1UECxMBczESMBAGA1UEAxMJbG9j"+
            "YWxob3N0MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhzpGZCQH5uv/JvK+NfwVH/fm"+
            "uKTi1vXAfT1Z3s8mhJtqFrbULzy8vx4EtbI8N+vpy+NjbX/u9toof/SGCYIrW5JOB32OvwXWpZiS"+
            "K+jqG78TamSvtIIuW1Hq7uNDTR4SbQh4iIGY6QYGmFr1aulYfuSuME2pOf69vUfnd/kdq53AM5aq"+
            "PYE1Iok6zAtntfKdQInmPgi5yA6g8X0Z2HwCkX3ncuJ/L13nOr4aj+BC1qsrRiIxNfuG0sSQg/xT"+
            "UHrEjZpshYNVaAL/5do/Tip4tS81PVt2EID4I2OMoFitgRuKteDeJlqpQSyGh8uTNdf5REETe4aQ"+
            "PXs9WA9bzF1+PQIDAQABoyEwHzAdBgNVHQ4EFgQUmGNekK0CP4LqIt3imr62Z+om1+0wDQYJKoZI"+
            "hvcNAQELBQADggEBAFl6+NDvnZVNJZ/PRVianLZHdlLDAxL90wZ3w2YcUOlLtkvfDZMsz62iu13b"+
            "cTv0mdlgjFLVbVgzykzGHKYg4AhYtOXlcq/WsWQty862NkolwRPLRgFtbTDU3AnMDN/9RwBBbV38"+
            "gOHdgF0VTtOV1UDqXt549KIkfx/+3co39TbbOnuXNsOu1zjRL9poUzH8g7KUUsRCjkKKfzQdQsrA"+
            "EdV8DVBUhNSU4b/AQ+SvlVqRCEKNI/GFDgqorAoG4sBglWHxroAon3OPP4kxpUTWve2dac3WlNFG"+
            "JBwVAi7DdT1NiGc/sWRW21VlYH6eRjbkhgRFnfu2sjY/KbhpfpLibZE=\n"+
            "-----END CERTIFICATE-----";

    private String bendiceshi_ip = "-----BEGIN CERTIFICATE-----\n"+
            "MIIDSzCCAjOgAwIBAgIEK6InATANBgkqhkiG9w0BAQsFADBVMQswCQYDVQQGEwJjbjEKMAgGA1UE"+
            "CBMBczEKMAgGA1UEBxMBczEKMAgGA1UEChMBczEKMAgGA1UECxMBczEWMBQGA1UEAxMNMTkyLjE2"+
            "OC4xLjEwNDAgFw0xODA3MDQxNDQ5MTFaGA8yMTE4MDYxMDE0NDkxMVowVTELMAkGA1UEBhMCY24x"+
            "CjAIBgNVBAgTAXMxCjAIBgNVBAcTAXMxCjAIBgNVBAoTAXMxCjAIBgNVBAsTAXMxFjAUBgNVBAMT"+
            "DTE5Mi4xNjguMS4xMDQwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCPS7XBTvGrfXON"+
            "qhh7wsLHkdy02asTSEn/xB1tTCXxkEZLJMQKivvIOrW4oBxJlxrehkNq+FWobXsANLDnb+djW8LG"+
            "79UbbTQkNGlSZMcbCpyvoJwFGuKrne08toV/h7azcw4BYcRobi/6o7r0YFaAxpgR9gzkzkXJi3oq"+
            "9zrShToEnklBtAg2XVKvfs0MN1v6zMZ9NKBazuVLcufVkXlwbMhsavneholNZxxGWRnhKwEQl8zQ"+
            "A5B9v4KqCyjoKnjUxPtziYtXEtP2f7lqVCZ9+oVXKoybRQsEKxOwM/CS7NBMlq6NO4lp8tvhHITI"+
            "9/yCXWIX1YxjIRhjsM/nju8xAgMBAAGjITAfMB0GA1UdDgQWBBS93iqvpzCPNbRDKLksNN3Tg2k5"+
            "NzANBgkqhkiG9w0BAQsFAAOCAQEAiyGnOkCkXASxEKkft81HPACDz4mz4cjyI8bJHWOGdvgMtEEg"+
            "BrxBBaOt2B46JAiC8K8baYNwpjpVxnFOhDItPwpC96iFv7h7/PouhFiTTmC2q5y+aIfUSJY3l22W"+
            "WMtH3iQzUl649PSpw1n/FsY4eIZ1eRp2xLFlH4E2e2qnCL/XSXXQbeZ1WbQQ4Th0/IUYvLcD7xxl"+
            "6hoDn2B+0GXzKirIl5xMdTb9el/pITemtLRNq13b78yyqkbKX0wyBnJIKqZHHwljZi3uoSYIKzu7"+
            "9w9z4dzQVtNYX/8ix5eu2/dVDvHvpNkxdhy4zt+NG59d0iiCeAlL5STMWmEEc8k3tA==\n"+
            "-----END CERTIFICATE-----";


    @Override
    public void onCreate() {
        super.onCreate();

        HttpsUtils.SSLParams sslParams = null;

        InputStream inbendiceshi_ip = new Buffer().writeUtf8(bendiceshi_ip).inputStream();
        InputStream inbendiceshi = new Buffer().writeUtf8(bendiceshi).inputStream();


        InputStream [] ins = new InputStream[]{inbendiceshi_ip,inbendiceshi};

        sslParams = HttpsUtils.getSslSocketFactory(ins, null, null);
//        sslParams = HttpsUtils.getSslSocketFactory(null, null, null);

//        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                .readTimeout(2000L,TimeUnit.MILLISECONDS)
                .connectTimeout(2000L,TimeUnit.MILLISECONDS)
                .writeTimeout(2000L,TimeUnit.MILLISECONDS)
         .build();
        OkHttpUtils.initClient(okHttpClient);

     /*   try {
            //[1]此处使用的是assets文件夹下的srca.cer文件输入证书的；
//            OkHttpUtils.initClient(setCertificates(getAssets().open("srca.cer")));

        } catch (IOException e) {
            e.printStackTrace();
        }*/

        /**
         *[2]使用String字符串代替证书文件
         *
         * [cmd命令]
         * keytool -printcert -rfc -file srca.cer
         */
//        OkHttpClient okHttpClient = null;
//        try {
//            okHttpClient = setCertificates(new Buffer().writeUtf8(cer_12306).inputStream(),
//                    new Buffer().writeUtf8(bendiceshi).inputStream(),
//                    new Buffer().writeUtf8(bendiceshi_ip).inputStream(),
//                    getAssets().open("ozTaking_server.cer")
////                    getAssets().open("bendiceshi.cer"),
////                    getAssets().open("bendiceshi_ip.cer")
//                    );
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        OkHttpUtils.initClient(okHttpClient);

        /**
         * [3]
         * 生成自签名的证书
         * keytool -genkey -alias ozTaking_server -keyalg RSA -keystore ozTaking_server.jks -validity 3600 -storepass 88888
         *
         * [4]
         *签发证书
         *G:\001_Android\005_okhttp\07-https\12306 cer>keytool -export -alias ozTaking_ser
         *ver -file ozTaking_server.cer -keystore ozTaking_server.jks -storepass 888888
         *存储在文件 <ozTaking_server.cer> 中的证书
         *
         */

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
