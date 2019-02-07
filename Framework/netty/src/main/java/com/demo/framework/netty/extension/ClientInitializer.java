package com.demo.framework.netty.extension;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/7/4
 */
public class ClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        // 添加自定义协议解析, 对于ChannelHandler
        // read: 从head到tail
        // write: 从tail到head
        pipeline.addLast(new CustomProtocolDecoder());
//        pipeline.addLast(new OutboundHandler1());
//        pipeline.addLast(new OutboundHandler2());
        pipeline.addLast(new CustomProtocolEncoder());
        pipeline.addLast(new ClientHandler());
        pipeline.addLast(new ClientHandler2());
    }

}
