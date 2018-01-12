package com.demos.java.designpatterns.behavioral.command;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/2
 * <p>
 * 具体命令: 关闭开关
 */
public class TurnOffCommand implements Command {

    private Light light;

    public TurnOffCommand(Light light) {
        this.light = light;
    }

    @Override
    public void execute() {
        light.turnOff();
    }

}
