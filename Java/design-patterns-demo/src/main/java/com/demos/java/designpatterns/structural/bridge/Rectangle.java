package com.demos.java.designpatterns.structural.bridge;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/22
 * <p>
 * 抽象类的子类: Rectangle
 */
public class Rectangle extends Shape {

    public Rectangle(DrawAPI drawAPI) {
        super(drawAPI);
    }

    @Override
    public void apply() {
        System.out.print("Draw Rectangle with ");
        super.drawAPI.draw();
    }

}
