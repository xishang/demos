package com.demos.java.designpatterns.behavioral.visitor;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/11
 * <p>
 * 具体元素
 */
public class Engine implements CarElement {

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    /**
     * 对象特有方法
     * @return
     */
    public String getPrice() {
        return "$1000.00";
    }

    /**
     * 对象特有方法
     * @return
     */
    public String getPower() {
        return "800 horse power";
    }

}
