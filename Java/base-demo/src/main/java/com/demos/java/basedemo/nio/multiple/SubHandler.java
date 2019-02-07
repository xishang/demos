package com.demos.java.basedemo.nio.multiple;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/31
 */
public abstract class SubHandler implements Runnable {

    final Selector selector;
    final SelectionKey key;
    final SocketChannel socket;

    public SubHandler(Selector selector, SocketChannel socket, int operation) throws IOException {
        this.selector = selector;
        this.socket = socket;
        this.socket.configureBlocking(false);
        key = socket.register(selector, operation);
        key.attach(this);
    }

    @Override
    public void run() {
        doProcess();
    }

    protected abstract void doProcess();

    /**
     * socket write完之后应该关闭SelectionKey
     */
    protected final void closeSelectionKey() {
        key.cancel();
    }

}
