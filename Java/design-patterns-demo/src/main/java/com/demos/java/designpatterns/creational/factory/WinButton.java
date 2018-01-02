package com.demos.java.designpatterns.creational.factory;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 具体产品族(Win系列)成员: WinButton
 */
public class WinButton implements Button {

    @Override
    public void click() {
        System.out.println("Click Button of Win");
    }

}
