package com.demos.java.designpatterns.behavioral.command;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/2
 * <p>
 * 具体命令: 打开开关
 */
public class TurnOnCommand implements Command {

    private Light light;

    public TurnOnCommand(Light light) {
        this.light = light;
    }

    @Override
    public void execute() {
        light.turnOn();
    }

}
