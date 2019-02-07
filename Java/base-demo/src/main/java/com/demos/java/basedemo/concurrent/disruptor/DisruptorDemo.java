package com.demos.java.basedemo.concurrent.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/6
 */
public class DisruptorDemo {

    public static void main(String[] args) throws InterruptedException {
        // Executor that will be used to construct new threads for consumers
        // Executor executor = Executors.newCachedThreadPool();

        // Use ThreadFactory
        ThreadFactory threadFactory = Executors.defaultThreadFactory();

        // The factory for the event
        LongEventFactory factory = new LongEventFactory();

        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 1024;

        // Construct the Disruptor
        Disruptor<LongEvent> disruptor = new Disruptor<>(factory, bufferSize, threadFactory);

        // Consumer的消费为事件驱动, 且为同步处理
        // Connect the handler
        disruptor.handleEventsWith(new LongEventHandler());

        // Start the Disruptor, starts all threads running
        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        LongEventProducer producer = new LongEventProducer(ringBuffer);

        ByteBuffer buffer = ByteBuffer.allocate(8);
        for (long l = 0; true; l++) {
            buffer.putLong(0, l);
            producer.onData(buffer);
            Thread.sleep(1000);
        }

    }

}
