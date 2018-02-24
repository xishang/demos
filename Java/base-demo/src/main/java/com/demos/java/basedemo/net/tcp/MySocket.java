package com.demos.java.basedemo.net.tcp;

import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/2/24
 */
public class MySocket {

    public static void main(String[] args) throws IOException, InterruptedException {
        for (int i = 0; i < 5; i++) {
            new Thread(MySocket::task).start();
            Thread.sleep(5000);
        }
    }

    public static void task() {
        Socket socket = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        try {
            socket = new Socket("localhost", 8800);
            // 向Server端发送信息
            os = socket.getOutputStream();
            String message = "你好，现在时间是: " + LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
            os.write(message.getBytes("utf-8"));
            os.flush();
            // 关闭输出流，以便Server端Socket接收完数据继续向下执行
            socket.shutdownOutput();

            // 读取Server端响应数据
            is = socket.getInputStream();
            br = new BufferedReader(new InputStreamReader(is, "utf-8"));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("接收到服务端信息: " + line);
            }
        } catch (Exception e) {
        } finally {
            SocketUtils.close(os, br, is, socket);
        }
    }

}
