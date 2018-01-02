package com.demos.java.designpatterns.creational.factory;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 具体工厂类: Win系列产品工厂类
 */
public class WinFactory extends AbstractFactory {

    @Override
    public Button createButton() {
        return new WinButton();
    }

    @Override
    public Border createBorder() {
        return new WinBorder();
    }

}
