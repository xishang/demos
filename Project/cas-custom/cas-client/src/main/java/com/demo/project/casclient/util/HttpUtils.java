package com.demo.project.casclient.util;

import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/2/23
 */
public class HttpUtils {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    // 日志打印拦截器
    private static final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(logger::info);

    static { // 设置打印内容级别
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    // OkHttpClient
    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build();

    /**
     * HTTP GET
     *
     * @param url
     * @return
     */
    public static String get(String url) {
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.code() == 200) {
                return response.body().string();
            }
        } catch (Exception e) {
            logger.error("GET请求出错", e);
        }
        return null;
    }

    /**
     * HTTP POST
     *
     * @param url
     * @param body
     * @return
     */
    public static String post(String url, String body) {
        MediaType contentType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = body == null ? null : RequestBody.create(contentType, body);
        Request request = new Request.Builder().url(url)
                .post(requestBody)
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.code() == 200) {
                return response.body().string();
            }
        } catch (Exception e) {
            logger.error("POST请求出错", e);
        }
        return null;
    }

}
