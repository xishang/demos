package com.demos.java.designpatterns.structural.flyweight;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/22
 * <p>
 * 享元模式: 运用共享技术有效地支持大量细粒度对象的复用
 * 典型应用: String常量池
 * 享元工厂类
 */
public class GoChessFactory {

    private static GoChessFactory instance = new GoChessFactory();

    private Map<String, GoChess> map = new HashMap<>();

    private GoChessFactory() {
        map.put("black", new BlackGoChess());
        map.put("white", new WhiteGoChess());
    }

    public static GoChessFactory getInstance() {
        return instance;
    }

    public GoChess getGoChess(String color) {
        return map.get(color);
    }

}
