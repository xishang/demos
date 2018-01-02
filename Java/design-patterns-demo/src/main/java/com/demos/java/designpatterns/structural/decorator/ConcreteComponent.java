package com.demos.java.designpatterns.structural.decorator;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/22
 * <p>
 * 具体组件
 */
public class ConcreteComponent implements Component {

    @Override
    public void operation() {
        System.out.println("具体组件 ");
    }

}
