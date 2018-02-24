package com.demos.java.basedemo.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/2/24
 */
public class URLDemo {

    public static void main(String[] args) throws Exception {
        URL url = new URL("https://www.baidu.com");
        InputStream is = url.openStream();
        File outputFile = new File("/Users/xishang/temp/baidu.html");
        OutputStream os = new FileOutputStream(outputFile);
        byte[] data = new byte[1024];
        int size;
        while ((size = is.read(data)) != -1) {
            os.write(data, 0, size);
        }
        os.close();
        is.close();
    }

}
