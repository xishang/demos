package com.demos.java.basedemo.concurrent.disruptor;

import com.lmax.disruptor.EventHandler;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/6
 */
public class LongEventHandler implements EventHandler<LongEvent> {

    @Override
    public void onEvent(LongEvent longEvent, long l, boolean b) throws Exception {
        System.out.println("onEvent: start!");
        Thread.sleep(3000);
        System.out.printf("onEvent: l = %d, value = %d\n", l, longEvent.get());
    }
}
