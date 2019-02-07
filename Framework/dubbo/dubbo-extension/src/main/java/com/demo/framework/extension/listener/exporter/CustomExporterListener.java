package com.demo.framework.extension.listener.exporter;

import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.ExporterListener;
import com.alibaba.dubbo.rpc.RpcException;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/22
 * <p>
 * 暴露监听扩展: 当有服务暴露时, 触发该事件
 */
public class CustomExporterListener implements ExporterListener {

    @Override
    public void exported(Exporter<?> exporter) throws RpcException {

    }

    @Override
    public void unexported(Exporter<?> exporter) {

    }
}
