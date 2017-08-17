package com.demos.spring.httpdemo.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模拟网页请求（以139邮箱为例）
 */
public class HttpClientUtils {

    public static Logger log = Logger.getLogger(HttpClientUtils.class);

    /** 是否已经登陆标识 */
    public static boolean isLogin = false;

    // 自己管理Cookie
    public static Map<String, String> rfCookies = new HashMap<String, String>();

    // Cookie上下文, 不用自己管理
    public static CookieStore cookieStore = new BasicCookieStore();

    // 当前使用的139邮箱账号和密码
    public static String curAccount;
    public static String curPassword;

    private static RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES)
            .setSocketTimeout(600000).setConnectTimeout(60000).setConnectionRequestTimeout(120000).build();// 设置请求和传输超时时间

    // 获取当前储存的所有cookie[自己管理cookie]
    public static String getCookieStore() {
        StringBuffer buff = new StringBuffer();
        for (Map.Entry<String, String> entry : rfCookies.entrySet()) {
            buff.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
        }
        return buff.toString();
    }

    // 请求返回后存入新的cookie[自己管理cookie]
    public static void setCookieStore(HttpResponse httpResponse) {
        // Set-Cookie: CSRFToken=dcf049a9-89fd-4b76-8260-2c74806d0abc,
        // Set-Cookie: JSESSIONID=44b85999-a0bc-4423-9f78-ef18397af451;
        // Path=/group; HttpOnly
        Header[] cookies = httpResponse.getHeaders("Set-Cookie");
        for (Header header : cookies) {
            String[] cookieInfo = header.getValue().split(";")[0].split("=");
            if (cookieInfo.length != 2) {
                continue;
            }
            String name = cookieInfo[0];
            String value = cookieInfo[1];
            rfCookies.put(name, value);
        }
    }

    // https方式获取html文件
    public String getSSLHtml(String url) throws Exception {
        String content = null;
        CloseableHttpClient client = SSLClient.createSSLInsecureClient();
        try {
            HttpGet httpGet = new HttpGet(url);

            // 设置 Request Headers
            httpGet.setHeader("Upgrade-Insecure-Requests", "1");
            httpGet.setHeader("Host", "login.10086.cn");
            httpGet.setHeader("Referer", "http://www.hn.10086.cn/service/static/componant/login.html");
            httpGet.setHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");

            // 设置 Cookie[Request Headers]
            String cookie = getCookieStore(); // 当前所有cookie
            if (StringUtils.isNotEmpty(cookie)) {
                httpGet.setHeader("Cookie", cookie);
            }

            // httpGet.setConfig(requestConfig);
            CloseableHttpResponse response = client.execute(httpGet);
            // 存储response返回的cookie
            setCookieStore(response);
            // setCookieStore(response);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                content = IOUtils.toString(entity.getContent());
                EntityUtils.consume(entity);
            } else {
                log.warn("get方法响应异常:" + response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            log.error("get方法执行异常", e);
            throw e;
        } finally {
            client.close();
        }
        return content;
    }

    // http方式获取html文件
    public String getHtml(String url) throws Exception {
        String content = null;
        CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
        try {
            HttpGet httpGet = new HttpGet(url);

            // 设置 Request Headers
            httpGet.setHeader("Host", "mail.10086.cn");
            httpGet.setHeader("Upgrade-Insecure-Requests", "1");
            httpGet.setHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");

            // 设置 Cookie[Request Headers]
            String cookie = getCookieStore();
            if (StringUtils.isNotEmpty(cookie)) {
                httpGet.setHeader("Cookie", cookie);
            }
            // httpGet.setConfig(requestConfig);
            CloseableHttpResponse response = client.execute(httpGet);
            // 存储response返回的cookie
            setCookieStore(response);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                content = IOUtils.toString(entity.getContent());
                EntityUtils.consume(entity);
            } else {
                log.warn("get方法响应异常:" + response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            log.error("get方法执行异常", e);
            throw e;
        } finally {
            client.close();
        }
        return content;
    }

    // 传入Map参数
    public String post(String url, Map<String, String> paramMap, Map<String, String> cookieMap)
            throws ClientProtocolException, IOException {
        CloseableHttpClient client = null;
        try {
            List<NameValuePair> paramList = new ArrayList<NameValuePair>();
            for (java.util.Map.Entry<String, String> entry : paramMap.entrySet()) {
                paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            // 增加cookie
            for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
                rfCookies.put(entry.getKey(), entry.getValue());
            }

            client = SSLClient.createSSLInsecureClient();
            HttpPost postMethod = new HttpPost(url);
            postMethod.setHeader("Cookie", getCookieStore());

            postMethod.setHeader("Host", "mail.10086.cn");
            postMethod.setHeader("Origin", "http://mail.10086.cn");
            postMethod.setHeader("Referer",
                    "http://mail.10086.cn/default.html?&s=1&v=0&u=MTM4NzU3NzgyMDQ=&m=1&ec=S001&resource=indexLogin&cguid=1647030751977&mtime=24");
            postMethod.setHeader("Upgrade-Insecure-Requests", "1");
            postMethod.setHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
            postMethod.setConfig(requestConfig);
            postMethod.setEntity(new UrlEncodedFormEntity(paramList, Charset.forName("UTF-8")));

            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            multipartEntityBuilder.addPart("file", new FileBody(new File("")));
            postMethod.setEntity(multipartEntityBuilder.build());

            CloseableHttpResponse postRes = null;
            postRes = client.execute(postMethod);
            setCookieStore(postRes);
            Header[] headers = postRes.getHeaders("Location");
            String value = headers[0].getValue();
            return value;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    // Cookie上下文手动添加cookie
    public String post(String url, String body, Map<String, String> cookieMap)
            throws ClientProtocolException, IOException {
        CloseableHttpClient client = null;
        String content = null;
        try {
            // Cookie上下文方式自动管理
            client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
            HttpPost postMethod = new HttpPost(url);
            postMethod.setEntity(new StringEntity(body, "utf-8"));

            // 增加Cookie
            for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
                BasicClientCookie cookie = new BasicClientCookie(entry.getKey(), entry.getValue());
                cookie.setVersion(0);
                cookie.setDomain("mail.10086.cn");
                cookie.setPath("/");
                cookieStore.addCookie(cookie);
            }

            CloseableHttpResponse postRes = client.execute(postMethod);
            HttpEntity entitys = postRes.getEntity();
            content = IOUtils.toString(entitys.getContent());
            EntityUtils.consume(entitys);
        } finally {
            if (client != null) {
                client.close();
            }
        }
        return content;
    }

    // 下载文件
    public File downFile(String url, String uuid) throws Exception {
        InputStream input = null;
        CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
        File storeFile = new File("D:/" + "" + uuid + ".jpg"); // 存储为jpg格式
        FileOutputStream fileOutputStream = null;
        try {
            HttpGet httpGet = new HttpGet(url);
            // 使用cookies
            if (StringUtils.isNotEmpty(getCookieStore())) {
                httpGet.setHeader("Cookie", getCookieStore());
            }
            CloseableHttpResponse response = client.execute(httpGet);
            // 设置cookies
            setCookieStore(response);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                input = entity.getContent();
                fileOutputStream = new FileOutputStream(storeFile);
                byte[] buffer = new byte[1024];
                int ch = 0;
                while ((ch = input.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, ch);
                }
                input.close();
                fileOutputStream.flush();
                EntityUtils.consume(entity);
            } else {
                log.warn("get方法响应异常:" + response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            log.error("get方法执行异常", e);
            throw e;
        } finally {
            IOUtils.closeQuietly(fileOutputStream);
            IOUtils.closeQuietly(input);
            client.close();
        }
        return storeFile;
    }

    // 上传文件
    public String uploadFile(File file, String codeType) throws IOException, JSONException {
        String content = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            // 以验证码识别网站为例
            HttpPost httpPost = new HttpPost("http://upload.chaojiying.net/Upload/Processing.php");
            httpPost.setConfig(requestConfig);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addBinaryBody("userfile", FileUtils.readFileToByteArray(file), ContentType.DEFAULT_BINARY, "1.jpg");
            builder.addTextBody("user", "notangys", ContentType.TEXT_PLAIN);
            builder.addTextBody("pass", "ouzhiyu", ContentType.TEXT_PLAIN);
            builder.addTextBody("softid", "891813", ContentType.TEXT_PLAIN);
            builder.addTextBody("codetype", codeType, ContentType.TEXT_PLAIN);
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            HttpResponse response = httpclient.execute(httpPost);
            content = IOUtils.toString(response.getEntity().getContent());
        } finally {
            httpclient.close();
        }
        return content;
    }

    // 请求之后跳转
    public String post302(String url, Map<String, String> paramMap) throws ClientProtocolException, IOException {
        CloseableHttpClient client = null;
        try {
            List<NameValuePair> paramList = new ArrayList<NameValuePair>();
            for (java.util.Map.Entry<String, String> entry : paramMap.entrySet()) {
                paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
            HttpPost postMethod = new HttpPost(url);
            postMethod.setEntity(new UrlEncodedFormEntity(paramList, Charset.forName("UTF-8")));
            CloseableHttpResponse postRes = null;
            postRes = client.execute(postMethod);
            Header[] headers = postRes.getHeaders("Location");
            String value = headers[0].getValue();
            return value;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

}
