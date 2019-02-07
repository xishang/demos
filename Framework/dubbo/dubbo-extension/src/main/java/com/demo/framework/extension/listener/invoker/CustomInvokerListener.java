package com.demo.framework.extension.listener.invoker;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.InvokerListener;
import com.alibaba.dubbo.rpc.RpcException;

/**
 * @author xishang
 * @version 1.0
 * @see com.alibaba.dubbo.rpc.listener.DeprecatedInvokerListener
 * @since 2018/6/22
 * <p>
 * 引用监听扩展: 当有服务引用时, 触发该事件
 */
public class CustomInvokerListener implements InvokerListener {

    @Override
    public void referred(Invoker<?> invoker) throws RpcException {

    }

    @Override
    public void destroyed(Invoker<?> invoker) {

    }

}
