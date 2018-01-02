package com.demos.java.designpatterns.behavioral.chain_of_responsibility;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/2
 * <p>
 * 责任链模式
 * 纯的责任链模式: 处理者要么承担全部责任, 要么把责任推给下家
 * 优点: 解耦请求和处理者, 很容易添加新的处理者
 * 缺点: 较长的责任链会导致性能低下, 建链不当可能造成死循环
 * 抽象处理器类: 审批人
 */
public abstract class Approver {

    protected String name; // 审批者姓名
    protected Approver successor; // 后继处理对象

    public Approver(String name) {
        this.name = name;
    }

    public Approver(String name, Approver successor) {
        this.name = name;
        this.successor = successor;
    }

    /**
     * 设置后继者
     *
     * @param successor
     */
    public void setSuccessor(Approver successor) {
        this.successor = successor;
    }

    /**
     * 请求处理逻辑
     *
     * @param request
     */
    public final void processRequest(PurchaseRequest request) {
        if (canProcess(request)) { // 当前对象可以处理则自行处理
            this.doProcess(request);
        } else if (successor != null) { // 不能处理且有后继者, 交给后继者处理
            successor.processRequest(request);
        } else { // 无法处理
            System.out.println("采购请求{编号:" + request.getNumber() + ", 金额:" + request.getAmount() + ", 目的:" + request.getPurpose() + "}: 无法处理");
        }
    }

    /**
     * 是否能够处理该请求
     *
     * @param request
     * @return
     */
    protected abstract boolean canProcess(PurchaseRequest request);

    /**
     * 抽象请求处理方法
     *
     * @param request
     */
    protected abstract void doProcess(PurchaseRequest request);

}
