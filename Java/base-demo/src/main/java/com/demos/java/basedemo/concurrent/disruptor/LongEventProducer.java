package com.demos.java.basedemo.concurrent.disruptor;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;

import java.nio.ByteBuffer;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/6
 */
public class LongEventProducer {

    private static final EventTranslatorOneArg<LongEvent, ByteBuffer> translator = (event, sequence, buffer) -> {
        event.set(buffer.get(0)); // // Fill with data
    };

    private final RingBuffer<LongEvent> ringBuffer;

    public LongEventProducer(RingBuffer<LongEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    // 发布写入的数据
    public void onData(ByteBuffer bb) {
        ringBuffer.publishEvent(translator, bb);
    }

}
