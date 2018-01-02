package com.demos.java.designpatterns.creational.factory;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 工厂方法模式: 动态创建对象
 */
public class ButtonFactoryMethod {

    public static Button createButton(String type) {
        if ("Mac".equals(type)) {
            return new MacButton();
        } else if ("Win".equals(type)) {
            return new WinButton();
        } else {
            throw new IllegalArgumentException();
        }
    }

}
