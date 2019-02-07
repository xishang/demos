package com.demo.framework.extension.cluster;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Cluster;
import com.alibaba.dubbo.rpc.cluster.Directory;

/**
 * @author xishang
 * @version 1.0
 * @see com.alibaba.dubbo.rpc.cluster.support.FailoverCluster
 * @see com.alibaba.dubbo.rpc.cluster.support.FailfastCluster
 * @see com.alibaba.dubbo.rpc.cluster.support.FailsafeCluster
 * @see com.alibaba.dubbo.rpc.cluster.support.FailbackCluster
 * @see com.alibaba.dubbo.rpc.cluster.support.ForkingCluster
 * @see com.alibaba.dubbo.rpc.cluster.support.AvailableCluster
 * @since 2018/6/22
 * <p>
 * 将多个服务提供方组织成一个集群, 并伪装成一个提供方
 */
public class CustomCluster implements Cluster {

    @Override
    public <T> Invoker<T> join(Directory<T> directory) throws RpcException {
        return null;
    }

}
