package com.demos.java.designpatterns.structural.bridge;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/22
 *
 * 抽象类的子类: Triangle
 */
public class Triangle extends Shape {

    public Triangle(DrawAPI drawAPI) {
        super(drawAPI);
    }

    @Override
    public void apply() {
        System.out.print("Draw Triangle with ");
        super.drawAPI.draw();
    }

}
