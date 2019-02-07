package com.demo.framework.netty.chat;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/7/3
 */
public class SimpleChatServerInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * 初始化连接
     *
     * @param channel
     * @throws Exception
     */
    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        // 粘包/拆包处理
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        // byte to string
        pipeline.addLast("decoder", new StringDecoder());
        // string to byte
        pipeline.addLast("encoder", new StringEncoder());
        // 聊天处理器
        pipeline.addLast("handler", new SimpleChatServerHandler());
        // log
        System.out.println("SimpleChatClient:" + channel.remoteAddress() + "已连接");
    }

}
