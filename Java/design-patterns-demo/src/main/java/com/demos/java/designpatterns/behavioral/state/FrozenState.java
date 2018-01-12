package com.demos.java.designpatterns.behavioral.state;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/8
 * <p>
 * 具体状态类: 冻结状态
 */
public class FrozenState extends AccountState {

    @Override
    public void handle(Account account) {
        System.out.println("账户{" + account.getName() + "}, 余额=" + account.getAmount() + ", 已冻结");
    }

}
