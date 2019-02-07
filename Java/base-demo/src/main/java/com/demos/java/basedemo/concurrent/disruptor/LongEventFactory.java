package com.demos.java.basedemo.concurrent.disruptor;

import com.lmax.disruptor.EventFactory;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/6
 */
public class LongEventFactory implements EventFactory<LongEvent> {

    @Override
    public LongEvent newInstance() {
        return new LongEvent();
    }

}
