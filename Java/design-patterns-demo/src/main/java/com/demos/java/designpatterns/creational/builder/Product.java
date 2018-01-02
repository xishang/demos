package com.demos.java.designpatterns.creational.builder;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 产品类: 需要被构建的复杂对象, 包含多个部件
 */
public class Product {

    private String partA;
    private String partB;
    private String partC;

    public String getPartA() {
        return partA;
    }

    public void setPartA(String partA) {
        this.partA = partA;
    }

    public String getPartB() {
        return partB;
    }

    public void setPartB(String partB) {
        this.partB = partB;
    }

    public String getPartC() {
        return partC;
    }

    public void setPartC(String partC) {
        this.partC = partC;
    }

}
