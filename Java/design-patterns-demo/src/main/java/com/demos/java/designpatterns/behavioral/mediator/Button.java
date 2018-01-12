package com.demos.java.designpatterns.behavioral.mediator;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/6
 * <p>
 * 具体同事类: 按钮
 */
public class Button extends Component {

    @Override
    public void update() {
        System.out.println("--- 按钮不需要更新 ---");
    }

}
