package com.demos.java.designpatterns.creational.factory;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 具体产品族(Mac系列)成员: MacBorder
 */
public class MacBorder implements Border {

    @Override
    public void show() {
        System.out.println("Border of Mac");
    }

}
