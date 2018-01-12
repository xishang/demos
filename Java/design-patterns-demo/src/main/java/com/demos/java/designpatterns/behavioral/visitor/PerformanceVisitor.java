package com.demos.java.designpatterns.behavioral.visitor;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/11
 * <p>
 * 具体访问者
 */
public class PerformanceVisitor implements Visitor {

    @Override
    public void visit(Engine engine) {
        System.out.println("性能分析(引擎): 功率 = " + engine.getPower());
    }

    @Override
    public void visit(Wheel wheel) {
        System.out.println("性能分析(轮胎): 里程数 = " + wheel.getMileage());
    }

}
