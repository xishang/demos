package com.demos.java.designpatterns.structural.facade;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/22
 * <p>
 * 门面模式: 为子系统中的一组接口提供一个统一的高层接口, 使得子系统更容易使用
 */
public class Computer {

    private CPU cpu;
    private Memory memory;

    public Computer() {
        cpu = new CPU();
        memory = new Memory();
    }

    public void start() {
        cpu.freeze();
        memory.load();
        cpu.jump();
        cpu.execute();
    }

}
