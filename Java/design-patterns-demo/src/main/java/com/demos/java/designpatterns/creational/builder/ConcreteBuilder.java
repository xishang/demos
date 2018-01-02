package com.demos.java.designpatterns.creational.builder;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 具体构建类
 */
public class ConcreteBuilder extends Builder {

    @Override
    public void buildPartA() {
        super.product.setPartA("组装具体部件A");
    }

    @Override
    public void buildPartB() {
        super.product.setPartB("组装具体部件B");
    }

    @Override
    public void buildPartC() {
        super.product.setPartC("组装具体部件C");
    }

}
