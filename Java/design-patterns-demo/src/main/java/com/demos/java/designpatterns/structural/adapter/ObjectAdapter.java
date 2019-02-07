package com.demos.java.designpatterns.structural.adapter;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/22
 * <p>
 * 对象适配器: Adaptee作为构造参数
 */
public class ObjectAdapter implements Target {

    private Adaptee adaptee;

    public ObjectAdapter(Adaptee adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public void request() {
        adaptee.specificRequest();
    }

}
