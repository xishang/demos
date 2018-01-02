package com.demos.java.designpatterns.structural.flyweight;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 抽象享元类: 围棋棋子
 */
public abstract class GoChess {

    protected abstract String color();

    public String display() {
        return color() + "棋子";
    }

}
