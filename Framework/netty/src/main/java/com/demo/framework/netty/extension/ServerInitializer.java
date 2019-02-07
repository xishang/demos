package com.demo.framework.netty.extension;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/7/4
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        // 添加自定义协议解析
        pipeline.addLast(new CustomProtocolEncoder());
        pipeline.addLast(new CustomProtocolDecoder());
        pipeline.addLast(new ServerHandler());
    }

}
