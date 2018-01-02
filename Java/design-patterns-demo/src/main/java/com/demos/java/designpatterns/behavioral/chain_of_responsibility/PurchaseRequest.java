package com.demos.java.designpatterns.behavioral.chain_of_responsibility;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/2
 * <p>
 * 请求类: 采购请求
 */
public class PurchaseRequest {

    private double amount;  // 采购金额
    private int number;  // 采购单编号
    private String purpose;  // 采购目的

    public PurchaseRequest(double amount, int number, String purpose) {
        this.amount = amount;
        this.number = number;
        this.purpose = purpose;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return this.amount;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getNumber() {
        return this.number;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getPurpose() {
        return this.purpose;
    }

}
