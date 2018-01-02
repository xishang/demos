package com.demos.java.designpatterns.creational.prototype;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 具体原型类
 */
public class AnalysisReport extends Document {

    public AnalysisReport() {
        super("可行性报告");
    }

    @Override
    public void display() {
        System.out.println("可行性报告");
    }

}
