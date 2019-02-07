package com.demo.framework.extension.filter;

import com.alibaba.dubbo.rpc.*;

/**
 * @author xishang
 * @version 1.0
 * @see com.alibaba.dubbo.rpc.filter.EchoFilter
 * @see com.alibaba.dubbo.rpc.filter.GenericFilter
 * @see com.alibaba.dubbo.rpc.filter.GenericImplFilter
 * @see com.alibaba.dubbo.rpc.filter.TokenFilter
 * @see com.alibaba.dubbo.rpc.filter.AccessLogFilter
 * @see com.alibaba.dubbo.rpc.filter.ActiveLimitFilter
 * @see com.alibaba.dubbo.rpc.filter.ClassLoaderFilter
 * @see com.alibaba.dubbo.rpc.filter.ContextFilter
 * @see com.alibaba.dubbo.rpc.filter.ConsumerContextFilter
 * @see com.alibaba.dubbo.rpc.filter.ExceptionFilter
 * @see com.alibaba.dubbo.rpc.filter.ExecuteLimitFilter
 * @see com.alibaba.dubbo.rpc.filter.DeprecatedFilter
 * @since 2018/6/22
 * <p>
 * <!-- 消费方调用过程拦截 -->
 * <dubbo:reference filter="xxx,yyy" />
 * <!-- 消费方调用过程缺省拦截器，将拦截所有reference -->
 * <dubbo:consumer filter="xxx,yyy"/>
 * <!-- 提供方调用过程拦截 -->
 * <dubbo:service filter="xxx,yyy" />
 * <!-- 提供方调用过程缺省拦截器，将拦截所有service -->
 * <dubbo:provider filter="xxx,yyy"/>
 * <p>
 * 调用拦截扩展: 服务提供方和服务消费方调用过程拦截
 * === Dubbo本身的大多功能均基于此扩展点实现, 每次远程方法执行, 该拦截都会被执行, 请注意对性能的影响
 */
public class CustomFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // before filter ...
        Result result = invoker.invoke(invocation);
        // after filter ...
        return result;
    }

}
