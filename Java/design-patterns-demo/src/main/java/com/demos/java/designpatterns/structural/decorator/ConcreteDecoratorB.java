package com.demos.java.designpatterns.structural.decorator;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/22
 * <p>
 * 具体装饰器
 */
public class ConcreteDecoratorB extends Decorator {

    public ConcreteDecoratorB(Component component) {
        super(component);
    }

    @Override
    public void operation() {
        System.out.println("具体装饰B-开始 -----------");
        super.operation();
        System.out.println("具体装饰B-结束 -----------");
    }

}
