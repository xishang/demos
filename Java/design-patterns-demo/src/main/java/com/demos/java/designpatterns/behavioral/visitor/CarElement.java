package com.demos.java.designpatterns.behavioral.visitor;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/11
 * <p>
 * 抽象元素, 定义一个accept()方法
 */
public interface CarElement {

    void accept(Visitor visitor);

}
