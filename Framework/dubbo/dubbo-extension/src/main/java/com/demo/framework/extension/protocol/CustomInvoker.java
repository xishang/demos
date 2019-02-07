package com.demo.framework.extension.protocol;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.protocol.AbstractInvoker;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/19
 * <p>
 * 自定义Invoker实现
 */
public class CustomInvoker<T> extends AbstractInvoker<T> {

    public CustomInvoker(Class<T> type, URL url) throws RemotingException {
        super(type, url);
    }

    @Override
    protected Result doInvoke(Invocation invocation) throws Throwable {
        return null;
    }

}
