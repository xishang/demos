package com.demo.framework.extension.cluster;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.directory.AbstractDirectory;

import java.util.List;

/**
 * @author xishang
 * @version 1.0
 * @see com.alibaba.dubbo.registry.integration.RegistryDirectory
 * @see com.alibaba.dubbo.rpc.cluster.directory.StaticDirectory
 * @since 2018/6/25
 * <p>
 * Directory扩展: 根据Invocation获取Invoker集合, 如从注册中心获取
 */
public class CustomDirectory extends AbstractDirectory {

    public CustomDirectory(URL url) {
        super(url);
    }

    @Override
    protected List<Invoker> doList(Invocation invocation) throws RpcException {
        return null;
    }

    @Override
    public Class getInterface() {
        return null;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

}
