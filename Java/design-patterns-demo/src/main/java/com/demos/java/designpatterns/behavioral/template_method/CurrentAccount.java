package com.demos.java.designpatterns.behavioral.template_method;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/30
 * <p>
 * 具体类: 活期账户
 */
public class CurrentAccount extends Account {

    public CurrentAccount(double capital) {
        super(capital);
    }

    @Override
    protected double getRate() {
        return 0.02;
    }

    @Override
    protected String getType() {
        return "活期账户";
    }

    /**
     * 实现抽象类定义的钩子方法
     *
     * @param amount
     * @return
     */
    @Override
    protected boolean doValidation(double amount) {
        return amount > 0;
    }

}
