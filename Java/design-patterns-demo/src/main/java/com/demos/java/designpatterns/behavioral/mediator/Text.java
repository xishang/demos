package com.demos.java.designpatterns.behavioral.mediator;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/6
 * <p>
 * 具体同事类: 文本框
 */
public class Text extends Component {

    @Override
    public void update() {
        System.out.println("--- 文本框更新 ---");
    }

}
