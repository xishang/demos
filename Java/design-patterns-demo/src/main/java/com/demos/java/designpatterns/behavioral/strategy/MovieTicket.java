package com.demos.java.designpatterns.behavioral.strategy;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/6
 * <p>
 * 环境类: 电影票
 */
public class MovieTicket {

    private PriceStrategy priceStrategy;

    public MovieTicket(PriceStrategy priceStrategy) {
        this.priceStrategy = priceStrategy;
    }

    public double realPrice(double price) {
        return priceStrategy.calculate(price);
    }

}
