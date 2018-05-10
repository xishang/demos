package com.demo.framework.netty.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/8
 */
public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    /**
     * 客户端到服务器的连接建立之后调用
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 连接建立之后向服务器发送消息
        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));
    }

    /**
     * 每次从服务器接收到消息时调用, 不同于ChannelInboundHandlerAdapter.channelRead()方法
     * <p>
     * 该方法返回时表示已经处理完成, SimpleChannelInboundHandler会负责释放保存该消息的ByteBuf的内容引用
     *
     * @param ctx
     * @param in
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        System.out.println("Client received: " + in.toString(CharsetUtil.UTF_8));
    }

    /**
     * 异常处理
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace(); // 打印异常栈信息
        ctx.close(); // 关闭channel
    }

}
