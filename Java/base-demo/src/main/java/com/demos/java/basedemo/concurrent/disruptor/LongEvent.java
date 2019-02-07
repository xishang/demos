package com.demos.java.basedemo.concurrent.disruptor;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/6
 */
public class LongEvent {

    private long value;

    public void set(long value) {
        this.value = value;
    }

    public long get() {
        return value;
    }

}
