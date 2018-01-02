package com.demos.java.designpatterns.behavioral.chain_of_responsibility;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/2
 * <p>
 * 具体处理器类: 董事长
 */
public class Chairman extends Approver {

    public Chairman(String name) {
        super(name);
    }

    public Chairman(String name, Approver successor) {
        super(name, successor);
    }

    @Override
    protected boolean canProcess(PurchaseRequest request) {
        // 董事长可以处理任何采购申请
        return true;
    }

    @Override
    protected void doProcess(PurchaseRequest request) {
        System.out.println("采购请求{编号:" + request.getNumber() + ", 金额:" + request.getAmount() + ", 目的:" + request.getPurpose() + "}: 董事长{" + super.name + "}已处理");
    }

}
