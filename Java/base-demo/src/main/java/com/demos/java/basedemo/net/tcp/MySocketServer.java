package com.demos.java.basedemo.net.tcp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/2/24
 */
public class MySocketServer {

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
            BufferedReader br = null;
            OutputStream os = null;
            try {
                StringBuilder sb = new StringBuilder();
                is = socket.getInputStream();
                br = new BufferedReader(new InputStreamReader(is, "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                System.out.printf("接收到socket信息: %s \n", sb);
                os = socket.getOutputStream();
                os.write("你好，已接收到你的信息！".getBytes("utf-8"));
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                SocketUtils.close(os, br, is, socket);
            }
        }
    }

}
