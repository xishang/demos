package com.demos.java.basedemo.net.tcp;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/2/24
 */
public class SocketUtils {

    public static void close(Closeable... list) {
        for (Closeable item : list) {
            try {
                item.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
