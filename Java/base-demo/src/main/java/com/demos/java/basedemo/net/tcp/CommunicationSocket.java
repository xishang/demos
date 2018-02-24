package com.demos.java.basedemo.net.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/2/24
 */
public class CommunicationSocket {

    public static void main(String[] args) throws IOException, InterruptedException {
        for (int i = 0; i < 5; i++) {
            new Thread(CommunicationSocket::task).start();
            Thread.sleep(5000);
        }
    }

    public static void task() {
        Socket socket = null;
        OutputStream os = null;
        InputStream is = null;
        try {
            socket = new Socket("localhost", 8800);
            is = socket.getInputStream();
            os = socket.getOutputStream();
            // 1.客户端首先发送信息给服务端，否则会互相等待输入导致死锁
            String message = "你好，现在时间是: " + LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
            os.write(message.getBytes("utf-8"));
            os.flush();

            byte[] data = new byte[1024];
            int size;
            // 2.while循环接收输入数据以达到交流的目的，只要Server端不关闭输出流(EOF)，就能一直保持通信
            while ((size = is.read(data)) != -1) {
                // 3.打印接收到的信息
                System.out.println("接收到服务端信息: " + new String(data, 0, size, "utf-8"));
                // 4.发送响应信息
                String message2 = "你好，收到回复，现在时间是: " + LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
                os.write(message2.getBytes("utf-8"));
                os.flush();
                // 5.休眠5秒，然后进行下一次交互
                Thread.sleep(5000);
            }
        } catch (Exception e) {
        } finally {
            SocketUtils.close(os, is, socket);
        }
    }

}
