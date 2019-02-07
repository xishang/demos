package com.demo.framework.extension.loadbalance;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;

import java.util.List;

/**
 * @author xishang
 * @version 1.0
 * @see com.alibaba.dubbo.rpc.cluster.loadbalance.RandomLoadBalance
 * @see com.alibaba.dubbo.rpc.cluster.loadbalance.RoundRobinLoadBalance
 * @see com.alibaba.dubbo.rpc.cluster.loadbalance.LeastActiveLoadBalance
 * @since 2018/6/22
 * <p>
 * 负载均衡扩展: 从Invoker集合中选出一个Invoker用于执行调用
 */
public class CustomLoadBalance implements LoadBalance {

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> list, URL url, Invocation invocation) throws RpcException {
        return null;
    }

}
