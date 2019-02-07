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
public class WriteHandler extends SubHandler {

    public static final int MAX_SIZE = 1024 * 1024;

    ByteBuffer output = ByteBuffer.allocate(MAX_SIZE);

    public WriteHandler(Selector selector, SocketChannel socket) throws IOException {
        super(selector, socket, SelectionKey.OP_WRITE);
    }

    @Override
    protected void doProcess() {
        try {
            write();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void write() throws IOException {
        process();
        socket.write(output);
        closeSelectionKey();
    }

    void process() {
       /* ... */
    }

}
