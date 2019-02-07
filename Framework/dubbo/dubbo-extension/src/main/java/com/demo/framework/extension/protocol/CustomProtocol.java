package com.demo.framework.extension.protocol;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.RpcException;

/**
 * @author xishang
 * @version 1.0
 * @see com.alibaba.dubbo.rpc.protocol.injvm.InjvmProtocol
 * @see com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol
 * @see com.alibaba.dubbo.rpc.protocol.rmi.RmiProtocol
 * @see com.alibaba.dubbo.rpc.protocol.http.HttpProtocol
 * @see com.alibaba.dubbo.rpc.protocol.hessian.HessianProtocol
 * @since 2018/6/19
 * <p>
 * 自定义Protocol实现
 */
public class CustomProtocol implements Protocol {

    /**
     * 暴露远程服务：<br>
     * 1. 协议在接收请求时，应记录请求来源方地址信息：RpcContext.getContext().setRemoteAddress();<br>
     * 2. export()必须是幂等的，也就是暴露同一个URL的Invoker两次，和暴露一次没有区别。<br>
     * 3. export()传入的Invoker由框架实现并传入，协议不需要关心。<br>
     *
     * @param <T>     服务的类型
     * @param invoker 服务的执行体
     * @return exporter 暴露服务的引用，用于取消暴露
     * @throws RpcException 当暴露服务出错时抛出，比如端口已占用
     */
    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        return null;
    }

    /**
     * 引用远程服务：<br>
     * 1. 当用户调用refer()所返回的Invoker对象的invoke()方法时，协议需相应执行同URL远端export()传入的Invoker对象的invoke()方法。<br>
     * 2. refer()返回的Invoker由协议实现，协议通常需要在此Invoker中发送远程请求。<br>
     * 3. 当url中有设置check=false时，连接失败不能抛出异常，需内部自动恢复。<br>
     *
     * @param <T>  服务的类型
     * @param type 服务的类型
     * @param url  远程服务的URL地址
     * @return invoker 服务的本地代理
     * @throws RpcException 当连接服务提供方失败时抛出
     */
    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        return null;
    }

    @Override
    public int getDefaultPort() {
        return 0;
    }

    @Override
    public void destroy() {

    }

}
