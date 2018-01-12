package com.demos.java.designpatterns.behavioral.mediator;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/6
 * <p>
 * 具体中介者类
 */
public class ConcreteMediator extends AbstractMediator {

    private Button button;
    private List list;
    private Text text;

    @Override
    public void componentChanged(Component component) {
        if (component == button) {
            System.out.println("--- 点击按钮 ---");
            list.update();
            text.update();
        } else if (component == list) {
            System.out.println("--- 选择列表项 ---");
            text.update();
        } else {
            System.out.println("--- 改变文本框 ---");
        }
    }

    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

}
