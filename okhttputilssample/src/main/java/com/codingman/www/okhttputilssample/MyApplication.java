package com.codingman.www.okhttputilssample;

import android.app.Application;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.https.HttpsUtils;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.OkHttpClient;

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

    private String bendiceshi ="-----BEGIN CERTIFICATE-----\n"+
        "MIIDQzCCAiugAwIBAgIEZsfnUTANBgkqhkiG9w0BAQsFADBRMQswCQYDVQQGEwJj\n"+
        "bjEKMAgGA1UECBMBczEKMAgGA1UEBxMBczEKMAgGA1UEChMBczEKMAgGA1UECxMB\n"+
        "czESMBAGA1UEAxMJbG9jYWxob3N0MCAXDTE4MDcwNTAyNTYyN1oYDzIxMTgwNjEx\n"+
        "MDI1NjI3WjBRMQswCQYDVQQGEwJjbjEKMAgGA1UECBMBczEKMAgGA1UEBxMBczEK\n"+
        "MAgGA1UEChMBczEKMAgGA1UECxMBczESMBAGA1UEAxMJbG9jYWxob3N0MIIBIjAN\n"+
        "BgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkz44kCElk2SMwuqpl6UAeR0Xh9Nv\n"+
        "+TaPDj+OZaD3bpPRygJRELXbmgn2AZ5h5nNts8L+hFSeCwHRq5TfbbXYsAjM6JRT\n"+
        "37fW/bZo34DSua+1mRIqHb9/HL8fSFk96cNQd7wUad77SOesNl5DW+UIqoJWRloN\n"+
        "bLFcBLEssPOou85Q0J/K1GtXCdtv45/wR/c2bZ57OARmgqCWb6tJoFWZLtlJTFuY\n"+
        "zeIv44YDIMOK6nNwc7nR1DlY4/NWRhayBINbzupLcjLY/O57wiBp3jL5U7Xvxw+R\n"+
        "KFzgDq9vJc/K/P4dCLN2/ZNmQt5LLxFV+3COqpGSg/kPlptwHgANDJ6GTwIDAQAB\n"+
        "oyEwHzAdBgNVHQ4EFgQUuux+6ot808C3eW+CV2UIuhN+CYMwDQYJKoZIhvcNAQEL\n"+
        "BQADggEBAHJKaR8bdKNw+Qr65Nwr/YFRSk6pmKkHBbkUR85IY/rOKZNvwxszM04X\n"+
        "AdIMN67zFh5SiBNdHdyr6OpaTszcn34ANhm6aSdZ22O/Mix6xa4nxmlxirnGBs8P\n"+
        "zcI2SsUEZeV8v8A0kdetpPLfL9aaezzFJ+PTCMidGMYxNUQ2w9LCloL6Ki2quYll\n"+
        "66j7T7+KmFu+Ls5elRI8vm7aWPG2Q2UMcNWjhlx8D8O/xAcy9QBk5eTyziqYry4B\n"+
        "b4GoNZBN/jDjWHGL9+l83Zn083t2HqD2LL5929Afjjxe6KZcxkXVJE4m65EK4Swq\n"+
        "SbclgPEvuANkY1S8uxUUSebNGBIa+Eo=\n"+
        "-----END CERTIFICATE-----";

    private String bendiceshi_ip ="-----BEGIN CERTIFICATE-----\n"+
            "MIIDSTCCAjGgAwIBAgIEe4CPxTANBgkqhkiG9w0BAQsFADBUMQswCQYDVQQGEwJj\n"+
            "bjEKMAgGA1UECBMBczEKMAgGA1UEBxMBczEKMAgGA1UEChMBczEKMAgGA1UECxMB\n"+
            "czEVMBMGA1UEAxMMMTkyLjE2OC4wLjEyMCAXDTE4MDcwNTAyNTY1NFoYDzIxMTgw\n"+
            "NjExMDI1NjU0WjBUMQswCQYDVQQGEwJjbjEKMAgGA1UECBMBczEKMAgGA1UEBxMB\n"+
            "czEKMAgGA1UEChMBczEKMAgGA1UECxMBczEVMBMGA1UEAxMMMTkyLjE2OC4wLjEy\n"+
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAl658SgbRwvgwiP0LB1i7\n"+
            "mNUTWpmlWXwXd3hiYUshgHWYDStIK+lX9SkRM8v/1amC4FvxIkJq4KQpnaUjDMpi\n"+
            "4Rx9xCPFEWRvtlFag8L65vBax8W6x3p4uVGTxTANU9d8esKJWfCsyHR476vOaJBz\n"+
            "Nv7Ig7dkWEMN8Zww7Mak37Cm9Cj0s/23R3QyaQE9MlLwQg29emEIPkPV700BqemE\n"+
            "EIdBfylDuIhhpeJkwfGVyjkRnPjC4B3pgdVylNTfs6bU6CIPhW58ED64P+Oi2lJI\n"+
            "kSspjBMS6Dd3cayw8vHkdwrx060pqxrMmiokUwDGRLlgW7v2Das5fsd2AI2LQlg/\n"+
            "oQIDAQABoyEwHzAdBgNVHQ4EFgQUP95xrzOb4h0F97WyNz2J9jxDIk0wDQYJKoZI\n"+
            "hvcNAQELBQADggEBAEmZEasGj7fMf3ynWoEdotQryH4/PFhwsvqz30sra+JJm06O\n"+
            "ypTyBexSleTUF0gZY1rKv8r65W8VVOOFgXomlSW6YsCm45xd3rVbNpF1+K1i63q7\n"+
            "jGQGeL6+7XgrMHDBS3K6l8h1pSwNJ65enEwsl4kgZWHnDvxJ+QB3JNXQ8RkvhJjX\n"+
            "byIsAftryyU+RWbyJM9GwVENck4PXzWImikR2ESon7Hv/DvckbYneLrHXHhKFTQg\n"+
            "AaKY7npVPuNY40kGsaxo8sr3i8WHnY49D0jevFZyfk+o/Sm9t7MawVCNdVzWTO5e\n"+
            "fL9CaOCmrQCCTxII3JQHnJadbCX598Mzft+QErA=\n"+
            "-----END CERTIFICATE-----";


    @Override
    public void onCreate() {
        super.onCreate();


        /**
         * [1]绕过所有的证书--不推荐；
         */
//        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);



        /**
         * [2]输入证书
         *
         * 输入字符串：keytool -printcert -rfc -file bendiceshi_ip.cer
         * 输入字符串：keytool -printcert -rfc -file bendiceshi.cer*/

            /*InputStream inCerIp = getApplicationContext().getClass().getClassLoader().getResourceAsStream("assets/bendiceshi_ip.cer");
            InputStream inCer = getApplicationContext().getClass().getClassLoader().getResourceAsStream("assets/bendiceshi.cer");
*/
        //在As中使用此方法获取资源不成功；
//            InputStream inCerIp = getAssets().open("bendiceshi_ip.cer");
//            InputStream inCer = getAssets().open("bendiceshi.cer");

           /* InputStream [] ins = new InputStream[]{inCerIp,inCer};

            HttpsUtils.SSLParams sslParams1 = HttpsUtils.getSslSocketFactory(ins, null, null);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .sslSocketFactory(sslParams1.sSLSocketFactory, sslParams1.trustManager)
                    .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                    .readTimeout(10000L, TimeUnit.MILLISECONDS)  //读时间不能过短，否则请求读取无法成功；
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            //强行返回true 即验证成功
                            return true;
                        }
                    })
                    .build();
            OkHttpUtils.initClient(okHttpClient);*/




        /**
         * [3] 将证书转换为String直接读取；--测试成功；
         */
      /*  InputStream inbendiceshi_ip = new Buffer().writeUtf8(bendiceshi_ip).inputStream();
        InputStream inbendiceshi = new Buffer().writeUtf8(bendiceshi).inputStream();
        InputStream [] ins = new InputStream[]{inbendiceshi_ip,inbendiceshi};

        HttpsUtils.SSLParams sslParams1 = HttpsUtils.getSslSocketFactory(ins, null, null);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(sslParams1.sSLSocketFactory, sslParams1.trustManager)
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)  //读时间不能过短，否则请求读取无法成功；
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        //强行返回true 即验证成功
                        return true;
                    }
                })
                .build();
        OkHttpUtils.initClient(okHttpClient);*/


        /**
         * [4]双向认证
         */

        InputStream inCerIp = getApplicationContext().getClass().getClassLoader().getResourceAsStream("assets/bendiceshi_ip.cer");
        InputStream inCer = getApplicationContext().getClass().getClassLoader().getResourceAsStream("assets/bendiceshi.cer");
        InputStream [] ins = new InputStream[]{inCerIp,inCer};

        InputStream inCerBks = getApplicationContext().getClass().getClassLoader().getResourceAsStream("assets/ozTaking_client.bks");

        /**
         *参数1：证书的inputstream
         *参数2：本地证书的inputstream
         *参数3：本地证书的密码
         */
        HttpsUtils.SSLParams sslParams1 = HttpsUtils.getSslSocketFactory(ins, inCerBks, "123456");

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(sslParams1.sSLSocketFactory, sslParams1.trustManager)
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)  //读时间不能过短，否则请求读取无法成功；
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        //强行返回true 即验证成功
                        return true;
                    }
                })
                .build();
        OkHttpUtils.initClient(okHttpClient);

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
