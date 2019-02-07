package com.demo.framework.extension.registry;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.NotifyListener;

import java.util.List;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/25
 */
public class CustomNotifyListener implements NotifyListener {

    /**
     * 当收到服务变更通知时触发。
     * <p>
     * 通知需处理契约：<br>
     * 1. 总是以服务接口和数据类型为维度全量通知，即不会通知一个服务的同类型的部分数据，用户不需要对比上一次通知结果。<br>
     * 2. 订阅时的第一次通知，必须是一个服务的所有类型数据的全量通知。<br>
     * 3. 中途变更时，允许不同类型的数据分开通知，比如：providers, consumers, routes, overrides，允许只通知其中一种类型，但该类型的数据必须是全量的，不是增量的。<br>
     * 4. 如果一种类型的数据为空，需通知一个empty协议并带category参数的标识性URL数据。<br>
     * 5. 通知者(即注册中心实现)需保证通知的顺序，比如：单线程推送，队列串行化，带版本对比。<br>
     *
     * @param urls 已注册信息列表，总不为空，含义同{@link com.alibaba.dubbo.registry.RegistryService#lookup(URL)}的返回值。
     */
    @Override
    public void notify(List<URL> urls) {

    }

}
