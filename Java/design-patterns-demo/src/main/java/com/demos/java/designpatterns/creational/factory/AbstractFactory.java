package com.demos.java.designpatterns.creational.factory;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 抽象工厂模式: 提供接口, 创建一系列相关或独立的对象, 而不指定这些对象的具体类
 * 抽象工厂类
 */
public abstract class AbstractFactory {

    public abstract Button createButton();

    public abstract Border createBorder();

    public static AbstractFactory getInstance(String type) {
        if ("Mac".equals(type)) {
            return new MacFactory();
        } else if ("Win".equals(type)) {
            return new WinFactory();
        } else {
            throw new IllegalArgumentException();
        }
    }

}
