package com.demos.spring.httpdemo.util;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * HTTPS请求客户端
 */
public class SSLClient {

    /**
     * 获取Https 请求客户端
     * @return
     */
    public static CloseableHttpClient createSSLInsecureClient() {
        SSLContext sslcontext = createSSLContext();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new HostnameVerifier() {

            @Override
            public boolean verify(String paramString, SSLSession paramSSLSession) {
                return true;
            }
        });
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        return httpclient;
    }

    /**
     * 获取初始化SslContext
     * @return
     */
    private static SSLContext createSSLContext() {
        SSLContext sslcontext = null;
        try {
            sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[] {new TrustAnyTrustManager()}, new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return sslcontext;
    }

    /**
     * 自定义静态私有类
     */
    private static class TrustAnyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }

    }

}
