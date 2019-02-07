package com.demo.framework.netty.extension;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/7/11
 */
public class OutboundHandler2 extends ChannelOutboundHandlerAdapter {

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        System.out.println("outbound2: read");
        super.read(ctx);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // 处理数据
        System.out.println("outbound2: write");
        // 调用super.write()将将执行权传递给next: ctx.write
        super.write(ctx, msg, promise);
    }

}
