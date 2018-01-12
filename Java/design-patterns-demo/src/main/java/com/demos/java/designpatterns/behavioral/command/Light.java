package com.demos.java.designpatterns.behavioral.command;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/2
 * <p>
 * 接收者类: 负责执行请求相关操作
 */
public class Light {

    public void turnOn() {
        System.out.println("The light is on");
    }

    public void turnOff() {
        System.out.println("The light is off");
    }

}
