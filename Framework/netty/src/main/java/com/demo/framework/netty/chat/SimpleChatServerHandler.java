package com.demo.framework.netty.chat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/7/3
 */
public class SimpleChatServerHandler extends SimpleChannelInboundHandler<String> {

    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 服务端收到新的客户端连接
     *
     * @param context
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext context) throws Exception {
        // 新添加的客户端
        Channel client = context.channel();
        // Broadcast a message to multiple Channels
        channels.writeAndFlush("[SERVER] - " + client.remoteAddress() + " 加入\n");
        // 将客户端添加到广播组
        channels.add(client);
    }

    /**
     * 客户端断开连接
     *
     * @param context
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext context) throws Exception {
        // 断开连接的客户端
        Channel client = context.channel();
        // Broadcast a message to multiple Channels
        channels.writeAndFlush("[SERVER] - " + client.remoteAddress() + " 离开\n");
        // 断开连接的客户端Channel会自动从ChannelGroup移除, 因此不需要手动调用`channels.remove(client);`
        // A closed Channel is automatically removed from ChannelGroup,
        // so there is no need to do "channels.remove(context.channel());"
    }

    /**
     * 客户端写入数据
     *
     * @param context
     * @param content
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext context, String content) throws Exception { // (4)
        Channel client = context.channel();
        for (Channel channel : channels) {
            if (channel != client) {
                channel.writeAndFlush("[" + client.remoteAddress() + "]" + content + "\n");
            } else {
                channel.writeAndFlush("[you]" + content + "\n");
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
        Channel client = context.channel();
        System.out.println("SimpleChatClient:" + client.remoteAddress() + "在线");
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) throws Exception {
        Channel client = context.channel();
        System.out.println("SimpleChatClient:" + client.remoteAddress() + "掉线");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        Channel client = context.channel();
        System.out.println("SimpleChatClient:" + client.remoteAddress() + "异常");
        // 当出现异常就关闭连接
        cause.printStackTrace();
        context.close();
    }

}
