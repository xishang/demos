package com.demos.java.designpatterns.structural.adapter;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/22
 * <p>
 * 需要适配的类
 */
public class Adaptee {

    public void specificRequest() {
        System.out.println("需要适配");
    }

}
