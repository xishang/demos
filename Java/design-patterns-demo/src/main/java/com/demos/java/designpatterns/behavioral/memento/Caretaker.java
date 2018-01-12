package com.demos.java.designpatterns.behavioral.memento;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/10
 * <p>
 * 负责人: 又称为管理者, 只负责保存备忘录, 不能对备忘录的内容进行操作或检查
 */
public class Caretaker {

    // 多重检查点, 使用列表来存储多个备忘录
    private List<Memento> mementos = new ArrayList<>();

    public Memento getMemento(int index) {
        return mementos.get(index);
    }

    public void addMemento(Memento memento) {
        mementos.add(memento);
    }

}
