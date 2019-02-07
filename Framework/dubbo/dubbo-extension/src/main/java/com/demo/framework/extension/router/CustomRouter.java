package com.demo.framework.extension.router;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Router;

import java.util.List;

/**
 * @author xishang
 * @version 1.0
 * @see com.alibaba.dubbo.rpc.cluster.router.script.ScriptRouter
 * @see com.alibaba.dubbo.rpc.cluster.router.condition.ConditionRouter
 * @since 2018/6/22
 */
public class CustomRouter implements Router {

    @Override
    public URL getUrl() {
        return null;
    }

    @Override
    public <T> List<Invoker<T>> route(List<Invoker<T>> list, URL url, Invocation invocation) throws RpcException {
        return null;
    }

    @Override
    public int compareTo(Router o) {
        return 0;
    }

}
