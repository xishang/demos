package com.demos.java.basedemo.nio.multiple;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/31
 * <p>
 * MainReactor: 负责accept客户端的连接
 * 多个Selector的目的是负载均衡, 提高响应, 至于selector怎么分没有特殊的规定
 */
public class MainReactor implements Runnable {

    // accept分发器: 主分发器
    final Selector acceptSelector;
    // read分发器
    final Selector readSelector;
    // write分发器
    final Selector writeSelector;

    final ExecutorService threadPool = Executors.newCachedThreadPool();

    final ServerSocketChannel serverSocket;

    public MainReactor(int port) throws IOException {
        acceptSelector = Selector.open();
        readSelector = Selector.open();
        writeSelector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        SelectionKey sk = serverSocket.register(acceptSelector, SelectionKey.OP_ACCEPT);
        sk.attach(new Acceptor());
    }

    @Override
    public void run() {
        // 分发事件, 分发线程需要一直循环执行, 因此也可以直接 new Thread
        threadPool.execute(new DispatcherHandler(acceptSelector));
        threadPool.execute(new DispatcherHandler(readSelector));
        threadPool.execute(new DispatcherHandler(writeSelector));
    }

    class Acceptor implements Runnable {
        @Override
        public synchronized void run() {
            try {
                SocketChannel socket = serverSocket.accept();
                new ReadHandler(readSelector, socket);
                new WriteHandler(writeSelector, socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
