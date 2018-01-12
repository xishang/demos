package com.demos.java.designpatterns.behavioral.strategy;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/6
 * <p>
 * 具体策略类: VIP价格策略
 */
public class VipPrice implements PriceStrategy {

    @Override
    public double calculate(double price) {
        return 0.8 * price;
    }

}
