package com.demos.java.basedemo.net.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/2/24
 * <p>
 * 交互式SocketServer
 */
public class CommunicationSocketServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8800);
        System.out.println("服务开始，监听8800端口-----------");
        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new SocketTask(socket)).start();
        }
    }

    static class SocketTask implements Runnable {
        private Socket socket;

        SocketTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("建立socket连接------");
            InputStream is = null;
            OutputStream os = null;
            try {
                is = socket.getInputStream();
                os = socket.getOutputStream();
                // while循环中与客户端交互
                byte[] data = new byte[1024];
                int size;
                while ((size = is.read(data)) != -1) {
                    System.out.println("接收到客户端数据: " + new String(data, 0, size, "utf-8"));
                    os.write("你好，已接收到你的信息！".getBytes("utf-8"));
                    os.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                SocketUtils.close(is, os, socket);
            }
        }
    }

}
