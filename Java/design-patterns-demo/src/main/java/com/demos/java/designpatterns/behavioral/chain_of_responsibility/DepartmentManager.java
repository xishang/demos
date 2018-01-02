package com.demos.java.designpatterns.behavioral.chain_of_responsibility;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/2
 * <p>
 * 具体处理器类: 部门经理
 */
public class DepartmentManager extends Approver {

    public DepartmentManager(String name) {
        super(name);
    }

    public DepartmentManager(String name, Approver successor) {
        super(name, successor);
    }

    @Override
    protected boolean canProcess(PurchaseRequest request) {
        // 部门经理只能处理1000元以下的采购申请
        return request.getAmount() <= 1000.0d;
    }

    @Override
    protected void doProcess(PurchaseRequest request) {
        System.out.println("采购请求{编号:" + request.getNumber() + ", 金额:" + request.getAmount() + ", 目的:" + request.getPurpose() + "}: 部门经理{" + super.name + "}已处理");
    }

}
