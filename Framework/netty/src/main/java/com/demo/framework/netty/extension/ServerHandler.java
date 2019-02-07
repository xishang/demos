package com.demo.framework.netty.extension;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/7/4
 */
public class ServerHandler extends SimpleChannelInboundHandler<CustomProtocol> {

    @Override
    protected void channelRead0(ChannelHandlerContext context, CustomProtocol protocol) throws Exception {
        System.out.println("server receive message : " + protocol);
        Channel channel = context.channel();
        // 回复消息
        CustomProtocol response = new CustomProtocol();
        response.setVersion(1);
        response.setContent("你好，已收到你的消息！");
        channel.writeAndFlush(response);
    }

}
