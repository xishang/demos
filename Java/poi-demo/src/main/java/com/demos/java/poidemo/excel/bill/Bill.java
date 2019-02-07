package com.demos.java.poidemo.excel.bill;

import java.math.BigDecimal;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/11/7
 */
public class Bill {

    // 摘要
    private String summary;
    // 科目名称
    private String subject;
    // 借方金额
    private BigDecimal borrowAmount;
    // 贷方金额
    private BigDecimal lendAmount;

    public Bill(String summary, String subject, BigDecimal borrowAmount, BigDecimal lendAmount) {
        this.summary = summary;
        this.subject = subject;
        this.borrowAmount = borrowAmount;
        this.lendAmount = lendAmount;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public BigDecimal getBorrowAmount() {
        return borrowAmount;
    }

    public void setBorrowAmount(BigDecimal borrowAmount) {
        this.borrowAmount = borrowAmount;
    }

    public BigDecimal getLendAmount() {
        return lendAmount;
    }

    public void setLendAmount(BigDecimal lendAmount) {
        this.lendAmount = lendAmount;
    }

}
