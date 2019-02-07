package com.demos.java.basedemo.nio;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.Pipe;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/30
 */
public class Main {

    public static void readFile() throws Throwable {
        RandomAccessFile aFile = new RandomAccessFile("/Users/xishang/temp/file.txt", "rw");
        FileChannel inChannel = aFile.getChannel();
        /* ==================== Buffer: 用于和NIO通道进行交互, 数据是从通道读入缓冲区, 从缓冲区写入到通道中的
        -> 缓冲区本质上是一块可以写入数据, 然后可以从中读取数据的内存, 这块内存被包装成NIO Buffer对象, 并提供了一组方法, 用来方便的访问该块内存
        -> 类型: ByteBuffer、CharBuffer、ShortBuffer、IntBuffer、LongBuffer、FloatBuffer、DoubleBuffer、MappedByteBuffer
        -> 属性: capacity(Buffer容量)、limit(最多能读写的数据)、position(下一个读/写元素的索引)
        -> 分配内存: allocate()
         */
        ByteBuffer buf = ByteBuffer.allocate(48);
        /* ==================== 写数据到Buffer(两种方式):
        -> 1.从Channel写到Buffer: Channel.read()
        -> 2.调用Buffer.put()
         */
        int bytesRead = inChannel.read(buf); //read into buffer.
        while (bytesRead != -1) {
            /* ==================== Buffer.flip(): 将Buffer从写模式切换到读模式
            调用flip()方法会将position设回0, 并将limit设置成之前position的值
             */
            buf.flip();  //make buffer ready for read
            while (buf.hasRemaining()) {
                /* ==================== 从Buffer中读数据(两种方式):
                -> 1.从Buffer读取数据到Channel: Channel.write()
                -> 2.调用Buffer.get()
                 */
                System.out.print((char) buf.get()); // read 1 byte at a time
            }
            // ==================== Buffer.rewind(): 将position设为0, 可以重读Buffer中的数据
            buf.rewind();
            while (buf.hasRemaining()) {
                System.out.print((char) buf.get());
            }
            /* Buffer.clear()/compact(): 清空Buffer, 以再次写入
            -> clear(): 清空Buffer, 将position设回0
            -> compact(): 清空已读数据, 将position设到最后一个未读元素后面
             */
            buf.clear();
            bytesRead = inChannel.read(buf);
        }
        aFile.close();
        /* mark()和reset()
        通过调用Buffer.mark()方法, 可以标记Buffer中的一个特定position, 之后可以通过调用Buffer.reset()方法恢复到这个position
         */
    }

    public static void writeFile() throws Exception {
        RandomAccessFile file = new RandomAccessFile("/Users/xishang/temp/file1.txt", "rw");
        FileChannel channel = file.getChannel();
        // Buffer分配内存
        ByteBuffer buffer = ByteBuffer.allocate(20);
        // 写数据到Buffer
        buffer.put((byte) 'h').put((byte) 'i');
        channel.write(buffer);
        buffer.clear();
        file.close();
    }

    public static void scatterAndGather() throws Exception {
        RandomAccessFile readFile = new RandomAccessFile("/Users/xishang/temp/file.txt", "rw");
        FileChannel readChannel = readFile.getChannel();
        ByteBuffer header = ByteBuffer.allocate(128);
        ByteBuffer body = ByteBuffer.allocate(1024);
        ByteBuffer[] bufferArray = {header, body};
        /* ==================== scatter/gather: 用于从通道读取或写入数据
        -> 常用场合: 需要将传输的数据分开处理的场合, 如: 传输一个由消息头和消息体组成的消息
        -> scatter(分散): 将从Channel中读取的数据`分散(scatter)`到多个Buffer中, 对应于Channel.read(ByteBuffer[])
        -> gather(聚集): 将多个Buffer中的数据`聚集(gather)`后发送到Channel, 对应于Channel.write(ByteBuffer[])
         */
        readChannel.read(bufferArray); // scatter(分散)
        RandomAccessFile writeFile = new RandomAccessFile("/Users/xishang/temp/file.txt", "rw");
        FileChannel writeChannel = writeFile.getChannel();
        writeChannel.write(bufferArray); // gather(聚集)
    }

    public static void pipe() throws Exception {
        // 管道: sink -> source
        Pipe pipe = Pipe.open();
        // 写通道
        Pipe.SinkChannel sinkChannel = pipe.sink();
        // 读通道
        Pipe.SourceChannel sourceChannel = pipe.source();
        ByteBuffer writeBuffer = ByteBuffer.allocate(30);
        writeBuffer.clear();
        writeBuffer.put("Hello".getBytes());
        writeBuffer.flip();
        while (writeBuffer.hasRemaining()) {
            sinkChannel.write(writeBuffer);
        }
        ByteBuffer readBuffer = ByteBuffer.allocate(30);
        int length = sourceChannel.read(readBuffer);
        System.out.printf("length: %d\n", length);
        readBuffer.flip();
        while (readBuffer.hasRemaining()) {
            System.out.print((char) readBuffer.get());
        }
        readBuffer.clear();
    }

    public static void main(String[] args) throws Throwable {
        readFile();
    }

}
