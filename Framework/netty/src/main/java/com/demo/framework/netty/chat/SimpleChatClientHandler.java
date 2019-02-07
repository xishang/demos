package com.demo.framework.netty.chat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/7/3
 */
public class SimpleChatClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext context, String s) throws Exception {
        System.out.println(s);
    }

}
