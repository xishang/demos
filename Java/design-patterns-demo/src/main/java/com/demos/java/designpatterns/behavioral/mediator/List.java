package com.demos.java.designpatterns.behavioral.mediator;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/6
 * <p>
 * 具体同事类: 列表项
 */
public class List extends Component {

    @Override
    public void update() {
        System.out.println("--- 列表项更新 ---");
    }

}
