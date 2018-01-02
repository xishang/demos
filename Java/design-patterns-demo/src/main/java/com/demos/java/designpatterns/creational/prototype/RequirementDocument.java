package com.demos.java.designpatterns.creational.prototype;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 具体原型类
 */
public class RequirementDocument extends Document {

    public RequirementDocument() {
        super("需求文档");
    }

    @Override
    public void display() {
        System.out.println("需求文档");
    }

}
