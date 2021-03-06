package com.demos.java.designpatterns.structural.flyweight;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 具体享元类: 白色棋子
 */
public class WhiteGoChess extends GoChess {

    @Override
    protected String color() {
        return "白色";
    }

}
