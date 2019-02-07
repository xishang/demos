package com.demo.framework.extension.protocol;

import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.protocol.AbstractExporter;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/19
 * <p>
 * 自定义Exporter实现
 */
public class CustomExporter<T> extends AbstractExporter<T> {

    public CustomExporter(Invoker<T> invoker) throws RemotingException {
        super(invoker);
        // ...
    }

    public void unexport() {
        super.unexport();
        // ...
    }

}
