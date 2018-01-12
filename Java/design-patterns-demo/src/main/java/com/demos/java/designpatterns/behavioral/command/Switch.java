package com.demos.java.designpatterns.behavioral.command;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/2
 * <p>
 * 请求者类: 开关请求
 */
public class Switch {

    private TurnOnCommand turnOnCommand;

    private TurnOffCommand turnOffCommand;

    public void setTurnOnCommand(TurnOnCommand turnOnCommand) {
        this.turnOnCommand = turnOnCommand;
    }

    public void setTurnOffCommand(TurnOffCommand turnOffCommand) {
        this.turnOffCommand = turnOffCommand;
    }

    public void turnOn() {
        turnOnCommand.execute();
    }

    public void turnOff() {
        turnOffCommand.execute();
    }

}
