package com.demos.java.designpatterns.structural.bridge;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/22
 * <p>
 * 桥接模式: 将抽象与行为解耦
 * Shape为抽象类, 包含行为接口DrawAPI
 */
public abstract class Shape {

    protected DrawAPI drawAPI;

    public Shape(DrawAPI drawAPI) {
        this.drawAPI = drawAPI;
    }

    public abstract void apply();

}
