package com.demos.java.basedemo.nio.multiple;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/31
 */
public class ReadHandler extends SubHandler {

    public static final int MAX_SIZE = 1024 * 1024;

    ByteBuffer input = ByteBuffer.allocate(MAX_SIZE);

    public ReadHandler(Selector selector, SocketChannel socket) throws IOException {
        super(selector, socket, SelectionKey.OP_READ);
    }

    @Override
    protected void doProcess() {
        try {
            read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void read() throws IOException {
        socket.read(input);
        if (inputIsComplete()) {
            process();
        }
    }

    boolean inputIsComplete() {
            /* ... */
        return true;
    }

    void process() {
            /* ... */
    }

}
