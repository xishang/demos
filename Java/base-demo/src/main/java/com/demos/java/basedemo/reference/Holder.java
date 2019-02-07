package com.demos.java.basedemo.reference;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/7/1
 */
public class Holder {

    private Object value;

    public Holder(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
