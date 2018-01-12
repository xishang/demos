package com.demos.java.designpatterns.behavioral.visitor;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/11
 * <p>
 * 具体访问者
 */
public class PriceVisitor implements Visitor {

    @Override
    public void visit(Engine engine) {
        System.out.println("引擎价格: " + engine.getPrice());
    }

    @Override
    public void visit(Wheel wheel) {
        System.out.println("轮胎价格: " + wheel.getPrice());
    }

}
