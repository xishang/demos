package com.demos.java.designpatterns.creational.factory;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 具体产品族(Mac系列)成员: MacButton
 */
public class MacButton implements Button {

    @Override
    public void click() {
        System.out.println("Click Button of Mac");
    }

}
