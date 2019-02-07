package com.demo.framework.netty.extension;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/7/4
 */
public class ClientHandler2 extends SimpleChannelInboundHandler<CustomProtocol> {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("client handler1: channelRead");
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, CustomProtocol protocol) throws Exception {
        System.out.println("handler2: client receive message : " + protocol);
    }

}
