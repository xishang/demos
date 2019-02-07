package com.demo.framework.extension.merger;

import com.alibaba.dubbo.rpc.cluster.Merger;

/**
 * @author xishang
 * @version 1.0
 * @see com.alibaba.dubbo.rpc.cluster.merger.ArrayMerger
 * @see com.alibaba.dubbo.rpc.cluster.merger.ListMerger
 * @see com.alibaba.dubbo.rpc.cluster.merger.SetMerger
 * @see com.alibaba.dubbo.rpc.cluster.merger.MapMerger
 * @since 2018/6/22
 * <p>
 * 合并结果扩展: 合并返回结果, 用于分组聚合
 */
public class CustomMerger implements Merger {

    @Override
    public Object merge(Object[] objects) {
        return null;
    }

}
