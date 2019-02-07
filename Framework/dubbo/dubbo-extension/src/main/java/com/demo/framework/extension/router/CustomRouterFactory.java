package com.demo.framework.extension.router;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.cluster.Router;
import com.alibaba.dubbo.rpc.cluster.RouterFactory;

/**
 * @author xishang
 * @version 1.0
 * @see com.alibaba.dubbo.rpc.cluster.router.script.ScriptRouterFactory
 * @see com.alibaba.dubbo.rpc.cluster.router.file.FileRouterFactory
 * @see com.alibaba.dubbo.rpc.cluster.router.condition.ConditionRouterFactory
 * @since 2018/6/22
 * <p>
 * 路由扩展: 根据路由规则过滤出Invoker子集
 */
public class CustomRouterFactory implements RouterFactory {

    @Override
    public Router getRouter(URL url) {
        return null;
    }

}
