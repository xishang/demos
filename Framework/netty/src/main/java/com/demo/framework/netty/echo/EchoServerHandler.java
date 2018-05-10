package com.demo.framework.netty.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/8
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 对于每个传入的消息都要调用
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        System.out.println("Server received: " + in.toString(CharsetUtil.UTF_8));
        ctx.write(in); // 写入数据且暂不刷出
    }

    /**
     * 消息读取完毕
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER) // 刷出写入的消息到远端
                .addListener(ChannelFutureListener.CLOSE); // 操作完成关闭channel
    }

    /**
     * 异常处理
     * <p>
     * 若exceptionCaught()方法未被实现, 该异常将沿着ChannelHandler链一直传递到ChannelPipeline的尾端并被记录
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
