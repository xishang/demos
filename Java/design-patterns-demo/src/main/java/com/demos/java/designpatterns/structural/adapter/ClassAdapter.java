package com.demos.java.designpatterns.structural.adapter;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/22
 * <p>
 * 类适配器: 继承Adaptee
 */
public class ClassAdapter extends Adaptee implements Target {

    @Override
    public void request() {
        super.specificRequest();
    }

}
