package com.demos.java.designpatterns.behavioral.template_method;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/30
 * <p>
 * 模版方法模式: 定义一个算法的骨架, 模版类中实现不变的部分, 具体子类实现变化的部分
 * 抽象模版类: 账户类
 * 典型应用: Servlet(service, doGet, doPost等方法)
 */
public abstract class Account {

    // 本金
    private double capital;

    public Account(double capital) {
        this.capital = capital;
    }

    /**
     * 模版方法: 在抽象类中定义, 把基本操作方法组合在一起形成一个总算法或一个总行为的方法
     * 模版方法调用的基本方法类型:
     * 1.抽象方法(Abstract Method): 子类必须实现
     * 2.具体方法(Concrete Method): 有抽象类实现, 子类不能重写
     * 3.钩子方法(Hook Method): 抽象类给出空方法(Do Nothing Hook), 子类可选择实现该方法. Java中通常为doXxx()形式
     *
     * @return
     */
    public final double calculateInterest() {
        if (!doValidation(capital)) {
            return 0d;
        }
        double interest = capital * getRate();
        display(interest);
        return interest;
    }

    /**
     * 抽象方法: 利率
     *
     * @return
     */
    protected abstract double getRate();

    /**
     * 抽象方法: 账户类型
     *
     * @return
     */
    protected abstract String getType();

    /**
     * 钩子方法: 子类可选择进行验证
     *
     * @return
     */
    protected boolean doValidation(double amount) {
        return true;
    }

    /**
     * 具体方法: 展示金额信息
     *
     * @param interest
     */
    private void display(double interest) {
        System.out.println("账户类型: " + getType() + ", 本金: " + capital + ", 利息: " + interest);
    }

}
