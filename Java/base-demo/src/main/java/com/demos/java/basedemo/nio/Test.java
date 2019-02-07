package com.demos.java.basedemo.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.Charset;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/2/1
 */
public class Test {

    public static void main(String[] args) {
        test(true, 7777, 1023);
        test(true, 7778, 1024);
    }

    static void test(boolean block, int port, int back) {
        new Thread(() -> {
            Charset charset = Charset.forName("UTF-8");
            // 创建一个选择器，可用close()关闭，isOpen()表示是否处于打开状态，他不隶属于当前线程
            Selector selector = null;
            try {
                selector = Selector.open();
                // 创建ServerSocketChannel，并把它绑定到指定端口上
                ServerSocketChannel server = ServerSocketChannel.open();
                server.socket().bind(new InetSocketAddress(port), back);
                // 设置为非阻塞模式, 这个非常重要
                long start = System.currentTimeMillis();
                System.out.println(String.valueOf(block) + ", start = " + start);
                server.configureBlocking(block);
                System.out.println(String.valueOf(block) + ", 耗时 = " + (System.currentTimeMillis() - start));
                // 在选择器里面注册关注这个服务器套接字通道的accept事件
                // ServerSocketChannel只有OP_ACCEPT可用，OP_CONNECT,OP_READ,OP_WRITE用于SocketChannel
                server.register(selector, SelectionKey.OP_ACCEPT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
