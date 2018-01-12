package com.demos.java.designpatterns.behavioral.state;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/8
 * <p>
 * 环境类: 维护状态的实例
 */
public class Account {

    // 定义账户状态枚举值
    private static final AccountState NORMAL_STATE = new NormalState();
    private static final AccountState OVERDRAWN_STATE = new OverdrawnState();
    private static final AccountState FROZEN_STATE = new FrozenState();

    // 账户当前状态
    private AccountState currentState;

    // 账户名
    private String name;

    // 账户余额
    private double amount;

    public Account(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    /**
     * 设置余额
     *
     * @param amount
     */
    public void setAmount(double amount) {
        this.amount = amount;
        changeState();
    }

    /**
     * 改变状态: 自己充当状态管理器(State Manager)的角色
     */
    private void changeState() {
        if (amount > 0) {
            this.currentState = NORMAL_STATE;
        } else if (amount > -1000) {
            this.currentState = OVERDRAWN_STATE;
        } else {
            this.currentState = FROZEN_STATE;
        }
        this.currentState.handle(this);
    }

}
