package com.demos.java.designpatterns.creational.factory;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 具体工厂类: Mac系列产品工厂类
 */
public class MacFactory extends AbstractFactory {

    @Override
    public Button createButton() {
        return new MacButton();
    }

    @Override
    public Border createBorder() {
        return new MacBorder();
    }

}
