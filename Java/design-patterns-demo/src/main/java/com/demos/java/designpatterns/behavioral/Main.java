package com.demos.java.designpatterns.behavioral;

import com.demos.java.designpatterns.behavioral.chain_of_responsibility.*;
import com.demos.java.designpatterns.behavioral.template_method.Account;
import com.demos.java.designpatterns.behavioral.template_method.CurrentAccount;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 */
public class Main {

    public static void main(String[] args) {
        // 模版方法模式
        Account account = new CurrentAccount(1000.00d);
        account.calculateInterest();
        // 责任链模式
        Approver cm = new Chairman("James");
        Approver gm = new GeneralManager("Jack", cm);
        Approver dm = new DepartmentManager("Tom", gm);
        dm.processRequest(new PurchaseRequest(800.0d, 1, "购买显示器"));
        dm.processRequest(new PurchaseRequest(6000.0d, 1, "购买电脑"));
        dm.processRequest(new PurchaseRequest(12000.0d, 1, "购买服务器"));
    }

}
