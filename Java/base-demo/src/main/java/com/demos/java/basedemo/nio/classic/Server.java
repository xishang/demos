package com.demos.java.basedemo.nio.classic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/31
 * <p>
 * 传统的SocketI/O
 */
public class Server implements Runnable {

    public static final int MAX_INPUT = 1024 * 1024;

    private int port;

    public Server(int port) {
        this.port = port;
    }

    public void run() {
        try {
            ServerSocket ss = new ServerSocket(port);
            while (!Thread.interrupted())
                new Thread(new Handler(ss.accept())).start();
            // or, single-threaded, or a thread pool
        } catch (IOException ex) {
            /* ... */
        }
    }

    static class Handler implements Runnable {
        final Socket socket;

        Handler(Socket s) {
            socket = s;
        }

        public void run() {
            try {
                byte[] input = new byte[MAX_INPUT];
                socket.getInputStream().read(input);
                byte[] output = process(input);
                socket.getOutputStream().write(output);
            } catch (IOException ex) {
                /* ... */
            }
        }

        private byte[] process(byte[] cmd) {
            /* ... */
            return null;
        }
    }

}
