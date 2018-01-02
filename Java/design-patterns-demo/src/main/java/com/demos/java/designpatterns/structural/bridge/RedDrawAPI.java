package com.demos.java.designpatterns.structural.bridge;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/22
 * <p>
 * 行为实现类: RedDrawAPI
 */
public class RedDrawAPI implements DrawAPI {

    @Override
    public void draw() {
        System.out.println("red.");
    }

}
