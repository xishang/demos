package com.demos.java.designpatterns.behavioral.command;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/4
 * <p>
 * 宏命令: 执行一个命令的总和
 */
public class MacroCommand implements Command {

    private List<Command> commandList = new ArrayList<>();

    public void addCommand(Command command) {
        commandList.add(command);
    }

    public boolean removeCommand(Command command) {
        return commandList.remove(command);
    }

    @Override
    public void execute() {
        commandList.forEach(Command::execute);
    }

}
