package com.demos.java.designpatterns.behavioral.memento;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/10
 * <p>
 * 原发器: 可以创建一个备忘录, 并存储它的当前内部状态, 也可以使用备忘录来恢复其内部状态
 */
public class Originator {

    private String state;

    public Memento createMemento() {
        return new InnerMemento(state);
    }

    public void restoreMemento(Memento memento) {
        this.state = ((InnerMemento) memento).state;
    }

    /**
     * 黑箱实现: 内部备忘录
     */
    private class InnerMemento implements Memento {
        private String state;

        private InnerMemento(String state) {
            this.state = state;
        }
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

}
